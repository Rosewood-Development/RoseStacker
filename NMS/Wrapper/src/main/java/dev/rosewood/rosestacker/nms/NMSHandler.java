package dev.rosewood.rosestacker.nms;

import dev.rosewood.rosestacker.nms.hologram.Hologram;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.nms.storage.EntityDataEntry;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorage;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorageType;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows performing certain actions that are only possible through the use of NMS.
 * For internal use only. Subject to change extremely frequently.
 */
@ApiStatus.Internal
public interface NMSHandler {

    List<String> REMOVABLE_NBT_KEYS = List.of(
            "UUID", "Pos", "Rotation", "WorldUUIDMost", "WorldUUIDLeast",
            "Motion", "OnGround", "FallDistance", "Leash", "Spigot.ticksLived",
            "Paper.OriginWorld", "Paper.Origin", "Patrolling", "PatrolTarget",
            "RaidId", "Wave", "AngryAt", "AngerTime", "Pose", "LastPoseTick",

            "uuid", "pos", "rotation", "world_uuid_most", "world_uuid_least",
            "motion", "on_ground", "fall_distance", "leash", "patrolling",
            "patrol_target", "raid_id", "wave", "angry_at", "anger_time", "pose",
            "last_pose_tick"
    );

    List<String> UNSAFE_NBT_KEYS = List.of(
            "ArmorItems", "HandItems", "Items", "ChestedHorse", "Saddle",
            "DecorItem", "Inventory", "carriedBlockState", "DeathTime", "Health",
            "Attributes", "ActiveEffects", "ArmorDropChances", "HandDropChances", "Brain",
            "LeftHanded", "Team",

            "armor_items", "hand_items", "items", "chested_horse", "saddle",
            "decor_item", "inventory", "carried_block_state", "death_time", "health",
            "attributes", "active_effects", "armor_drop_chances", "hand_drop_chances", "brain",
            "left_handed", "team"
    );

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
     * Gets a String value from an ItemStack's NBT compound
     *
     * @param itemStack The ItemStack
     * @param compoundKey The key the compound is stored at on the item
     * @param valueKey The key the value is stored at in the compound
     * @return The value stored on the ItemStack, or an empty String if none found
     */
    String getItemStackNBTStringFromCompound(ItemStack itemStack, String compoundKey, String valueKey);

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
     * Creates a new EntityDataEntry from a LivingEntity
     *
     * @param livingEntity The LivingEntity
     * @return The EntityDataEntry
     */
    EntityDataEntry createEntityDataEntry(LivingEntity livingEntity);

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

    /**
     * Sets the entity custom name bypassing the 256 character limit set by Bukkit.
     * Why is this character limit still a thing? Names have supported it for ages.
     *
     * @param entity The entity to change the custom name of
     * @param customName The custom name to set
     */
    void setCustomNameUncapped(Entity entity, String customName);

    int getItemDespawnRate(Item item);

    List<ItemStack> getBoxContents(ItemStack item);

}
