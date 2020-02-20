package dev.esophose.rosestacker.nms;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public interface NMSHandler {

    /**
     * Serializes a LivingEntity to a base64 string
     *
     * @param livingEntity to serialize
     * @return base64 string of the entity
     */
    String getEntityAsNBTString(LivingEntity livingEntity);

    /**
     * Deserializes and spawns the entity at the given location
     *
     * @param serialized entity
     * @param location to spawn the entity at
     * @return the entity spawned from the NBT string
     */
    LivingEntity spawnEntityFromNBTString(String serialized, Location location);

    /**
     * Gets a LivingEntity from an NBT string without spawning the entity into the world
     *
     * @param entityType The type of the entity to spawn
     * @param location The location that the entity would normally be spawned in
     * @param serialized The serialized entity NBT data
     * @return A LivingEntity instance, not in the world
     */
    LivingEntity getNBTStringAsEntity(EntityType entityType, Location location, String serialized);

    /**
     * Creates a LivingEntity instance where the actual entity has not been added to the world
     *
     * @param entityType The type of the entity to spawn
     * @param location The location the entity would have spawned at
     * @return The newly created LivingEntity instance
     */
    LivingEntity createEntityUnspawned(EntityType entityType, Location location);

}
