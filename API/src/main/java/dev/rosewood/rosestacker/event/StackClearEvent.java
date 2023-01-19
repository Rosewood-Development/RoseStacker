package dev.rosewood.rosestacker.event;

import dev.rosewood.rosestacker.stack.Stack;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Base event for when the stack clearall is run by either a command or other means
 *
 * @param <T> The stack type
 */
public abstract class StackClearEvent<T extends Stack<?>> extends Event implements Cancellable {

    private boolean cancelled;
    private final World world;
    private final List<T> cleared;

    public StackClearEvent(@NotNull World world, @NotNull List<T> cleared) {
        super(!Bukkit.isPrimaryThread());

        this.world = world;
        this.cleared = cleared;
        this.cancelled = false;
    }

    /**
     * @return the world the stacks are being removed from
     */
    @NotNull
    public World getWorld() {
        return this.world;
    }

    /**
     * @return a modifyable list of stacks to be cleared
     */
    @NotNull
    public List<T> getStacks() {
        return this.cleared;
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
