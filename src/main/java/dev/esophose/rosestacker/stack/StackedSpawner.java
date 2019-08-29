package dev.esophose.rosestacker.stack;

import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;

public class StackedSpawner implements Stack {

    private int size;
    private CreatureSpawner spawner;

    public StackedSpawner(int size, CreatureSpawner spawner) {
        this.size = size;
        this.spawner = spawner;

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
        return this.spawner.getLocation();
    }

    @Override
    public void updateDisplay() {

    }

}
