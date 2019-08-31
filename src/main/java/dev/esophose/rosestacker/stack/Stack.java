package dev.esophose.rosestacker.stack;

import org.bukkit.Location;

public interface Stack {

    int getStackSize();

    Location getLocation();

    void updateDisplay();

}
