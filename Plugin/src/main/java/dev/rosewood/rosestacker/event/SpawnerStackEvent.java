package dev.rosewood.rosestacker.event;

import dev.rosewood.rosestacker.stack.StackedSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the size of a StackedSpawner increases
 */
public class SpawnerStackEvent extends StackEvent<StackedSpawner> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private int increaseAmount;

    /**
     * @param player The player modifying the stack
     * @param target The spawner being stacked into
     * @param increaseAmount The amount the spawner stack is being increased by
     */
    public SpawnerStackEvent(@NotNull Player player, @NotNull StackedSpawner target, int increaseAmount) {
        super(target);

        this.player = player;
        this.increaseAmount = increaseAmount;
    }

    /**
     * @return the Player modifying the stack
     */
    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return the amount the stack is being increased by
     */
    public int getIncreaseAmount() {
        return this.increaseAmount;
    }

    /**
     * Sets the amount the stack will be increased by
     *
     * @param increaseAmount the amount to increase the stack by
     */
    public void setIncreaseAmount(int increaseAmount) {
        if (increaseAmount < 1)
            throw new IllegalArgumentException("Increase amount must be at least 1");
        this.increaseAmount = increaseAmount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
