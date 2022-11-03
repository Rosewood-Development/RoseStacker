package dev.rosewood.rosestacker.spawner.conditions.tags;

import com.google.common.base.Stopwatch;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.spawner.conditions.ConditionTag;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.EntityUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class AirConditionTag extends ConditionTag {

    public AirConditionTag(String tag) {
        super(tag, true);
    }

    @Override
    public boolean check(StackedSpawner stackedSpawner, Block spawnBlock) {
        boolean isAir = true;
        for (Material type : EntityUtils.getIntersectingBlocks(stackedSpawner.getSpawnerTile().getSpawnedType(), spawnBlock.getLocation().clone().add(0.5, 0, 0.5)).values())
            isAir &= StackerUtils.isAir(type) || !StackerUtils.isOccluding(type);
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
