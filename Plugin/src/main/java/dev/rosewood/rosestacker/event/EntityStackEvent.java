package dev.rosewood.rosestacker.event;

import dev.rosewood.rosestacker.stack.StackedEntity;
import java.util.List;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the size of a StackedEntity increases
 */
public class EntityStackEvent extends StackEvent<StackedEntity> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<StackedEntity> targets;

    /**
     * @param targets The entities getting stacked into the other entity
     * @param result The entity being stacked into
     */
    public EntityStackEvent(@NotNull List<StackedEntity> targets, @NotNull StackedEntity result) {
        super(result);

        this.targets = targets;
    }

    /**
     * @return the StackedEntities that are getting stacked
     */
    @NotNull
    public List<StackedEntity> getTargets() {
        return this.targets;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
