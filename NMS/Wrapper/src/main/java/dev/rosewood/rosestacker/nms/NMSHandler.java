package dev.rosewood.rosestacker.nms;

import dev.rosewood.rosestacker.nms.hologram.Hologram;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataEntry;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorage;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorageType;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Turtle;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

/**
 * Allows performing certain actions that are only possible through the use of NMS.
 * For internal use only. Subject to change extremely frequently.
 */
public interface NMSHandler {

    List<String> REMOVABLE_NBT_KEYS = List.of(
            "UUID", "Pos", "Rotation", "WorldUUIDMost", "WorldUUIDLeast",
            "Motion", "OnGround", "FallDistance", "Leash", "Spigot.ticksLived",
            "Paper.OriginWorld", "Paper.Origin", "Patrolling", "PatrolTarget",
            "RaidId", "Wave"
    );

    List<String> UNSAFE_NBT_KEYS = List.of(
            "ArmorItems", "HandItems", "Items", "ChestedHorse", "Saddle",
            "DecorItem", "Inventory", "carriedBlockState", "DeathTime", "Health"
    );

    /**
     * Serializes a LivingEntity to a base64 string
     *
     * @param livingEntity to serialize
     * @return base64 string of the entity
     * @deprecated To be changed to transformEntityType(LivingEntity, EntityType)
     */
    @Deprecated(forRemoval = true)
    StackedEntityDataEntry<?> getEntityAsNBT(LivingEntity livingEntity);

    /**
     * Deserializes and optionally forcefully spawns the entity at the given location
     *
     * @param serialized entity
     * @param location to spawn the entity at
     * @param addToWorld whether or not to add the entity to the world
     * @param entityType entity type to create and apply the serialized nbt over
     * @return the entity spawned from the NBT
     */
    @Deprecated(forRemoval = true)
    LivingEntity createEntityFromNBT(StackedEntityDataEntry<?> serialized, Location location, boolean addToWorld, EntityType entityType);

    /**
     * Creates a LivingEntity instance where the actual entity has not been added to the world.
     * To be used in conjunction with {@link #spawnExistingEntity(LivingEntity, SpawnReason, boolean)}
     *
     * @param entityType The type of the entity to spawn
     * @param location The location the entity would have spawned at
     * @param spawnReason The reason the entity would have been spawned
     * @return The newly created LivingEntity instance
     */
    LivingEntity createNewEntityUnspawned(EntityType entityType, Location location, SpawnReason spawnReason);

    /**
     * Adds an unspawned entity to the world.
     * To be used in conjunction with {@link #createNewEntityUnspawned(EntityType, Location, SpawnReason)}
     *
     * @param entity The entity to add
     * @param spawnReason The reason the entity is spawning
     * @param bypassSpawnEvent Should an EntitySpawnEvent be called for this entity?
     */
    void spawnExistingEntity(LivingEntity entity, SpawnReason spawnReason, boolean bypassSpawnEvent);

    /**
     * Spawns an entity with a certain SpawnReason
     *
     * @param entityType The type of entity to spawn
     * @param spawnReason The reason the entity is spawning
     */
    default LivingEntity spawnEntityWithReason(EntityType entityType, Location location, SpawnReason spawnReason, boolean bypassSpawnEvent) {
        LivingEntity entity = this.createNewEntityUnspawned(entityType, location, spawnReason);
        this.spawnExistingEntity(entity, spawnReason, bypassSpawnEvent);
        return entity;
    }

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
     * Used to check if a turtle is pregnant (has an egg to lay)
     *
     * @param turtle the turtle
     * @return true if the turtle is trying to lay an egg, false otherwise
     */
    boolean isTurtlePregnant(Turtle turtle);

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
     * Sets the LivingEntity's lastHurtByPlayer value to the given Player
     *
     * @param livingEntity The LivingEntity
     * @param player The Player
     */
    void setLastHurtBy(LivingEntity livingEntity, Player player);

    /**
     * Checks if a LivingEntity can see a specific Location point
     *
     * @param entity1 The LivingEntity
     * @param location The Location point
     * @return true if the LivingEntity can see the Location point, false otherwise
     */
    boolean hasLineOfSight(LivingEntity entity1, Location location);

    /**
     * Checks if a LivingEntity can see a specific Entity
     *
     * @param entity1 The LivingEntity
     * @param entity2 The Entity
     * @return true if the LivingEntity can see the Entity, false otherwise
     */
    default boolean hasLineOfSight(LivingEntity entity1, Entity entity2) {
        Location location;
        if (entity2 instanceof LivingEntity) {
            location = ((LivingEntity) entity2).getEyeLocation();
        } else {
            location = entity2.getLocation().add(0, entity2.getHeight() * 0.85, 0);
        }
        return this.hasLineOfSight(entity1, location);
    }

    /**
     * Checks if an entity is an actively participating in a raid
     *
     * @param entity The entity to check
     * @return true if the entity is an active raider, false otherwise
     */
    boolean isActiveRaider(LivingEntity entity);

    /**
     * Creates a new StackedEntityDataStorage instance for storing large amounts of entities of the same type in a small data footprint
     *
     * @param livingEntity The base entity
     * @param storageType The type of storage to create
     * @return a new StackedEntityDataStorage instance
     */
    StackedEntityDataStorage createEntityDataStorage(LivingEntity livingEntity, StackedEntityDataStorageType storageType);

    /**
     * Creates a new StackedEntityDataStorage instance from existing serialized data
     *
     * @param livingEntity The base entity
     * @param data The StackedEntityDataStorage data, should be acquired from {@link StackedEntityDataStorage#serialize()}
     * @param storageType The type of storage to deserialize
     * @return a new StackedEntityDataStorage instance
     */
    StackedEntityDataStorage deserializeEntityDataStorage(LivingEntity livingEntity, byte[] data, StackedEntityDataStorageType storageType);

    /**
     * Injects the custom stacked spawner logic into the tile entity of the given spawner
     *
     * @param stackedSpawner The StackedSpawner instance to inject the custom stacked spawner logic into
     * @return A StackedSpawnerTile instance that was injected or null if the object given was not a valid StackedSpawner
     */
    StackedSpawnerTile injectStackedSpawnerTile(Object stackedSpawner);

    /**
     * Creates a hologram at the given location with the given text
     *
     * @param location The location to create the hologram at
     * @param text The text to display on the hologram
     * @return The hologram created
     */
    Hologram createHologram(Location location, List<String> text);

    /**
     * @return true if empty spawners are supported, false otherwise
     */
    default boolean supportsEmptySpawners() {
        return false;
    }

    /**
     * 1.19 uses a new RandomSource system which causes a server crash when accessed async.
     * Try to hijack this RandomSource and inject our own into the world which allows "thread-safe" access.
     * This is likely a very bad idea, sorry to whoever is reading this.
     *
     * @param world The World to hijack the RandomSource of
     */
    default void hijackRandomSource(World world) {

    }

    default void setPaperFromMobSpawner(Entity entity) {

    }

}
