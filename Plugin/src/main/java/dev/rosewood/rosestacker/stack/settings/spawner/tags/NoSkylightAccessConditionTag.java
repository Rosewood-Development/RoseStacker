package dev.rosewood.rosestacker.stack.settings.spawner.tags;

import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTag;
import java.util.Collections;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;

public class NoSkylightAccessConditionTag extends ConditionTag {

    public NoSkylightAccessConditionTag(String tag) {
        super(tag, true);
    }

    @Override
    public boolean check(CreatureSpawner creatureSpawner, Block spawnBlock) {
        return spawnBlock.getLightFromSky() <= 7;
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
