package dev.rosewood.rosestacker.event;

import dev.rosewood.rosestacker.stack.StackedItem;
import java.util.List;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when stacked items are to be cleared
 */
public class ItemStackClearEvent extends StackClearEvent<StackedItem> {

    private static final HandlerList HANDLERS = new HandlerList();

    public ItemStackClearEvent(@NotNull World world, @NotNull List<StackedItem> cleared) {
        super(world, cleared);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
