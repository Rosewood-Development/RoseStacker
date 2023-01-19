package dev.rosewood.rosestacker.nms.storage;

import dev.rosewood.rosestacker.nms.NMSAdapter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public interface EntityDataEntry {

    /**
     * Creates a new entity at the specified location from this entry
     *
     * @param location The location to spawn the entity at
     * @param addToWorld Should the entity be added to the world?
     * @param entityType The type of entity to spawn // TODO: Why isn't this nullable? We should be able to infer the type from the entry
     * @return The newly created entity
     */
    LivingEntity createEntity(Location location, boolean addToWorld, EntityType entityType);

    /**
     * Creates a new EntityDataEntry from a LivingEntity
     *
     * @param livingEntity The LivingEntity
     * @return The EntityDataEntry
     */
    static EntityDataEntry of(LivingEntity livingEntity) {
        return NMSAdapter.getHandler().createEntityDataEntry(livingEntity);
    }

}
