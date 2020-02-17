package dev.esophose.sparkstacker.stack;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public interface StackingLogic {

    /**
     * @return a map of all loaded stacked entities
     */
    Map<UUID, StackedEntity> getStackedEntities();

    /**
     * @return a map of all loaded stacked items
     */
    Map<UUID, StackedItem> getStackedItems();

    /**
     * @return a map of all loaded stacked blocks
     */
    Map<Block, StackedBlock> getStackedBlocks();

    /**
     * @return a map of all loaded stacked spawners
     */
    Map<Block, StackedSpawner> getStackedSpawners();

    /**
     * Gets a StackedEntity for a given LivingEntity
     *
     * @param livingEntity the target entity
     * @return a StackedEntity, or null if not found
     */
    StackedEntity getStackedEntity(LivingEntity livingEntity);

    /**
     * Gets a StackedItem for a given Item
     *
     * @param item the target item
     * @return a StackedItem, or null if not found
     */
    StackedItem getStackedItem(Item item);

    /**
     * Gets a StackedBlock for a given Block
     *
     * @param block the target block
     * @return a StackedBlock, or null if not found
     */
    StackedBlock getStackedBlock(Block block);

    /**
     * Gets a StackedSpawner for a given Block
     *
     * @param block the target block
     * @return a StackedBlock, or null if not found
     */
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
     * @param newKey the LivingEntity at the new head of the stack
     */
    void updateStackedEntityKey(LivingEntity oldKey, LivingEntity newKey);

    /**
     * Splits the top entity off the StackedEntity
     *
     * @param stackedEntity the StackedEntity to split
     * @return the new StackedEntity that was created
     */
    StackedEntity splitEntityStack(StackedEntity stackedEntity);

    /**
     * Splits a StackedItem into another stack
     *
     * @param stackedItem the StackedItem to split
     * @param newSize the amount to split off
     * @return the new StackedItem that was created
     */
    StackedItem splitItemStack(StackedItem stackedItem, int newSize);

    /**
     * Creates a StackedEntity from a LivingEntity
     *
     * @param livingEntity the LivingEntity to create a stack from
     * @param tryStack true to try to stack the entity instantly, otherwise false
     * @return the newly created stack, or null if one wasn't created
     */
    StackedEntity createEntityStack(LivingEntity livingEntity, boolean tryStack);

    /**
     * Creates a StackedItem from an Item
     *
     * @param item the Item to create a stack from
     * @param tryStack true to try to stack the item instantly, otherwise false
     * @return the newly created stack, or null if one wasn't created
     */
    StackedItem createItemStack(Item item, boolean tryStack);

    /**
     * Creates a StackedBlock from a Block
     *
     * @param block the Block to create a stack from
     * @param amount the size of the stack being created
     * @return the newly created stack, or null if one wasn't created
     */
    StackedBlock createBlockStack(Block block, int amount);

    /**
     * Creates a StackedSpawner from a Block
     *
     * @param block the Block to create a stack from
     * @param amount the size of the stack being created
     * @return the newly created stack, or null if one wasn't created
     */
    StackedSpawner createSpawnerStack(Block block, int amount);

    /**
     * Pre-stacks a collection of ItemStacks and spawns StackedEntities at the given location
     *
     * @param items the items to stack and spawn
     * @param location the location to spawn at
     */
    void preStackItems(Collection<ItemStack> items, Location location);

    /**
     * Loads all stacks from a chunk
     *
     * @param chunk the target chunk
     */
    void loadChunk(Chunk chunk);

    /**
     * Saves all stacks in a chunk and unloads them
     *
     * @param chunk the target chunk
     */
    void unloadChunk(Chunk chunk);

}
