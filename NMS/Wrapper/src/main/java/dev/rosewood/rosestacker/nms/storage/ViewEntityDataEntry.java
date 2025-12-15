package dev.rosewood.rosestacker.nms.storage;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class ViewEntityDataEntry implements EntityDataEntry {

    private final LivingEntity entity;

    public ViewEntityDataEntry(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public LivingEntity createEntity(Location location, boolean addToWorld, EntityType entityType) {
        return this.entity;
    }

}
