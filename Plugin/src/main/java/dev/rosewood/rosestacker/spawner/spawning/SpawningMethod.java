package dev.rosewood.rosestacker.spawner.spawning;

import dev.rosewood.rosestacker.nms.object.StackedSpawnerTile;
import dev.rosewood.rosestacker.stack.StackedSpawner;

public interface SpawningMethod {

    /**
     * Spawns things out of the spawner, can be practically anything depending on the implementation
     *
     * @param stackedSpawner The StackedSpawner instance
     * @param spawnerTile The spawner tile
     */
    void spawn(StackedSpawner stackedSpawner, StackedSpawnerTile spawnerTile);

}
