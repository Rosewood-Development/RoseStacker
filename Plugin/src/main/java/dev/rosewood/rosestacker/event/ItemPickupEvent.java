package dev.rosewood.rosestacker.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemPickupEvent extends EntityEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ItemStack item;
    private boolean cancelled;

    /**
     * @param entity The entity picking up item
     * @param item   The item being picking up from stacked dropped items
     */
    public ItemPickupEvent(@NotNull final LivingEntity entity, @NotNull final ItemStack item) {
        super(entity);
        this.item = item;
    }

    /**
     * Gets the entity picking up items.
     *
     * @return the entity
     */
    @NotNull
    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    /**
     * Gets the items being picking up.
     *
     * @return the itemStack being picking up
     */
    @NotNull
    public ItemStack getItem() {
        return this.item;
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
