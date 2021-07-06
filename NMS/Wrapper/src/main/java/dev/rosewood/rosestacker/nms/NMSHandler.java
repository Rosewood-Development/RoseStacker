package dev.rosewood.rosestacker.nms;

import dev.rosewood.rosestacker.nms.object.CompactNBT;
import dev.rosewood.rosestacker.nms.object.SpawnerTileWrapper;
import dev.rosewood.rosestacker.nms.object.WrappedNBT;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

public interface NMSHandler {

    /**
     * Serializes a LivingEntity to a base64 string
     *
     * @param livingEntity to serialize
     * @return base64 string of the entity
     */
    WrappedNBT<?> getEntityAsNBT(LivingEntity livingEntity);

    /**
     * Deserializes and optionally forcefully spawns the entity at the given location
     *
     * @param serialized entity
     * @param location to spawn the entity at
     * @param addToWorld whether or not to add the entity to the world
     * @param overwriteType entity type to use over the serialized type, nullable
     * @return the entity spawned from the NBT
     */
    LivingEntity createEntityFromNBT(WrappedNBT<?> serialized, Location location, boolean addToWorld, EntityType overwriteType);

    /**
     * Deserializes and creates a LivingEntity from compressed NBT data.
     * Should only be used for legacy purposes.
     *
     * @param serialized entity
     * @param location to spawn the entity at
     * @param overwriteType entity type to use over the serialized type, nullable
     * @return the entity spawned from the NBT
     */
    LivingEntity createEntityFromNBT(byte[] serialized, Location location, EntityType overwriteType);

    /**
     * Creates a LivingEntity instance where the actual entity has not been added to the world
     *
     * @param entityType The type of the entity to spawn
     * @param location The location the entity would have spawned at
     * @return The newly created LivingEntity instance
     */
    LivingEntity createNewEntityUnspawned(EntityType entityType, Location location);

    /**
     * Adds an unspawned entity to the world
     *
     * @param entity The entity to add
     * @param spawnReason The reason the entity is spawning
     */
    void spawnExistingEntity(LivingEntity entity, SpawnReason spawnReason);

    /**
     * Spawns a LivingEntity at the given location with a custom SpawnReason
     *
     * @param entityType The type of entity to spawn
     * @param location The location to spawn the entity at
     * @param spawnReason The reason for the entity spawning
     * @return The entity that was spawned
     */
    LivingEntity spawnEntityWithReason(EntityType entityType, Location location, SpawnReason spawnReason);

    /**
     * Updates the name and visibility of an Entity's nametag for a Player
     *
     * @param player The Player to send the packet to
     * @param entity The Entity to toggle
     * @param customName The name to display for the entity, nullable
     * @param customNameVisible true to make the nametag visible, otherwise false
     */
    void updateEntityNameTagForPlayer(Player player, Entity entity, String customName, boolean customNameVisible);

    /**
     * Updates the visibility of an Entity's nametag for a Player
     *
     * @param player The Player to send the packet to
     * @param entity The Entity to toggle
     * @param customNameVisible true to make the nametag visible, otherwise false
     */
    void updateEntityNameTagVisibilityForPlayer(Player player, Entity entity, boolean customNameVisible);

    /**
     * Unignites a creeper
     *
     * @param creeper The creeper to unignite
     */
    void unigniteCreeper(Creeper creeper);

    /**
     * Removes entity goals and movement
     *
     * @param livingEntity The entity to remove goals and movement from
     */
    void removeEntityGoals(LivingEntity livingEntity);

    /**
     * Sets a String value into an ItemStack's NBT
     *
     * @param itemStack The ItemStack
     * @param key The key to store the value at
     * @param value The value to store
     * @return A copy of the ItemStack with the applied NBT value
     */
    ItemStack setItemStackNBT(ItemStack itemStack, String key, String value);

    /**
     * Sets an int value into an ItemStack's NBT
     *
     * @param itemStack The ItemStack
     * @param key The key to store the value at
     * @param value The value to store
     * @return A copy of the ItemStack with the applied NBT value
     */
    ItemStack setItemStackNBT(ItemStack itemStack, String key, int value);

    /**
     * Gets a String value from an ItemStack's NBT
     *
     * @param itemStack The ItemStack
     * @param key The key the value is stored at
     * @return The value stored on the ItemStack, or an empty String if none found
     */
    String getItemStackNBTString(ItemStack itemStack, String key);

    /**
     * Gets an int value from an ItemStack's NBT
     *
     * @param itemStack The ItemStack
     * @param key The key the value is stored at
     * @return The value stored on the ItemStack, or 0 if none found
     */
    int getItemStackNBTInt(ItemStack itemStack, String key);

    /**
     * Gets an object that allows manipulating a CreatureSpawner without having to use Bukkit's BlockState system.
     * This is preferrable because of how slow generating a BlockState snapshot is.
     *
     * @param spawner The spawner to get the tile wrapper for
     * @return A SpawnerTileWrapper for a CreatureSpawner
     */
    SpawnerTileWrapper getSpawnerTile(CreatureSpawner spawner);

    /**
     * Get a list of all entities in this World
     *
     * @param world The world to get the entities from
     * @return A List of all Entities currently residing in the given world
     */
    default List<Entity> getEntities(World world) {
        return world.getEntities();
    }

    /**
     * Get a list of all entities in this Chunk
     *
     * @param chunk The chunk to get the entities from
     * @return A List of all Entities currently residing in the given chunk
     */
    default List<Entity> getEntities(Chunk chunk) {
        return Arrays.asList(chunk.getEntities());
    }

    /**
     * Sets the LivingEntity's lastHurtByPlayer value to the given Player
     *
     * @param livingEntity The LivingEntity
     * @param player The Player
     */
    void setLastHurtBy(LivingEntity livingEntity, Player player);

    /**
     * Creates a new CompactNBT instance for storing large amounts of entities of the same type in a small data footprint
     *
     * @param livingEntity The base entity
     * @return a new CompactNBT instance
     */
    CompactNBT createCompactNBT(LivingEntity livingEntity);

    /**
     * Creates a new CompactNBT instance from existing serialized data
     *
     * @param data The CompactNBT data, should be acquired from {@link CompactNBT#serialize()}
     * @return a new CompactNBT instance
     */
    CompactNBT loadCompactNBT(byte[] data);

}
