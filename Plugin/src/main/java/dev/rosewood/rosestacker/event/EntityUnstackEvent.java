package dev.rosewood.rosestacker.event;

import dev.rosewood.rosestacker.stack.StackedEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the size of a StackedEntity decreases
 */
public class EntityUnstackEvent extends UnstackEvent<StackedEntity> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StackedEntity result;

    /**
     * @param original The entity getting unstacked from
     * @param result The entity being created from the unstack
     */
    public EntityUnstackEvent(@NotNull StackedEntity original, @NotNull StackedEntity result) {
        super(original);

        this.result = result;
    }

    /**
     * @return the new StackedEntity that is being unstacked
     */
    @NotNull
    public StackedEntity getResult() {
        return this.result;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
