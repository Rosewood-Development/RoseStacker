package dev.esophose.rosestacker.stack;

import org.bukkit.Location;

public interface Stack {

    void increaseStackSize(int amount);

    int getStackSize();

    void setStackSize(int size);

    Location getLocation();

    void updateDisplay();

}
