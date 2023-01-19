package dev.rosewood.rosestacker.api;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.event.EntityStackMultipleDeathEvent;
import dev.rosewood.rosestacker.manager.StackManagerLogic;
import dev.rosewood.rosestacker.manager.StackSettingManagerLogic;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedItem;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.StackingThread;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.ItemStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The API for the RoseStacker plugin
 */
public final class RoseStackerAPI {

    private static RoseStackerAPI instance;

    private final RoseStacker roseStacker;
    private StackManagerLogic stackManager;
    private StackSettingManagerLogic stackSettingManager;

    private RoseStackerAPI(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    private static void initialize(RoseStacker plugin) {
        if (instance != null)
            throw new IllegalStateException("RoseStackerAPI has already been initialized");
        instance = new RoseStackerAPI(plugin);
    }

    /**
     * @return the instance of the RoseStackerAPI
     */
    @NotNull
    public static RoseStackerAPI getInstance() {
        if (instance == null)
            throw new IllegalStateException("The API is not loaded. Is the plugin enabled?");

        if (instance.stackManager == null)
            instance.stackManager = instance.roseStacker.getManagerInternal(StackManagerLogic.class);

        if (instance.stackManager == null)
            instance.stackSettingManager = instance.roseStacker.getManagerInternal(StackSettingManagerLogic.class);

        return instance;
    }

    /**
     * @return the instance of RoseStacker
     */
    @NotNull
    @ApiStatus.Internal
    public RoseStacker getPluginInstance() {
        return this.roseStacker;
    }

    /**
     * @return the currently installed version of the plugin
     */
    @NotNull
    public String getVersion() {
        return this.roseStacker.getDescription().getVersion();
    }

    /**
     * @return true if {@link EntityStackMultipleDeathEvent} is called
     */
    public boolean isEntityStackMultipleDeathEventCalled() {
        return this.stackManager.isEntityStackMultipleDeathEventCalled();
    }

    //region Stack Manager

    /**
     * Gets a StackingThread for a World
     *
     * @param world the World
     * @return a StackingThread for the World, otherwise null if one doesn't exist
     */
    @Nullable
    public StackingThread getStackingThread(@NotNull World world) {
        Objects.requireNonNull(world);

        return this.stackManager.getStackingThread(world);
    }

    /**
     * @return a Map of key -> World UUID, value -> StackingThread of all StackingThreads
     */
    @NotNull
    public Map<UUID, StackingThread> getStackingThreads() {
        return Collections.unmodifiableMap(this.stackManager.getStackingThreads());
    }

    /**
     * @return an unmodifiable map of all loaded stacked entities
     */
    @NotNull
    public Map<UUID, StackedEntity> getStackedEntities() {
        return Collections.unmodifiableMap(this.stackManager.getStackedEntities());
    }

    /**
     * @return an unmodifiable map of all loaded stacked items
     */
    @NotNull
    public Map<UUID, StackedItem> getStackedItems() {
        return Collections.unmodifiableMap(this.stackManager.getStackedItems());
    }

    /**
     * @return an unmodifiable map of all loaded stacked blocks
     */
    @NotNull
    public Map<Block, StackedBlock> getStackedBlocks() {
        return Collections.unmodifiableMap(this.stackManager.getStackedBlocks());
    }

    /**
     * @return an unmodifiable map of all loaded stacked spawners
     */
    @NotNull
    public Map<Block, StackedSpawner> getStackedSpawners() {
        return Collections.unmodifiableMap(this.stackManager.getStackedSpawners());
    }

    /**
     * Gets a StackedEntity for a given LivingEntity
     *
     * @param livingEntity the target entity
     * @return a StackedEntity, or null if not found
     */
    @Nullable
    public StackedEntity getStackedEntity(@NotNull LivingEntity livingEntity) {
        Objects.requireNonNull(livingEntity);

        return this.stackManager.getStackedEntity(livingEntity);
    }

    /**
     * Gets a StackedItem for a given Item
     *
     * @param item the target item
     * @return a StackedItem, or null if not found
     */
    @Nullable
    public StackedItem getStackedItem(@NotNull Item item) {
        Objects.requireNonNull(item);

        return this.stackManager.getStackedItem(item);
    }

    /**
     * Gets a StackedBlock for a given Block
     *
     * @param block the target block
     * @return a StackedBlock, or null if not found
     */
    @Nullable
    public StackedBlock getStackedBlock(@NotNull Block block) {
        Objects.requireNonNull(block);

        return this.stackManager.getStackedBlock(block);
    }

    /**
     * Gets a StackedSpawner for a given Block
     *
     * @param block the target block
     * @return a StackedBlock, or null if not found
     */
    @Nullable
    public StackedSpawner getStackedSpawner(@NotNull Block block) {
        Objects.requireNonNull(block);

        return this.stackManager.getStackedSpawner(block);
    }

    /**
     * Checks if a given LivingEntity is part of a StackedEntity
     *
     * @param livingEntity the entity to check
     * @return true if the entity is part of a StackedEntity, otherwise false
     */
    public boolean isEntityStacked(@NotNull LivingEntity livingEntity) {
        Objects.requireNonNull(livingEntity);

        return this.stackManager.isEntityStacked(livingEntity);
    }

    /**
     * Checks if a given Item is part of a StackedItem
     *
     * @param item the item to check
     * @return true if the item is part of a StackedItem, otherwise false
     */
    public boolean isItemStacked(@NotNull Item item) {
        Objects.requireNonNull(item);

        return this.stackManager.isItemStacked(item);
    }

    /**
     * Checks if a given Block is part of a StackedBlock
     *
     * @param block the block to check
     * @return true if the block is part of a StackedBlock, otherwise false
     */
    public boolean isBlockStacked(@NotNull Block block) {
        Objects.requireNonNull(block);

        return this.stackManager.isBlockStacked(block);
    }

    /**
     * Checks if a given Block is part of a StackedSpawner
     *
     * @param block the block to check
     * @return true if the block is part of a StackedSpawner, otherwise false
     */
    public boolean isSpawnerStacked(@NotNull Block block) {
        Objects.requireNonNull(block);

        return this.stackManager.isSpawnerStacked(block);
    }

    /**
     * Removes a StackedEntity
     *
     * @param stackedEntity the StackedEntity to remove
     */
    public void removeEntityStack(@NotNull StackedEntity stackedEntity) {
        Objects.requireNonNull(stackedEntity);

        this.stackManager.removeEntityStack(stackedEntity);
    }

    /**
     * Removes a StackedItem
     *
     * @param stackedItem the StackedItem to remove
     */
    public void removeItemStack(@NotNull StackedItem stackedItem) {
        Objects.requireNonNull(stackedItem);

        this.stackManager.removeItemStack(stackedItem);
    }

    /**
     * Removes a StackedBlock
     *
     * @param stackedBlock the StackedBlock to remove
     */
    public void removeBlockStack(@NotNull StackedBlock stackedBlock) {
        Objects.requireNonNull(stackedBlock);

        this.stackManager.removeBlockStack(stackedBlock);
    }

    /**
     * Removes a StackedSpawner
     *
     * @param stackedSpawner the StackedSpawner to remove
     */
    public void removeSpawnerStack(@NotNull StackedSpawner stackedSpawner) {
        Objects.requireNonNull(stackedSpawner);

        this.stackManager.removeSpawnerStack(stackedSpawner);
    }

    /**
     * Removes all StackedEntities and the LivingEntities tied to them
     *
     * @return the number of entities removed
     */
    public int removeAllEntityStacks() {
        return this.stackManager.removeAllEntityStacks();
    }

    /**
     * Removes all StackedItems and the Items tied to them
     *
     * @return the number of items removed
     */
    public int removeAllItemStacks() {
        return this.stackManager.removeAllItemStacks();
    }

    /**
     * Creates a StackedEntity from a LivingEntity
     *
     * @param livingEntity the LivingEntity to create a stack from
     * @param tryStack true to try to stack the entity instantly, otherwise false
     * @return the newly created stack, or null if one wasn't created
     */
    @Nullable
    public StackedEntity createEntityStack(@NotNull LivingEntity livingEntity, boolean tryStack) {
        Objects.requireNonNull(livingEntity);

        return this.stackManager.createEntityStack(livingEntity, tryStack);
    }

    /**
     * Creates a StackedItem from an Item
     *
     * @param item the Item to create a stack from
     * @param tryStack true to try to stack the item instantly, otherwise false
     * @return the newly created stack, or null if one wasn't created
     */
    @Nullable
    public StackedItem createItemStack(@NotNull Item item, boolean tryStack) {
        Objects.requireNonNull(item);

        return this.stackManager.createItemStack(item, tryStack);
    }

    /**
     * Creates a StackedBlock from a Block
     *
     * @param block the Block to create a stack from
     * @param amount the size of the stack being created
     * @return the newly created stack, or null if one wasn't created
     */
    @Nullable
    public StackedBlock createBlockStack(@NotNull Block block, int amount) {
        Objects.requireNonNull(block);

        return this.stackManager.createBlockStack(block, amount);
    }

    /**
     * Creates a StackedSpawner from a Block
     *
     * @param block the Block to create a stack from
     * @param amount the size of the stack being created
     * @param placedByPlayer true for if the spawner was placed by a player, false otherwise
     * @return the newly created stack, or null if one wasn't created
     */
    @Nullable
    public StackedSpawner createSpawnerStack(@NotNull Block block, int amount, boolean placedByPlayer) {
        Objects.requireNonNull(block);

        return this.stackManager.createSpawnerStack(block, amount, placedByPlayer);
    }

    /**
     * Creates a StackedSpawner from a Block
     *
     * @param block the Block to create a stack from
     * @param amount the size of the stack being created
     * @return the newly created stack, or null if one wasn't created
     */
    @Nullable
    public StackedSpawner createSpawnerStack(@NotNull Block block, int amount) {
        return this.createSpawnerStack(block, amount, false);
    }

    /**
     * Pre-stacks a number of entities of a given type and a custom spawn reason and spawns StackedEntities at the given location
     *
     * @param entityType the type of entity to spawn
     * @param amount the amount of entities to spawn
     * @param location the location to spawn at
     * @param spawnReason The reason the entities are being spawned
     */
    public void preStackEntities(@NotNull EntityType entityType, int amount, @NotNull Location location, @NotNull SpawnReason spawnReason) {
        Objects.requireNonNull(entityType);
        Objects.requireNonNull(location);
        Objects.requireNonNull(spawnReason);

        this.stackManager.preStackEntities(entityType, amount, location, spawnReason);
    }

    /**
     * Pre-stacks a number of entities of a given type and spawns StackedEntities at the given location
     *
     * @param entityType the type of entity to spawn
     * @param amount the amount of entities to spawn
     * @param location the location to spawn at
     */
    public void preStackEntities(@NotNull EntityType entityType, int amount, @NotNull Location location) {
        Objects.requireNonNull(entityType);
        Objects.requireNonNull(location);

        this.stackManager.preStackEntities(entityType, amount, location);
    }

    /**
     * Pre-stacks a collection of ItemStacks and spawns StackedItems at the given location
     *
     * @param items the items to stack and spawn
     * @param location the location to spawn at
     */
    public void preStackItems(@NotNull Collection<ItemStack> items, Location location) {
        Objects.requireNonNull(items);

        this.stackManager.preStackItems(items, location);
    }

    /**
     * Drops a single StackedItem at the given Location with a specified ItemStack and amount
     *
     * @param itemStack The ItemStack to drop
     * @param amount The amount of the item to drop
     * @param location The Location to drop the item at
     * @param dropNaturally true to drop naturally, false otherwise
     * @return The newly created StackedItem, may be null if item stacking is disabled
     */
    @Nullable
    public StackedItem dropItemStack(@NotNull ItemStack itemStack, int amount, @NotNull Location location, boolean dropNaturally) {
        Objects.requireNonNull(itemStack);
        Objects.requireNonNull(location);

        return this.stackManager.dropItemStack(itemStack, amount, location, dropNaturally);
    }

    //endregion

    //region Stack Settings

    /**
     * Gets the EntityStackSettings for an entity
     *
     * @param entity The entity to get the settings of
     * @return The EntityStackSettings for the entity
     */
    @NotNull
    public EntityStackSettings getEntityStackSettings(@NotNull LivingEntity entity) {
        Objects.requireNonNull(entity);

        return this.getEntityStackSettings(entity.getType());
    }

    /**
     * Gets the EntityStackSettings for an entity type
     *
     * @param entityType The entity type to get the settings of
     * @return The EntityStackSettings for the entity type
     */
    @NotNull
    public EntityStackSettings getEntityStackSettings(@NotNull EntityType entityType) {
        Objects.requireNonNull(entityType);

        return this.stackSettingManager.getEntityStackSettings(entityType);
    }

    /**
     * Gets the EntityStackSettings for a spawn egg material
     *
     * @param material The spawn egg material to get the settings of
     * @return The EntityStackSettings for the spawn egg material, or null if the material is not a spawn egg
     */
    @Nullable
    public EntityStackSettings getEntityStackSettings(@NotNull Material material) {
        Objects.requireNonNull(material);

        return this.stackSettingManager.getEntityStackSettings(material);
    }

    /**
     * Gets the ItemStackSettings for an item
     *
     * @param item The item to get the settings of
     * @return The ItemStackSettings for the item
     */
    @NotNull
    public ItemStackSettings getItemStackSettings(@NotNull Item item) {
        Objects.requireNonNull(item);

        return this.getItemStackSettings(item.getItemStack().getType());
    }

    /**
     * Gets the ItemStackSettings for an item type
     *
     * @param material The item type to get the settings of
     * @return The ItemStackSettings for the item type
     */
    @NotNull
    public ItemStackSettings getItemStackSettings(@NotNull Material material) {
        Objects.requireNonNull(material);

        return this.stackSettingManager.getItemStackSettings(material);
    }

    /**
     * Gets the BlockStackSettings for a block
     *
     * @param block The block to get the settings of
     * @return The BlockStackSettings for the block, or null if the block type is not stackable
     */
    @Nullable
    public BlockStackSettings getBlockStackSettings(@NotNull Block block) {
        Objects.requireNonNull(block);

        return this.getBlockStackSettings(block.getType());
    }

    /**
     * Gets the BlockStackSettings for a block type
     *
     * @param material The block material to get the settings of
     * @return The BlockStackSettings for the block type, or null if the block type is not stackable
     */
    @Nullable
    public BlockStackSettings getBlockStackSettings(@NotNull Material material) {
        Objects.requireNonNull(material);

        return this.stackSettingManager.getBlockStackSettings(material);
    }

    /**
     * Gets the SpawnerStackSettings for a spawner
     *
     * @param creatureSpawner The spawner to get the settings of
     * @return The SpawnerStackSettings for the spawner
     */
    @Nullable
    public SpawnerStackSettings getSpawnerStackSettings(@NotNull CreatureSpawner creatureSpawner) {
        Objects.requireNonNull(creatureSpawner);

        return this.getSpawnerStackSettings(creatureSpawner.getSpawnedType());
    }

    /**
     * Gets the SpawnerStackSettings for a spawner entity type
     *
     * @param entityType The spawner entity type to get the settings of
     * @return The SpawnerStackSettings for the spawner entity type
     */
    @Nullable
    public SpawnerStackSettings getSpawnerStackSettings(@NotNull EntityType entityType) {
        Objects.requireNonNull(entityType);

        return this.stackSettingManager.getSpawnerStackSettings(entityType);
    }

    //endregion

    //region Chunk Stacks

    /**
     * Gets a List of loaded StackedEntities within the given Collection of Chunks
     *
     * @param chunks The Chunks to query
     * @return A List of StackedEntities
     */
    @NotNull
    public List<StackedEntity> getStackedEntities(@NotNull Collection<Chunk> chunks) {
        Objects.requireNonNull(chunks);

        return this.stackManager.getStackedEntities().values().stream()
                .filter(x -> chunks.contains(x.getLocation().getChunk()))
                .toList();
    }

    /**
     * Gets a List of loaded StackedItems within the given Collection of Chunks
     *
     * @param chunks The Chunks to query
     * @return A List of StackedItems
     */
    @NotNull
    public List<StackedItem> getStackedItems(@NotNull Collection<Chunk> chunks) {
        Objects.requireNonNull(chunks);

        return this.stackManager.getStackedItems().values().stream()
                .filter(x -> chunks.contains(x.getLocation().getChunk()))
                .toList();
    }

    /**
     * Gets a List of loaded StackedBlocks within the given Collection of Chunks
     *
     * @param chunks The Chunks to query
     * @return A List of StackedBlocks
     */
    @NotNull
    public List<StackedBlock> getStackedBlocks(@NotNull Collection<Chunk> chunks) {
        Objects.requireNonNull(chunks);

        return this.stackManager.getStackedBlocks().values().stream()
                .filter(x -> chunks.contains(x.getLocation().getChunk()))
                .toList();
    }

    /**
     * Gets a List of loaded StackedSpawners within the given Collection of Chunks
     *
     * @param chunks The Chunks to query
     * @return A List of StackedSpawners
     */
    @NotNull
    public List<StackedSpawner> getStackedSpawners(@NotNull Collection<Chunk> chunks) {
        Objects.requireNonNull(chunks);

        return this.stackManager.getStackedSpawners().values().stream()
                .filter(x -> chunks.contains(x.getLocation().getChunk()))
                .toList();
    }

    //endregion

}
