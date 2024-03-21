package dev.rosewood.rosestacker.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import dev.rosewood.rosestacker.stack.StackedItem;
import org.jetbrains.annotations.NotNull;

public class ItemPickupEvent extends EntityEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StackedItem stackedItem;
    private boolean cancelled;

    /**
     * @param entity The entity picking up item
     * @param stackedItem The StackedItem being picking up from stacked dropped items
     */
    public ItemPickupEvent(@NotNull final LivingEntity entity, @NotNull final StackedItem stackedItem) {
        super(entity);
        this.stackedItem = stackedItem;
    }

    /**
     * Gets the entity picking up items.
     *
     * @return the entity try to pick up item
     */
    @NotNull
    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    /**
     * Gets the StackedItem being picking up.
     *
     * @return the StackedItem being picking up
     */
    @NotNull
    public StackedItem getStackedItem() {
        return this.stackedItem;
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
