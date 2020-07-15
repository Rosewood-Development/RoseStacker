package dev.rosewood.rosestacker.stack.settings.spawner.tags;

import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTag;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;

public class FluidConditionTag extends ConditionTag {

    private Material fluidType;

    public FluidConditionTag(String tag) {
        super(tag, true);
    }

    @Override
    public boolean check(CreatureSpawner creatureSpawner, Block spawnBlock) {
        return spawnBlock.getType() == this.fluidType && spawnBlock.getRelative(BlockFace.UP).getType() == this.fluidType;
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
