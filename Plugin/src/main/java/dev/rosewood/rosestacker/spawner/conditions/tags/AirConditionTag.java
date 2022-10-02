package dev.rosewood.rosestacker.spawner.conditions.tags;

import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.spawner.conditions.ConditionTag;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.EntityUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.List;
import org.bukkit.block.Block;

public class AirConditionTag extends ConditionTag {

    public AirConditionTag(String tag) {
        super(tag, true);
    }

    @Override
    public boolean check(StackedSpawner stackedSpawner, Block spawnBlock) {
        boolean isAir = true;
        for (Block block : EntityUtils.getIntersectingBlocks(stackedSpawner.getSpawnerTile().getSpawnedType(), spawnBlock.getLocation().clone().add(0.5, 0, 0.5)))
            isAir &= StackerUtils.isAir(block.getType()) || !StackerUtils.isOccluding(block.getType());
        return isAir;
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
