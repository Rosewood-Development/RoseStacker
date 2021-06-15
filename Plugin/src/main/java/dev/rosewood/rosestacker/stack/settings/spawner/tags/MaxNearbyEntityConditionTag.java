package dev.rosewood.rosestacker.stack.settings.spawner.tags;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTag;
import java.util.Collections;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

public class MaxNearbyEntityConditionTag extends ConditionTag {

    private int maxNearbyEntities;
    private final EntityCacheManager entityCacheManager;

    public MaxNearbyEntityConditionTag(String tag) {
        super(tag, false);

        this.entityCacheManager = RoseStacker.getInstance().getManager(EntityCacheManager.class);
    }

    @Override
    public boolean check(CreatureSpawner creatureSpawner, SpawnerStackSettings stackSettings, Block spawnBlock) {
        int detectionRange = stackSettings.getEntitySearchRange() == -1 ? creatureSpawner.getSpawnRange() : stackSettings.getEntitySearchRange();
        Block block = creatureSpawner.getBlock();
        EntityType entityType = creatureSpawner.getSpawnedType();
        return this.entityCacheManager.getNearbyEntities(
                block.getLocation().clone().add(0.5, 0.5, 0.5),
                detectionRange,
                entity -> entity.getType() == entityType).size() < this.maxNearbyEntities;
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
