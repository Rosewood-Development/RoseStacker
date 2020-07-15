package dev.rosewood.rosestacker.event;

import dev.rosewood.rosestacker.stack.StackedBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the size of a StackedBlock increases
 */
public class BlockStackEvent extends StackEvent<StackedBlock> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private int increaseAmount;

    /**
     * @param player The player modifying the stack
     * @param target The block being stacked into
     * @param increaseAmount The amount the block stack is being increased by
     */
    public BlockStackEvent(@NotNull Player player, @NotNull StackedBlock target, int increaseAmount) {
        super(target);

        this.player = player;
        this.increaseAmount = increaseAmount;
    }

    /**
     * @return the player modifying the stack
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
