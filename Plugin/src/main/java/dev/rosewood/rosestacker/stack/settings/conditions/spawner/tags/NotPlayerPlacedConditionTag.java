package dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags;

import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.ConditionTag;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import java.util.List;
import org.bukkit.block.Block;

public class NotPlayerPlacedConditionTag extends ConditionTag {

    public static final NotPlayerPlacedConditionTag INSTANCE = new NotPlayerPlacedConditionTag("not-player-placed");

    public NotPlayerPlacedConditionTag(String tag) {
        super(tag, false);
    }

    @Override
    public boolean check(StackedSpawner stackedSpawner, Block spawnBlock) {
        return false;
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
