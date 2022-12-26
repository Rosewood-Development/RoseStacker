package dev.rosewood.rosestacker.nms.spawner;

import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.ApiStatus;

public interface StackedSpawnerTile {

    /**
     * @return the type of entity that this spawner will spawn next
     */
    @ApiStatus.Experimental
    SpawnerType getSpawnerType();

    /**
     * Sets the type of entity that this spawner will spawn
     *
     * @param spawnerType The type of entity to spawn
     */
    @ApiStatus.Experimental
    void setSpawnerType(SpawnerType spawnerType);

    /**
     * Gets the EntityType this spawner will spawn.
     * Does not support empty spawners or multiple entity types.
     * Will return {@link EntityType#PIG} if the spawner is empty.
     * This method is obsolete, consider using {@link #setSpawnerType(SpawnerType)} instead.
     *
     * @return the type of entity that this spawner will spawn next
     */
    @ApiStatus.Obsolete
    default EntityType getSpawnedType() {
        return this.getSpawnerType().get().orElse(EntityType.PIG);
    }

    /**
     * Sets the type of entity that this spawner will spawn.
     * This method is obsolete, consider using {@link #setSpawnerType(SpawnerType)} instead.
     *
     * @param entityType The type of entity to spawn
     */
    @ApiStatus.Obsolete
    default void setSpawnedType(EntityType entityType) {
        this.setSpawnerType(SpawnerType.of(entityType));
    }

    /**
     * @return the delay of the spawner
     */
    int getDelay();

    /**
     * Sets the delay until the next spawn attempt
     *
     * @param delay The delay before the next spawn attempt
     */
    void setDelay(int delay);

    /**
     * @return the minimum spawn delay amount (in ticks)
     */
    int getMinSpawnDelay();

    /**
     * Sets the minimum new delay value between spawns
     *
     * @param delay The new min delay
     */
    void setMinSpawnDelay(int delay);

    /**
     * @return the maximum spawn delay amount (in ticks)
     */
    int getMaxSpawnDelay();

    /**
     * Sets the maximum new delay value between spawns
     *
     * @param delay The new max delay
     */
    void setMaxSpawnDelay(int delay);

    /**
     * @return how many mobs will attempt to be spawned
     */
    int getSpawnCount();

    /**
     * Sets the max number of mobs that can be spawned
     *
     * @param spawnCount The new spawn count
     */
    void setSpawnCount(int spawnCount);

    /**
     * @return the max allows nearby entities
     */
    int getMaxNearbyEntities();

    /**
     * Sets the max nearby entities allowed near the spawner before spawns will be prevented
     *
     * @param maxNearbyEntities The new max nearby entities
     */
    void setMaxNearbyEntities(int maxNearbyEntities);

    /**
     * @return the maximum distance a player can be in order for this spawner to be active
     */
    int getRequiredPlayerRange();

    /**
     * Sets the radius around which the spawner will attempt to spawn mobs in
     *
     * @param requiredPlayerRange The new required player range
     */
    void setRequiredPlayerRange(int requiredPlayerRange);

    /**
     * @return the radius around which the spawner will attempt to spawn mobs in
     */
    int getSpawnRange();

    /**
     * Set the radius around which the spawner will attempt to spawn mobs in
     *
     * @param spawnRange The maximum distance from the spawner mobs can spawn
     */
    void setSpawnRange(int spawnRange);

    /**
     * @return The PersistentDataContainer attached to this spawner tile
     */
    PersistentDataContainer getPersistentDataContainer();

}
