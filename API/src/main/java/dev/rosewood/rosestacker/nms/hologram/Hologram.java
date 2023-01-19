package dev.rosewood.rosestacker.nms.hologram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class Hologram {

    private static final double LINE_OFFSET = 0.3;

    protected final List<HologramLine> hologramLines;
    protected final Map<Player, Boolean> watchers;
    protected final Location location;
    private final Supplier<Integer> entityIdSupplier;

    public Hologram(List<String> text, Location location, Supplier<Integer> entityIdSupplier) {
        this.location = location.clone();
        this.watchers = Collections.synchronizedMap(new WeakHashMap<>());
        this.entityIdSupplier = entityIdSupplier;
        this.hologramLines = new ArrayList<>();
        this.createLines(text);
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
            this.update(List.of(player), true);
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
    public List<String> getText() {
        return this.hologramLines.stream().map(HologramLine::getText).toList();
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
            this.update(List.of(player), true);
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
     * @throws IllegalArgumentException if the text is a different length than the existing text
     */
    public void setText(List<String> text) {
        if (text.size() != this.hologramLines.size()) {
            this.createLines(text);
            return;
        }

        for (int i = 0; i < text.size(); i++)
            this.hologramLines.get(i).setText(text.get(i));

        this.update(this.watchers.keySet(), false);
    }

    private void createLines(List<String> text) {
        this.watchers.keySet().forEach(this::delete);
        this.hologramLines.clear();
        for (int i = 0; i < text.size(); i++) {
            double offset = (text.size() - i - 1) * LINE_OFFSET;
            Location lineLocation = this.location.clone().add(0, offset, 0);
            this.hologramLines.add(new HologramLine(this.entityIdSupplier.get(), lineLocation, text.get(i)));
        }
        this.watchers.keySet().forEach(this::create);
        this.update(this.watchers.keySet(), true);
    }

    /**
     * Creates a new hologram entity for the given player
     *
     * @param player The player to spawn the hologram for
     */
    protected abstract void create(Player player);

    /**
     * Sends the metadata packet for this hologram to the specified players if the line needs to be updated
     *
     * @param players The players to send the packet to
     * @param force true to force the packet to be sent, false otherwise
     */
    protected abstract void update(Collection<Player> players, boolean force);

    /**
     * Deletes the hologram entity for the given player
     *
     * @param player The player to delete the hologram for
     */
    protected abstract void delete(Player player);

}
