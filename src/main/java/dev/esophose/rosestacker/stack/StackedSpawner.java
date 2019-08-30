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

    public CreatureSpawner getSpawner() {
        return this.spawner;
    }

    @Override
    public void increaseStackSize(int amount) {
        this.size += amount;
        this.updateDisplay();
    }

    @Override
    public int getStackSize() {
        return this.size;
    }

    @Override
    public void setStackSize(int size) {
        this.size = size;
        this.updateDisplay();
    }

    @Override
    public Location getLocation() {
        return this.spawner.getLocation();
    }

    @Override
    public void updateDisplay() {

    }

}
