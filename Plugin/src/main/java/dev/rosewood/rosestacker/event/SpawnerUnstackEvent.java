package dev.rosewood.rosestacker.event;

import dev.rosewood.rosestacker.stack.StackedSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when the size of a StackedSpawner decreases
 */
public class SpawnerUnstackEvent extends UnstackEvent<StackedSpawner> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private int decreaseAmount;

    /**
     * @param player The player modifying the stack
     * @param target The spawner being unstacked from
     * @param decreaseAmount The amount the spawner stack is being decreased by
     */
    public SpawnerUnstackEvent(@Nullable Player player, @NotNull StackedSpawner target, int decreaseAmount) {
        super(target);

        this.player = player;
        this.decreaseAmount = decreaseAmount;
    }

    /**
     * @return the player modifying the stack, or null if due to an explosion or other reasons
     */
    @Nullable
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return the amount the stack is being decreased by
     */
    public int getDecreaseAmount() {
        return this.decreaseAmount;
    }

    /**
     * Sets the amount the stack will be decreased by
     *
     * @param decreaseAmount the amount to decrease the stack by
     */
    public void setDecreaseAmount(int decreaseAmount) {
        if (decreaseAmount < 1)
            throw new IllegalArgumentException("Decrease amount must be at least 1");
        if (decreaseAmount > this.stack.getStackSize())
            throw new IllegalArgumentException("Decrease amount must not be larger than the total stack size");
        this.decreaseAmount = decreaseAmount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
