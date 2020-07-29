package dev.rosewood.rosestacker.stack.settings.spawner.tags;

import dev.rosewood.rosestacker.manager.LocaleManager;
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
    public boolean check(CreatureSpawner creatureSpawner, Block spawnBlock) {
        Block above = spawnBlock.getRelative(BlockFace.UP);
        boolean isAir = spawnBlock.getType().isAir() && above.getType().isAir();
        EntityType entityType = creatureSpawner.getSpawnedType();
        // TODO: Check to make sure all entities will fit here. Probably going to have to do AABB checks somehow.
        if (entityType == EntityType.ENDERMAN)
            isAir &= above.getRelative(BlockFace.UP).getType().isAir();
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
