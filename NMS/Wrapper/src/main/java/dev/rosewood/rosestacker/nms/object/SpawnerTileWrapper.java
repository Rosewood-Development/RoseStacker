package dev.rosewood.rosestacker.nms.object;

import java.util.List;
import org.bukkit.entity.EntityType;

/**
 * Used to bypass the costly BlockState snapshot system that Bukkit uses
 */
public interface SpawnerTileWrapper {

    int getDelay();

    void setDelay(int delay);

    int getMinSpawnDelay();

    void setMinSpawnDelay(int delay);

    int getMaxSpawnDelay();

    void setMaxSpawnDelay(int delay);

    int getSpawnCount();

    void setSpawnCount(int spawnCount);

    int getMaxNearbyEntities();

    void setMaxNearbyEntities(int maxNearbyEntities);

    int getRequiredPlayerRange();

    void setRequiredPlayerRange(int requiredPlayerRange);

    int getSpawnRange();

    void setSpawnRange(int spawnRange);

    List<EntityType> getSpawnedTypes();

}
