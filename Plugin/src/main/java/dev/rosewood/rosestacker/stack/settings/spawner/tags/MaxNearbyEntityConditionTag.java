package dev.rosewood.rosestacker.stack.settings.spawner.tags;

import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTag;
import java.util.Collections;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

public class MaxNearbyEntityConditionTag extends ConditionTag {

    private int maxNearbyEntities;

    public MaxNearbyEntityConditionTag(String tag) {
        super(tag, false);
    }

    @Override
    public boolean check(CreatureSpawner creatureSpawner, Block spawnBlock) {
        int spawnRange = creatureSpawner.getSpawnRange();
        Block block = creatureSpawner.getBlock();
        EntityType entityType = creatureSpawner.getSpawnedType();
        return block.getWorld().getNearbyEntities(
                block.getLocation().clone().add(0.5, 0.5, 0.5),
                spawnRange, spawnRange, spawnRange,
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
