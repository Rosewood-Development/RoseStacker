package dev.rosewood.rosestacker.event;

import dev.rosewood.rosestacker.stack.Stack;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StackGUIOpenEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Stack<?> stack;
    private boolean cancelled;

    /**
     * @param player The player opening the stack GUI
     * @param stack The Stack having its GUI opened
     */
    public StackGUIOpenEvent(@NotNull Player player, @NotNull Stack<?> stack) {
        this.player = player;
        this.stack = stack;
    }

    /**
     * @return the player modifying opening the stack GUI
     */
    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return the Stack having its GUI opened
     */
    @NotNull
    public Stack<?> getStack() {
        return this.stack;
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
