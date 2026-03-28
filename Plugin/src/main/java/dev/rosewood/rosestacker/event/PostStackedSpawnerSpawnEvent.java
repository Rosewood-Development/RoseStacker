package dev.rosewood.rosestacker.event;

import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import java.util.List;
import java.util.Set;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Only called when spawn-into-nearby-stacks is true. If false, a normal SpawnerSpawnEvent gets called for each mob instead.
 */
public class PostStackedSpawnerSpawnEvent extends StackEvent<StackedSpawner> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Set<StackedEntity> modifiedStacks;
    private final Set<StackedEntity> spawnedStacks;
    private final int spawnAmount;

    /**
     * @param stackedSpawner The stacked spawner that is spawning
     * @param modifiedStacks The stacked entities that were modified
     * @param spawnedStacks The stacked entities that were spawned
     * @param spawnAmount The number of spawns that will be attempted
     */
    public PostStackedSpawnerSpawnEvent(@NotNull StackedSpawner stackedSpawner, @NotNull Set<StackedEntity> modifiedStacks, @NotNull Set<StackedEntity> spawnedStacks, int spawnAmount) {
        super(stackedSpawner);
        this.modifiedStacks = modifiedStacks;
        this.spawnedStacks = spawnedStacks;
        this.spawnAmount = spawnAmount;
    }

    /**
     * @return the stacked entities that were modified
     */
    public Set<StackedEntity> getModifiedStacks() {
        return this.modifiedStacks;
    }

    /**
     * @return the stacked entities that were spawned
     */
    public Set<StackedEntity> getSpawnedStacks() {
        return this.spawnedStacks;
    }

    /**
     * @return the total number of mobs that were spawned
     */
    public int getSpawnAmount() {
        return this.spawnAmount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
