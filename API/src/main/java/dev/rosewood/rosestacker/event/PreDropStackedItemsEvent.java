package dev.rosewood.rosestacker.event;

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PreDropStackedItemsEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Map<ItemStack, Integer> items;
    private final Location location;
    private boolean cancelled;

    /**
     * @param items The items being dropped
     * @param location The location where the items are being dropped
     */
    public PreDropStackedItemsEvent(@NotNull Map<ItemStack, Integer> items, @NotNull Location location) {
        this.items = items;
        this.location = location;
    }

    /**
     * Gets a Map of the items being dropped. The key is the ItemStack to drop, and the value is the amount to drop
     *
     * @return a mutable Map of the items being dropped
     */
    @NotNull
    public Map<ItemStack, Integer> getItems() {
        return this.items;
    }

    /**
     * @return the location where the items are being dropped
     */
    @NotNull
    public Location getLocation() {
        return this.location;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
