package dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags;

import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.ConditionTag;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.EntityUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class TotalDarknessConditionTag extends ConditionTag {

    public TotalDarknessConditionTag(String tag) {
        super(tag, true);
    }

    @Override
    public boolean check(StackedSpawner stackedSpawner, Block spawnBlock) {
        Material type = EntityUtils.getLazyBlockMaterial(spawnBlock.getLocation());
        if (StackerUtils.isOccluding(type))
            return false;

        return spawnBlock.getLightLevel() == 0;
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
