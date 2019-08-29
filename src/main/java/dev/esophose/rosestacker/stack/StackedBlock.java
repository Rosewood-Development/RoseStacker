package dev.esophose.rosestacker.stack;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class StackedBlock implements Stack {

    private int size;
    private Block block;

    public StackedBlock(int size, Block block) {
        this.size = size;
        this.block = block;

        this.updateDisplay();
    }

    @Override
    public int getStackSize() {
        return this.size;
    }

    @Override
    public void setStackSize(int size) {

    }

    @Override
    public Location getLocation() {
        return this.block.getLocation();
    }

    @Override
    public void updateDisplay() {

    }

}
