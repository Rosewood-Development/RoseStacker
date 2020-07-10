package dev.rosewood.rosestacker.event;

import dev.rosewood.rosestacker.stack.StackedEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the size of a StackedEntity increases
 */
public class EntityStackEvent extends StackEvent<StackedEntity> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StackedEntity target;

    /**
     * @param target The entity getting stacked into the other entity
     * @param result The entity being stacked into
     */
    public EntityStackEvent(@NotNull StackedEntity target, @NotNull StackedEntity result) {
        super(result);

        this.target = target;
    }

    /**
     * @return the StackedEntity that is getting stacked
     */
    @NotNull
    public StackedEntity getTarget() {
        return this.target;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
