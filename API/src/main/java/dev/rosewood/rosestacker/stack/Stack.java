package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosestacker.stack.settings.StackSettings;
import org.bukkit.Location;
import org.bukkit.World;

public interface Stack<T extends StackSettings> {

    /**
     * @return the size of this Stack
     */
    int getStackSize();

    /**
     * @return the Location of this Stack
     */
    Location getLocation();

    /**
     * @return the StackSettings for this Stack
     */
    T getStackSettings();

    /**
     * Gets the World the stack belongs in
     *
     * @return this Stack's World
     * @throws IllegalStateException if the World is null
     */
    default World getWorld() {
        Location location = this.getLocation();
        World world = location.getWorld();
        if (world == null)
            throw new IllegalStateException("Stack world is null");
        return world;
    }

}
