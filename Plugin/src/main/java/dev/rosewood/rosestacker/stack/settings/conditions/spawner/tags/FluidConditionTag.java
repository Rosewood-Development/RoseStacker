package dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags;

import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.ConditionTag;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.EntityUtils;
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
        for (Material type : EntityUtils.getIntersectingBlocks(stackedSpawner.getSpawnerTile().getSpawnerType().getOrThrow(), spawnBlock.getLocation().clone().add(0.5, 0, 0.5)).values())
            isFluid &= type == this.fluidType;
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
        return List.of(this.fluidType.name());
    }

}
