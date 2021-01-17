package dev.rosewood.rosestacker.stack.settings.spawner.tags;

import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTag;
import java.util.Collections;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

public class AirConditionTag extends ConditionTag {

    public AirConditionTag(String tag) {
        super(tag, true);
    }

    @Override
    public boolean check(CreatureSpawner creatureSpawner, SpawnerStackSettings stackSettings, Block spawnBlock) {
        Block above = spawnBlock.getRelative(BlockFace.UP);
        boolean spawnIsAir = spawnBlock.getType().isAir() || !spawnBlock.getType().isOccluding();
        boolean aboveIsAir = above.getType().isAir() || !above.getType().isOccluding();
        boolean isAir = spawnIsAir && aboveIsAir;
        EntityType entityType = creatureSpawner.getSpawnedType();
        // TODO: Check to make sure all entities will fit here. Probably going to have to do AABB checks somehow.
        if (entityType == EntityType.ENDERMAN) {
            Block third = above.getRelative(BlockFace.UP);
            isAir &= third.getType().isAir() || !third.getType().isOccluding();
        }
        return isAir;
    }

    @Override
    public boolean parseValues(String[] values) {
        return values.length == 0;
    }

    @Override
    protected List<String> getInfoMessageValues(LocaleManager localeManager) {
        return Collections.emptyList();
    }

}
