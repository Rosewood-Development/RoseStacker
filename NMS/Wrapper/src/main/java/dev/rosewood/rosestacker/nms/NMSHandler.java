package dev.rosewood.rosestacker.nms;

import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface NMSHandler {

    /**
     * Serializes a LivingEntity to a base64 string
     *
     * @param livingEntity to serialize
     * @param includeAttributes true to include the entitiy attribute tags, otherwise false
     * @return base64 string of the entity
     */
    byte[] getEntityAsNBT(LivingEntity livingEntity, boolean includeAttributes);

    /**
     * Deserializes and forcefully spawns the entity at the given location
     *
     * @param serialized entity
     * @param location to spawn the entity at
     * @return the entity spawned from the NBT
     */
    LivingEntity spawnEntityFromNBT(byte[] serialized, Location location);

    /**
     * Gets a LivingEntity from an NBT string without spawning the entity into the world
     *
     * @param entityType The type of the entity to spawn
     * @param location The location that the entity would normally be spawned in
     * @param serialized The serialized entity NBT data
     * @return A LivingEntity instance, not in the world
     */
    LivingEntity getNBTAsEntity(EntityType entityType, Location location, byte[] serialized);

    /**
     * Creates a LivingEntity instance where the actual entity has not been added to the world
     *
     * @param entityType The type of the entity to spawn
     * @param location The location the entity would have spawned at
     * @return The newly created LivingEntity instance
     */
    LivingEntity createEntityUnspawned(EntityType entityType, Location location);

    /**
     * Toggles the visibility of an Entity's nametag for a Player
     *
     * @param player The Player to send the packet to
     * @param entity The Entity to toggle
     * @param visible true to make the nametag visible, otherwise false
     */
    void toggleEntityNameTagForPlayer(Player player, Entity entity, boolean visible);

    /**
     * Unignites a creeper
     *
     * @param creeper The creeper to unignite
     */
    void unigniteCreeper(Creeper creeper);

}
