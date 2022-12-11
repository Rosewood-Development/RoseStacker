package dev.rosewood.rosestacker.command.type;

import dev.rosewood.rosestacker.nms.spawner.SpawnerType;

public class StackedSpawnerType {

    private final SpawnerType spawnerType;

    public StackedSpawnerType(SpawnerType spawnerType) {
        this.spawnerType = spawnerType;
    }

    public SpawnerType get() {
        return this.spawnerType;
    }

}
