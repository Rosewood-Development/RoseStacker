package dev.rosewood.rosestacker.nms.object;

import java.util.List;
import org.bukkit.entity.EntityType;

/**
 * Used to bypass the costly BlockState snapshot system that Bukkit uses
 */
public interface SpawnerTileWrapper {

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
     * @param spawnRange
     */
    void setSpawnRange(int spawnRange);

    /**
     * @return the types of entities that will be spawned from this spawner
     */
    List<EntityType> getSpawnedTypes();

}
