package dev.rosewood.rosestacker.stack.settings.spawner.tags;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTag;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class MaxNearbyEntityConditionTag extends ConditionTag {

    private int maxNearbyEntities;
    private StackManager stackManager;
    private EntityCacheManager entityCacheManager;

    public MaxNearbyEntityConditionTag(String tag) {
        super(tag, false);
    }

    @Override
    public boolean check(CreatureSpawner creatureSpawner, SpawnerStackSettings stackSettings, Block spawnBlock) {
        if (this.stackManager == null || this.entityCacheManager == null) {
            this.stackManager = RoseStacker.getInstance().getManager(StackManager.class);
            this.entityCacheManager = RoseStacker.getInstance().getManager(EntityCacheManager.class);
        }

        int detectionRange = stackSettings.getEntitySearchRange() == -1 ? creatureSpawner.getSpawnRange() : stackSettings.getEntitySearchRange();
        Block block = creatureSpawner.getBlock();
        EntityType entityType = creatureSpawner.getSpawnedType();

        Collection<Entity> nearbyEntities = this.entityCacheManager.getNearbyEntities(
                block.getLocation().clone().add(0.5, 0.5, 0.5),
                detectionRange,
                entity -> entity.getType() == entityType);

        if (Setting.SPAWNER_MAX_NEARBY_ENTITIES_INCLUDE_STACKS.getBoolean()) {
            return nearbyEntities.stream().mapToInt(x -> {
                StackedEntity stackedEntity = this.stackManager.getStackedEntity((LivingEntity) x);
                return stackedEntity == null ? 1 : stackedEntity.getStackSize();
            }).sum() < this.maxNearbyEntities;
        } else {
            return nearbyEntities.size() < this.maxNearbyEntities;
        }
    }

    @Override
    public boolean parseValues(String[] values) {
        if (values.length != 1)
            return false;

        try {
            this.maxNearbyEntities = Integer.parseInt(values[0]);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @Override
    protected List<String> getInfoMessageValues(LocaleManager localeManager) {
        return Collections.singletonList(String.valueOf(this.maxNearbyEntities));
    }

}
