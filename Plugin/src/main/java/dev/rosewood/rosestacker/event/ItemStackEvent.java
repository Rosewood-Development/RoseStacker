package dev.rosewood.rosestacker.event;

import dev.rosewood.rosestacker.stack.StackedItem;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the size of a StackedItem increases
 */
public class ItemStackEvent extends StackEvent<StackedItem> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StackedItem target;

    /**
     * @param target The item getting stacked into the other item
     * @param result The item being stacked into
     */
    public ItemStackEvent(@NotNull StackedItem target, @NotNull StackedItem result) {
        super(result);

        this.target = target;
    }

    /**
     * @return the StackedItem that is getting stacked
     */
    @NotNull
    public StackedItem getTarget() {
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
