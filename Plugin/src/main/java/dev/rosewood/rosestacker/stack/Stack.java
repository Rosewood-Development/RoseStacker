package dev.rosewood.rosestacker.stack;

import org.bukkit.Location;

public abstract class Stack {

    private int id;

    public Stack(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public abstract int getStackSize();

    public abstract Location getLocation();

    public abstract void updateDisplay();

}
