package dev.rosewood.rosestacker.spawner.conditions.tags;

import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.spawner.conditions.ConditionTag;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.EntityUtils;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class FluidConditionTag extends ConditionTag {

    private Material fluidType;

    public FluidConditionTag(String tag) {
        super(tag, true);
    }

    @Override
    public boolean check(StackedSpawner stackedSpawner, Block spawnBlock) {
        boolean isFluid = true;
        for (Block block : EntityUtils.getIntersectingBlocks(stackedSpawner.getSpawnerTile().getSpawnedType(), spawnBlock.getLocation().clone().add(0.5, 0, 0.5)))
            isFluid &= block.getType() == this.fluidType;
        return isFluid;
    }

    @Override
    public boolean parseValues(String[] values) {
        if (values.length != 1)
            return false;

        Material fluidType = Material.matchMaterial(values[0]);
        if (fluidType == Material.WATER || fluidType == Material.LAVA) {
            this.fluidType = fluidType;
            return true;
        }

        return false;
    }

    @Override
    protected List<String> getInfoMessageValues(LocaleManager localeManager) {
        return Collections.singletonList(this.fluidType.name());
    }

}
