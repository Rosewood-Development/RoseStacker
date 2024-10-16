package dev.rosewood.rosestacker.nms.storage;

import dev.rosewood.rosestacker.nms.NMSAdapter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public interface EntityDataEntry {

    /**
     * Creates an entity at the specified location from this entry
     *
     * @param location The location to spawn the entity at
     * @param addToWorld Should the entity be added to the world?
     * @param entityType The type of entity to spawn // TODO: Why isn't this nullable? We should be able to infer the type from the entry
     * @return The created entity
     */
    LivingEntity createEntity(Location location, boolean addToWorld, EntityType entityType);

    /**
     * Creates a new EntityDataEntry from a LivingEntity's NBT
     *
     * @param livingEntity The LivingEntity
     * @return The EntityDataEntry
     */
    static EntityDataEntry createFromEntityNBT(LivingEntity livingEntity) {
        return NMSAdapter.getHandler().createEntityDataEntry(livingEntity);
    }

    /**
     * Creates a new EntityDataEntry from a LivingEntity.
     * Calling {@link #createEntity(Location, boolean, EntityType)} will always result in the original entity.
     *
     * @param livingEntity The LivingEntity
     * @return The EntityDataEntry
     */
    static EntityDataEntry createFromEntity(LivingEntity livingEntity) {
        return new ViewEntityDataEntry(livingEntity);
    }

}
