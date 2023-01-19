package dev.rosewood.rosestacker.event;

import dev.rosewood.rosestacker.stack.StackedEntity;
import java.util.List;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when stacked entities are to be cleared
 */
public class EntityStackClearEvent extends StackClearEvent<StackedEntity> {

    private static final HandlerList HANDLERS = new HandlerList();

    public EntityStackClearEvent(@NotNull World world, @NotNull List<StackedEntity> cleared) {
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
