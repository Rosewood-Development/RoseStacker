package dev.esophose.rosestacker.stack;

import org.bukkit.Location;

public interface Stack {

    int getStackSize();

    void setStackSize(int size);

    Location getLocation();

    void updateDisplay();

}
