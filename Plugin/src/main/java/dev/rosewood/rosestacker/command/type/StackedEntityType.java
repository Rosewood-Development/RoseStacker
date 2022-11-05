package dev.rosewood.rosestacker.command.type;

import org.bukkit.entity.EntityType;

public class StackedEntityType {

    private final EntityType entityType;

    public StackedEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public EntityType get() {
        return this.entityType;
    }

}
