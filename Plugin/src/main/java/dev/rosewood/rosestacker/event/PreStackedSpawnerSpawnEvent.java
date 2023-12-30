package dev.rosewood.rosestacker.event;

import dev.rosewood.rosestacker.stack.StackedSpawner;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PreStackedSpawnerSpawnEvent extends StackEvent<StackedSpawner> implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private int spawnAmount;
    private boolean cancelled;

    /**
     * @param stackedSpawner The stacked spawner that is spawning
     * @param spawnAmount The number of spawns that will be attempted
     */
    public PreStackedSpawnerSpawnEvent(@NotNull StackedSpawner stackedSpawner, int spawnAmount) {
        super(stackedSpawner);
        this.spawnAmount = spawnAmount;
    }

    /**
     * @return the number of spawns that will be attempted
     */
    public int getSpawnAmount() {
        return this.spawnAmount;
    }

    /**
     * Sets the number of spawns that will be attempted
     *
     * @param spawnAmount the number of spawns that will be attempted
     */
    public void setSpawnAmount(int spawnAmount) {
        this.spawnAmount = spawnAmount;
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
