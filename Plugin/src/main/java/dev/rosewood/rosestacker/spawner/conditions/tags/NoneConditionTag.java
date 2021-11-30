package dev.rosewood.rosestacker.spawner.conditions.tags;

import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.spawner.conditions.ConditionTag;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import java.util.List;
import org.bukkit.block.Block;

public class NoneConditionTag extends ConditionTag {

    public NoneConditionTag(String tag) {
        super(tag, true);
    }

    @Override
    public boolean check(StackedSpawner stackedSpawner, Block spawnBlock) {
        throw new IllegalStateException("None condition tag should not be used");
    }

    @Override
    public boolean parseValues(String[] values) {
        throw new IllegalStateException("None condition tag should not be used");
    }

    @Override
    protected List<String> getInfoMessageValues(LocaleManager localeManager) {
        throw new IllegalStateException("None condition tag should not be used");
    }

}
