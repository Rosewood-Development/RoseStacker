package dev.rosewood.rosestacker.stack.settings.spawner.tags;

import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTag;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.Collections;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

public class DarknessConditionTag extends ConditionTag {

    public DarknessConditionTag(String tag) {
        super(tag, true);
    }

    @Override
    public boolean check(CreatureSpawner creatureSpawner, SpawnerStackSettings stackSettings, Block spawnBlock) {
        if (!StackerUtils.isAir(spawnBlock.getType()))
            return false;

        if (creatureSpawner.getSpawnedType() == EntityType.BLAZE || creatureSpawner.getSpawnedType() == EntityType.SILVERFISH)
            return spawnBlock.getLightLevel() <= 11;
        return spawnBlock.getLightLevel() <= 7;
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
