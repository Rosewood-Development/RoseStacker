package dev.rosewood.rosestacker.nms.hologram;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class Hologram {

    protected final int entityId;
    protected final Map<Player, Boolean> watchers;
    protected final Location location;
    protected String text;

    public Hologram(int entityId, Location location, String text) {
        this.entityId = entityId;
        this.location = location;
        this.text = text;
        this.watchers = Collections.synchronizedMap(new WeakHashMap<>());
    }

    /**
     * Adds a player to the watchers of this hologram
     *
     * @param player The player to add
     * @param visible true to make the hologram visible, false otherwise
     */
    public void addWatcher(Player player, boolean visible) {
        if (!this.watchers.containsKey(player)) {
            this.watchers.put(player, visible);
            this.create(player);
            this.update(player);
        }
    }

    /**
     * Adds a player to the watchers of this hologram
     *
     * @param player The player to add
     */
    public void addWatcher(Player player) {
        this.addWatcher(player, true);
    }

    /**
     * Removes a player from the watchers of this hologram
     *
     * @param player The player to remove
     */
    public void removeWatcher(Player player) {
        if (this.watchers.containsKey(player)) {
            this.watchers.remove(player);
            this.delete(player);
        }
    }

    /**
     * @return a set of all players watching this hologram
     */
    public Set<Player> getWatchers() {
        return this.watchers.keySet();
    }

    /**
     * @return the location of this hologram
     */
    public Location getLocation() {
        return this.location;
    }

    /**
     * @return the display location of this hologram (at the height where the text actually renders)
     */
    public Location getDisplayLocation() {
        return this.location.clone().add(0, 1, 0);
    }

    /**
     * @return the text of this hologram
     */
    public String getText() {
        return this.text;
    }

    /**
     * Sets the visibility of the hologram for a player
     *
     * @param player The player to set the visibility for
     * @param visible true to make the hologram visible, false otherwise
     */
    public void setVisibility(Player player, boolean visible) {
        Boolean alreadyVisible = this.watchers.get(player);
        if (alreadyVisible == null)
            return;

        if (alreadyVisible ^ visible) {
            this.watchers.put(player, visible);
            this.update(player);
        }
    }

    /**
     * Deletes the hologram for all watchers
     */
    public void delete() {
        this.watchers.keySet().forEach(this::delete);
        this.watchers.clear();
    }

    /**
     * Sets the hologram text and updates it to all watchers
     *
     * @param text The text to set
     */
    public void setText(String text) {
        this.text = text;
        this.watchers.keySet().forEach(this::update);
    }

    /**
     * Creates a new hologram entity for the given player
     *
     * @param player The player to spawn the hologram for
     */
    protected abstract void create(Player player);

    /**
     * Sends the metadata packet for this hologram to the specified player
     *
     * @param player The player to send the packet to
     */
    protected abstract void update(Player player);

    /**
     * Deletes the hologram entity for the given player
     *
     * @param player The player to delete the hologram for
     */
    protected abstract void delete(Player player);

}
