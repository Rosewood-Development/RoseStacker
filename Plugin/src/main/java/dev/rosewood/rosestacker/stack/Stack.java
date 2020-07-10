package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosestacker.stack.settings.StackSettings;
import org.bukkit.Location;

public abstract class Stack<T extends StackSettings> {

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

    public abstract T getStackSettings();

}
