package dev.rosewood.rosestacker.command.type;

import org.bukkit.entity.EntityType;

public class StackedSpawnerType {

    private final EntityType entityType;

    public StackedSpawnerType(EntityType entityType) {
        this.entityType = entityType;
    }

    public EntityType get() {
        return this.entityType;
    }

}
