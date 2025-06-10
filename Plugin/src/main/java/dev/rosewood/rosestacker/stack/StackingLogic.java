package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosestacker.manager.StackManager;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Common logic shared between {@link StackManager} and {@link StackingThread}
 */
public interface StackingLogic {

    /**
     * @return a map of all loaded stacked entities
     */
    @NotNull
    Map<UUID, StackedEntity> getStackedEntities();

    /**
     * @return a map of all loaded stacked items
     */
    @NotNull
    Map<UUID, StackedItem> getStackedItems();

    /**
     * @return a map of all loaded stacked blocks
     */
    @NotNull
    Map<Block, StackedBlock> getStackedBlocks();

    /**
     * @return a map of all loaded stacked spawners
     */
    @NotNull
    Map<Block, StackedSpawner> getStackedSpawners();

    /**
     * Gets a StackedEntity for a given LivingEntity
     *
     * @param livingEntity the target entity
     * @return a StackedEntity, or null if not found
     */
    @Nullable
    StackedEntity getStackedEntity(LivingEntity livingEntity);

    /**
     * Gets a StackedItem for a given Item
     *
     * @param item the target item
     * @return a StackedItem, or null if not found
     */
    @Nullable
    StackedItem getStackedItem(Item item);

    /**
     * Gets a StackedBlock for a given Block
     *
     * @param block the target block
     * @return a StackedBlock, or null if not found
     */
    @Nullable
    StackedBlock getStackedBlock(Block block);

    /**
     * Gets a StackedSpawner for a given Block
     *
     * @param block the target block
     * @return a StackedBlock, or null if not found
     */
    @Nullable
    StackedSpawner getStackedSpawner(Block block);

    /**
     * Checks if a given LivingEntity is part of a StackedEntity
     *
     * @param livingEntity the entity to check
     * @return true if the entity is part of a StackedEntity, otherwise false
     */
    boolean isEntityStacked(LivingEntity livingEntity);

    /**
     * Checks if a given Item is part of a StackedItem
     *
     * @param item the item to check
     * @return true if the item is part of a StackedItem, otherwise false
     */
    boolean isItemStacked(Item item);

    /**
     * Checks if a given Block is part of a StackedBlock
     *
     * @param block the block to check
     * @return true if the block is part of a StackedBlock, otherwise false
     */
    boolean isBlockStacked(Block block);

    /**
     * Checks if a given Block is part of a StackedSpawner
     *
     * @param block the block to check
     * @return true if the block is part of a StackedSpawner, otherwise false
     */
    boolean isSpawnerStacked(Block block);

    /**
     * Removes a StackedEntity
     *
     * @param stackedEntity the StackedEntity to remove
     */
    void removeEntityStack(StackedEntity stackedEntity);

    /**
     * Removes a StackedItem
     *
     * @param stackedItem the StackedItem to remove
     */
    void removeItemStack(StackedItem stackedItem);

    /**
     * Removes a StackedBlock
     *
     * @param stackedBlock the StackedBlock to remove
     */
    void removeBlockStack(StackedBlock stackedBlock);

    /**
     * Removes a StackedSpawner
     *
     * @param stackedSpawner the StackedSpawner to remove
     */
    void removeSpawnerStack(StackedSpawner stackedSpawner);

    /**
     * Removes all StackedEntities and the LivingEntities tied to them
     *
     * @return the number of entities removed
     */
    int removeAllEntityStacks();

    /**
     * Removes all StackedItems and the Items tied to them
     *
     * @return the number of items removed
     */
    int removeAllItemStacks();

    /**
     * Updates a StackedEntity key in the Map
     * This is used for when the head of a StackedEntity becomes a different LivingEntity
     *
     * @param oldKey the LivingEntity at the old head of the stack
     * @param stackedEntity the StackedEntity being updated
     */
    void updateStackedEntityKey(LivingEntity oldKey, StackedEntity stackedEntity);

    /**
     * Splits the top entity off the StackedEntity
     *
     * @param stackedEntity the StackedEntity to split
     * @return the new StackedEntity that was created, or null if one wasn't created
     */
    @Nullable
    StackedEntity splitEntityStack(StackedEntity stackedEntity);

    /**
     * Splits a StackedItem into another stack
     *
     * @param stackedItem the StackedItem to split
     * @param newSize the amount to split off
     * @return the new StackedItem that was created, or null if one wasn't created
     */
    @Nullable
    StackedItem splitItemStack(StackedItem stackedItem, int newSize);

    /**
     * Creates a StackedEntity from a LivingEntity
     *
     * @param livingEntity the LivingEntity to create a stack from
     * @param tryStack true to try to stack the entity instantly, otherwise false
     * @return the newly created stack, or null if one wasn't created
     */
    @Nullable
    StackedEntity createEntityStack(LivingEntity livingEntity, boolean tryStack);

    /**
     * Creates a StackedItem from an Item
     *
     * @param item the Item to create a stack from
     * @param tryStack true to try to stack the item instantly, otherwise false
     * @return the newly created stack, or null if one wasn't created
     */
    @Nullable
    StackedItem createItemStack(Item item, boolean tryStack);

    /**
     * Creates a StackedBlock from a Block
     *
     * @param block the Block to create a stack from
     * @param amount the size of the stack being created
     * @return the newly created stack, or null if one wasn't created
     */
    @Nullable
    StackedBlock createBlockStack(Block block, int amount);

    /**
     * Creates a StackedSpawner from a Block
     *
     * @param block the Block to create a stack from
     * @param amount the size of the stack being created
     * @param placedByPlayer true for if the spawner was placed by a player, false otherwise
     * @return the newly created stack, or null if one wasn't created
     */
    @Nullable
    StackedSpawner createSpawnerStack(Block block, int amount, boolean placedByPlayer);

    /**
     * Adds a pre-existing StackedEntity
     *
     * @param stackedEntity the StackedEntity to add
     */
    void addEntityStack(StackedEntity stackedEntity);

    /**
     * Adds a pre-existing StackedItem
     *
     * @param stackedItem the StackedItem to add
     */
    void addItemStack(StackedItem stackedItem);

    /**
     * Pre-stacks a number of entities of a given type and a custom spawn reason and spawns StackedEntities at the given location
     *
     * @param entityType the type of entity to spawn
     * @param amount the amount of entities to spawn
     * @param location the location to spawn at
     * @param spawnReason The reason the entities are being spawned
     */
    void preStackEntities(EntityType entityType, int amount, Location location, SpawnReason spawnReason);

    /**
     * Pre-stacks a number of entities of a given type and spawns StackedEntities at the given location
     *
     * @param entityType the type of entity to spawn
     * @param amount the amount of entities to spawn
     * @param location the location to spawn at
     */
    void preStackEntities(EntityType entityType, int amount, Location location);

    /**
     * Pre-stacks a collection of ItemStacks and spawns StackedItems at the given location
     *
     * @param items the items to stack and spawn
     * @param location the location to spawn at
     */
    default void preStackItems(Collection<ItemStack> items, Location location) {
        this.preStackItems(items, location, true);
    }

    /**
     * Pre-stacks a collection of ItemStacks and spawns StackedItems at the given location
     *
     * @param items the items to stack and spawn
     * @param location the location to spawn at
     * @param dropNaturally true to drop naturally, false otherwise
     */
    void preStackItems(Collection<ItemStack> items, Location location, boolean dropNaturally);

    /**
     * Drops a StackedItem at the given Location with a specified ItemStack and amount
     *
     * @param itemStack The ItemStack to drop
     * @param amount The amount of the item to drop
     * @param location The Location to drop the item at
     * @param dropNaturally true to drop naturally, false otherwise
     * @return The newly created StackedItem, may be null if item stacking is disabled
     */
    @Nullable
    StackedItem dropItemStack(ItemStack itemStack, int amount, Location location, boolean dropNaturally);

    /**
     * Loads stacks for blocks and spawners for a chunk
     *
     * @param chunk The chunk to load data in
     */
    void loadChunkBlocks(Chunk chunk);

    /**
     * Loads stacks for entities and items for a chunk
     *
     * @param entities The entities that are to be loaded
     */
    void loadChunkEntities(List<Entity> entities);

    /**
     * Saves stacks for blocks and spawners for a chunk
     *
     * @param chunk The chunk to save data to
     * @param clearStored If the data should be cleared from cache
     */
    void saveChunkBlocks(Chunk chunk, boolean clearStored);

    /**
     * Saves stacks for entities and items for a chunk
     *
     * @param entities The entities that are to be saved
     * @param clearStored If the data should be cleared from cache
     */
    void saveChunkEntities(List<Entity> entities, boolean clearStored);

    /**
     * Saves stacks for entities and items for a chunk
     *
     * @param stacks The stacks that are to be saved
     * @param clearStored If the data should be cleared from cache
     */
    <T extends Stack<?>> void saveChunkEntityStacks(List<T> stacks, boolean clearStored);

    /**
     * Saves all stack data in loaded chunks
     *
     * @param clearStored If the data should be cleared from cache
     */
    void saveAllData(boolean clearStored);

    /**
     * Tries to stack a StackedEntity with all other StackedEntities
     *
     * @param stackedEntity the StackedEntity to try to stack
     */
    void tryStackEntity(StackedEntity stackedEntity);

    /**
     * Tries to stack a StackedItem with all other StackedItems
     *
     * @param stackedItem the StackedItem to try to stack
     */
    void tryStackItem(StackedItem stackedItem);

    /**
     * Tries to unstack the given entity if it meets unstacking conditions
     *
     * @param stackedEntity The stacked entity to try unstacking
     */
    void tryUnstackEntity(StackedEntity stackedEntity);

}
