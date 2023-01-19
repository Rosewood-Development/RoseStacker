package dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags;

import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.stack.StackedSpawnerImpl;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.ConditionTag;
import java.util.List;
import org.bukkit.block.Block;

public class NoSkylightAccessConditionTag extends ConditionTag {

    public NoSkylightAccessConditionTag(String tag) {
        super(tag, true);
    }

    @Override
    public boolean check(StackedSpawnerImpl stackedSpawner, Block spawnBlock) {
        return spawnBlock.getLightFromSky() <= 7;
    }

    @Override
    public boolean parseValues(String[] values) {
        return values.length == 0;
    }

    @Override
    protected List<String> getInfoMessageValues(LocaleManager localeManager) {
        return List.of();
    }

}
