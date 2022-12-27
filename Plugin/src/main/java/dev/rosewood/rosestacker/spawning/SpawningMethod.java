package dev.rosewood.rosestacker.spawning;

import dev.rosewood.rosestacker.stack.StackedSpawner;

public interface SpawningMethod {

    /**
     * Spawns things out of the spawner, can be practically anything depending on the implementation
     *
     * @param stackedSpawner The StackedSpawner instance
     * @param onlyCheckConditions Whether to only check conditions and not actually spawn anything
     */
    void spawn(StackedSpawner stackedSpawner, boolean onlyCheckConditions);

}
