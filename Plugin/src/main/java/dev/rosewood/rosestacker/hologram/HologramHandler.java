package dev.rosewood.rosestacker.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface HologramHandler {

    /**
     * Creates or updates a hologram at the given location
     *
     * @param location The location of the hologram
     * @param text The text for the hologram
     */
    void createOrUpdateHologram(Location location, String text);

    /**
     * Deletes a hologram at a given location if one exists
     *
     * @param location The location of the hologram
     */
    void deleteHologram(Location location);

    /**
     * Deletes all holograms
     */
    void deleteAllHolograms();

    /**
     * @return true if this hologram handler plugin is enabled, false otherwise
     */
    boolean isEnabled();

}
