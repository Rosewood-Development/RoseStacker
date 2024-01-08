package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.nms.spawner.SpawnerType;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorageType;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedItem;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.StackingLogic;
import dev.rosewood.rosestacker.stack.StackingThread;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.utils.DataUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

/**
 * Manages {@link StackingThread} and chunk processing
 */
public class StackManager extends Manager implements StackingLogic {

    private final Map<UUID, StackingThread> stackingThreads;

    private BukkitTask autosaveTask;

    private boolean isEntityStackingTemporarilyDisabled;
    private boolean isEntityUnstackingTemporarilyDisabled;
    private boolean isEntityStackingEnabledForPerformance;
    private boolean stackingBasedOnPerformance;

    private StackedEntityDataStorageType entityDataStorageType;

    public StackManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.stackingThreads = new ConcurrentHashMap<>();

        this.isEntityStackingTemporarilyDisabled = false;
        this.isEntityStackingEnabledForPerformance = false;
        this.stackingBasedOnPerformance = false;
    }

    @Override
    public void reload() {
        this.entityDataStorageType = StackedEntityDataStorageType.fromName(Setting.ENTITY_DATA_STORAGE_TYPE.getString());

        // Load a new StackingThread per world
        Bukkit.getWorlds().forEach(this::loadWorld);

        // Kick off autosave task if enabled
        long autosaveFrequency = Setting.AUTOSAVE_FREQUENCY.getLong();
        if (autosaveFrequency > 0) {
            long interval = autosaveFrequency * 20 * 60;
            this.autosaveTask = Bukkit.getScheduler().runTaskTimer(this.rosePlugin, () -> this.saveAllData(false), interval, interval);
        }

        this.stackingBasedOnPerformance = ConfigurationManager.Setting.PERFORMANCE_TPS_TOGGLE.getBoolean();
        if(this.stackingBasedOnPerformance){
            Bukkit.getServer().getScheduler().runTaskAsynchronously(rosePlugin, new Runnable() {
                @Override
                public void run() {
                    if(ConfigurationManager.Setting.PERFORMANCE_TPS_TOGGLE.getBoolean()){
                        Bukkit.getScheduler().runTaskTimerAsynchronously(rosePlugin, () -> {
                            double[] tps = Bukkit.getServer().getTPS();
                            if(tps.length == 0) return;
                            double lastTPS = tps[0];
                            // hysteresis
                            if(isEntityStackingEnabledForPerformance && lastTPS >= Setting.PERFORMANCE_TPS_DISABLE_ABOVE.getDouble()){
                                // stacking was enabled due to performance, the TPS increased, we can turn it off again
                                isEntityStackingEnabledForPerformance=false;
                            } else if(!isEntityStackingEnabledForPerformance && lastTPS <= Setting.PERFORMANCE_TPS_ENABLE_BELOW.getDouble()) {
                                // stacking was disabled because the performance was above the low bound, but they have decreased past that
                                isEntityStackingEnabledForPerformance=true;
                            }
                        }, 0L, 20L*30L);
                    }
                }
            });
        }
    }

    @Override
    public void disable() {
        if (this.autosaveTask != null) {
            this.autosaveTask.cancel();
            this.autosaveTask = null;
        }

        // Save anything that's loaded
        this.saveAllData(true);

        // Close and clear StackingThreads
        this.stackingThreads.values().forEach(StackingThread::close);
        this.stackingThreads.clear();
    }

    @Override
    public Map<UUID, StackedEntity> getStackedEntities() {
        Map<UUID, StackedEntity> stackedEntities = new HashMap<>();
        this.stackingThreads.values().forEach(x -> stackedEntities.putAll(x.getStackedEntities()));
        return stackedEntities;
    }

    @Override
    public Map<UUID, StackedItem> getStackedItems() {
        Map<UUID, StackedItem> stackedItems = new HashMap<>();
        this.stackingThreads.values().forEach(x -> stackedItems.putAll(x.getStackedItems()));
        return stackedItems;
    }

    @Override
    public Map<Block, StackedBlock> getStackedBlocks() {
        Map<Block, StackedBlock> stackedBlocks = new HashMap<>();
        this.stackingThreads.values().forEach(x -> stackedBlocks.putAll(x.getStackedBlocks()));
        return stackedBlocks;
    }

    @Override
    public Map<Block, StackedSpawner> getStackedSpawners() {
        Map<Block, StackedSpawner> stackedSpawners = new HashMap<>();
        this.stackingThreads.values().forEach(x -> stackedSpawners.putAll(x.getStackedSpawners()));
        return stackedSpawners;
    }

    @Override
    public StackedEntity getStackedEntity(LivingEntity livingEntity) {
        StackingThread stackingThread = this.getStackingThread(livingEntity.getWorld());
        if (stackingThread == null)
            return null;

        return stackingThread.getStackedEntity(livingEntity);
    }

    @Override
    public StackedItem getStackedItem(Item item) {
        StackingThread stackingThread = this.getStackingThread(item.getWorld());
        if (stackingThread == null)
            return null;

        return stackingThread.getStackedItem(item);
    }

    @Override
    public StackedBlock getStackedBlock(Block block) {
        StackingThread stackingThread = this.getStackingThread(block.getWorld());
        if (stackingThread == null)
            return null;

        return stackingThread.getStackedBlock(block);
    }

    @Override
    public StackedSpawner getStackedSpawner(Block block) {
        StackingThread stackingThread = this.getStackingThread(block.getWorld());
        if (stackingThread == null)
            return null;

        return stackingThread.getStackedSpawner(block);
    }

    @Override
    public boolean isEntityStacked(LivingEntity livingEntity) {
        return this.getStackedEntity(livingEntity) != null;
    }

    @Override
    public boolean isItemStacked(Item item) {
        return this.getStackedItem(item) != null;
    }

    @Override
    public boolean isBlockStacked(Block block) {
        return this.getStackedBlock(block) != null;
    }

    @Override
    public boolean isSpawnerStacked(Block block) {
        return this.getStackedSpawner(block) != null;
    }

    @Override
    public void removeEntityStack(StackedEntity stackedEntity) {
        StackingThread stackingThread = this.getStackingThread(stackedEntity.getEntity().getWorld());
        if (stackingThread != null)
            stackingThread.removeEntityStack(stackedEntity);
    }

    @Override
    public void removeItemStack(StackedItem stackedItem) {
        StackingThread stackingThread = this.getStackingThread(stackedItem.getItem().getWorld());
        if (stackingThread != null)
            stackingThread.removeItemStack(stackedItem);
    }

    @Override
    public void removeBlockStack(StackedBlock stackedBlock) {
        StackingThread stackingThread = this.getStackingThread(stackedBlock.getBlock().getWorld());
        if (stackingThread != null)
            stackingThread.removeBlockStack(stackedBlock);
    }

    @Override
    public void removeSpawnerStack(StackedSpawner stackedSpawner) {
        StackingThread stackingThread = this.getStackingThread(stackedSpawner.getWorld());
        if (stackingThread != null)
            stackingThread.removeSpawnerStack(stackedSpawner);
    }

    @Override
    public int removeAllEntityStacks() {
        int total = 0;
        for (StackingThread stackingThread : this.stackingThreads.values())
            total += stackingThread.removeAllEntityStacks();
        return total;
    }

    @Override
    public int removeAllItemStacks() {
        int total = 0;
        for (StackingThread stackingThread : this.stackingThreads.values())
            total += stackingThread.removeAllItemStacks();
        return total;
    }

    @Override
    public void updateStackedEntityKey(LivingEntity oldKey, LivingEntity newKey) {
        StackingThread stackingThread = this.getStackingThread(newKey.getWorld());
        if (stackingThread != null)
            stackingThread.updateStackedEntityKey(oldKey, newKey);
    }

    @Override
    public StackedEntity splitEntityStack(StackedEntity stackedEntity) {
        StackingThread stackingThread = this.getStackingThread(stackedEntity.getEntity().getWorld());
        if (stackingThread == null)
            return null;

        return stackingThread.splitEntityStack(stackedEntity);
    }

    @Override
    public StackedItem splitItemStack(StackedItem stackedItem, int newSize) {
        StackingThread stackingThread = this.getStackingThread(stackedItem.getItem().getWorld());
        if (stackingThread == null)
            return null;

        return stackingThread.splitItemStack(stackedItem, newSize);
    }

    @Override
    public StackedEntity createEntityStack(LivingEntity livingEntity, boolean tryStack) {
        StackingThread stackingThread = this.getStackingThread(livingEntity.getWorld());
        if (stackingThread == null)
            return null;

        return stackingThread.createEntityStack(livingEntity, tryStack);
    }

    @Override
    public StackedItem createItemStack(Item item, boolean tryStack) {
        StackingThread stackingThread = this.getStackingThread(item.getWorld());
        if (stackingThread == null)
            return null;

        return stackingThread.createItemStack(item, tryStack);
    }

    @Override
    public StackedBlock createBlockStack(Block block, int amount) {
        StackingThread stackingThread = this.getStackingThread(block.getWorld());
        if (stackingThread == null)
            return null;

        return stackingThread.createBlockStack(block, amount);
    }

    @Override
    public StackedSpawner createSpawnerStack(Block block, int amount, boolean placedByPlayer) {
        StackingThread stackingThread = this.getStackingThread(block.getWorld());
        if (stackingThread == null)
            return null;

        return stackingThread.createSpawnerStack(block, amount, placedByPlayer);
    }

    @Override
    public void addEntityStack(StackedEntity stackedEntity) {
        StackingThread stackingThread = this.getStackingThread(stackedEntity.getEntity().getWorld());
        if (stackingThread == null)
            return;

        stackingThread.addEntityStack(stackedEntity);
    }

    @Override
    public void addItemStack(StackedItem stackedItem) {
        StackingThread stackingThread = this.getStackingThread(stackedItem.getItem().getWorld());
        if (stackingThread == null)
            return;

        stackingThread.addItemStack(stackedItem);
    }

    @Override
    public void preStackEntities(EntityType entityType, int amount, Location location, SpawnReason spawnReason) {
        World world = location.getWorld();
        if (world == null)
            return;

        StackingThread stackingThread = this.getStackingThread(world);
        if (stackingThread == null)
            return;

        stackingThread.preStackEntities(entityType, amount, location, spawnReason);
    }

    @Override
    public void preStackEntities(EntityType entityType, int amount, Location location) {
        this.preStackEntities(entityType, amount, location, SpawnReason.CUSTOM);
    }

    @Override
    public void preStackItems(Collection<ItemStack> items, Location location, boolean dropNaturally) {
        World world = location.getWorld();
        if (world == null)
            return;

        StackingThread stackingThread = this.getStackingThread(world);
        if (stackingThread == null)
            return;

        stackingThread.preStackItems(items, location, dropNaturally);
    }

    @Override
    public StackedItem dropItemStack(ItemStack itemStack, int amount, Location location, boolean dropNaturally) {
        World world = location.getWorld();
        if (world == null)
            return null;

        StackingThread stackingThread = this.getStackingThread(world);
        if (stackingThread == null)
            return null;

        return stackingThread.dropItemStack(itemStack, amount, location, dropNaturally);
    }

    @Override
    public void loadChunkBlocks(Chunk chunk) {
        StackingThread stackingThread = this.getStackingThread(chunk.getWorld());
        if (stackingThread != null)
            stackingThread.loadChunkBlocks(chunk);
    }

    @Override
    public void loadChunkEntities(Chunk chunk, List<Entity> entities) {
        StackingThread stackingThread = this.getStackingThread(chunk.getWorld());
        if (stackingThread != null)
            stackingThread.loadChunkEntities(chunk, entities);
    }

    @Override
    public void saveChunkBlocks(Chunk chunk, boolean clearStored) {
        StackingThread stackingThread = this.getStackingThread(chunk.getWorld());
        if (stackingThread != null)
            stackingThread.saveChunkBlocks(chunk, clearStored);
    }

    @Override
    public void saveChunkEntities(Chunk chunk, List<Entity> entities, boolean clearStored) {
        StackingThread stackingThread = this.getStackingThread(chunk.getWorld());
        if (stackingThread != null)
            stackingThread.saveChunkEntities(chunk, entities, clearStored);
    }

    /**
     * Saves all data in loaded chunks
     */
    public void saveAllData(boolean clearStored) {
        for (StackingThread stackingThread : this.stackingThreads.values()) {
            for (Chunk chunk : stackingThread.getTargetWorld().getLoadedChunks()) {
                stackingThread.saveChunkBlocks(chunk, clearStored);
                stackingThread.saveChunkEntities(chunk, Arrays.asList(chunk.getEntities()), clearStored);
            }
        }
    }

    public boolean isEntityStackingEnabled() {
        return Setting.ENTITY_STACKING_ENABLED.getBoolean();
    }

    public boolean isItemStackingEnabled() {
        return Setting.ITEM_STACKING_ENABLED.getBoolean();
    }

    public boolean isBlockStackingEnabled() {
        return Setting.BLOCK_STACKING_ENABLED.getBoolean();
    }

    public boolean isSpawnerStackingEnabled() {
        return Setting.SPAWNER_STACKING_ENABLED.getBoolean();
    }

    /**
     * Gets a StackingThread for a World
     *
     * @param world the World
     * @return a StackingThread for the World, otherwise null if one doesn't exist
     */
    public StackingThread getStackingThread(World world) {
        return this.stackingThreads.get(world.getUID());
    }

    /**
     * @return a Map of key -> World UUID, value -> StackingThread of all StackingThreads
     */
    public Map<UUID, StackingThread> getStackingThreads() {
        return this.stackingThreads;
    }

    /**
     * Creates a StackingThread for the given World
     *
     * @param world to create a StackingThread for
     */
    public void loadWorld(World world) {
        if (this.isWorldDisabled(world) || this.stackingThreads.containsKey(world.getUID()))
            return;

        this.stackingThreads.put(world.getUID(), new StackingThread(this.rosePlugin, this, world));
    }

    /**
     * Removes a World's StackingThread
     *
     * @param world to remove the StackingThread of
     */
    public void unloadWorld(World world) {
        UUID worldUUID = world.getUID();
        StackingThread stackingThread = this.stackingThreads.get(worldUUID);
        if (stackingThread != null) {
            stackingThread.close();
            this.stackingThreads.remove(worldUUID);
        }
    }

    /**
     * Checks if a given block type is able to be stacked
     *
     * @param block The block to check
     * @return true if the block is stackable, otherwise false
     */
    public boolean isBlockTypeStackable(Block block) {
        BlockStackSettings stackSettings = this.rosePlugin.getManager(StackSettingManager.class).getBlockStackSettings(block);
        return stackSettings != null && stackSettings.isStackingEnabled();
    }

    /**
     * Checks if a given entity type for a spawner is able to be stacked
     *
     * @param entityType the type to check
     * @return true if the spawner entity type is stackable, otherwise false
     */
    public boolean isSpawnerTypeStackable(EntityType entityType) {
        SpawnerStackSettings stackSettings = this.rosePlugin.getManager(StackSettingManager.class).getSpawnerStackSettings(entityType);
        return stackSettings != null && stackSettings.isStackingEnabled();
    }

    /**
     * Checks if a given spanwer type for a spawner is able to be stacked
     *
     * @param spawnerType the type to check
     * @return true if the spawner entity type is stackable, otherwise false
     */
    public boolean isSpawnerTypeStackable(SpawnerType spawnerType) {
        SpawnerStackSettings stackSettings = this.rosePlugin.getManager(StackSettingManager.class).getSpawnerStackSettings(spawnerType);
        return stackSettings != null && stackSettings.isStackingEnabled();
    }

    /**
     * Checks if stacking is disabled in a given World
     *
     * @param world the World to check
     * @return true if stacking is disabled in the World, otherwise false
     */
    public boolean isWorldDisabled(World world) {
        if (world == null)
            return true;
        return Setting.DISABLED_WORLDS.getStringList().stream().anyMatch(x -> x.equalsIgnoreCase(world.getName()));
    }

    public void changeStackingThread(UUID entityUUID, StackedEntity stackedEntity, World from, World to) {
        StackingThread fromThread = this.getStackingThread(from);
        StackingThread toThread = this.getStackingThread(to);

        if (fromThread == null || toThread == null)
            return;

        DataUtils.writeStackedEntity(stackedEntity);
        fromThread.transferExistingEntityStack(entityUUID, stackedEntity, toThread);
    }

    public void changeStackingThread(UUID entityUUID, StackedItem stackedItem, World from, World to) {
        StackingThread fromThread = this.getStackingThread(from);
        StackingThread toThread = this.getStackingThread(to);

        if (fromThread == null || toThread == null)
            return;

        DataUtils.writeStackedItem(stackedItem);
        fromThread.transferExistingEntityStack(entityUUID, stackedItem, toThread);
    }

    public void processNametags() {
        for (StackingThread stackingThread : this.stackingThreads.values())
            stackingThread.processNametags();
    }

    /**
     * Toggles instant entity stacking as temporarily disabled to allow for entity manipulation without
     * stacks automatically being created.
     *
     * @param disabled true to disable, otherwise false to enable
     */
    public void setEntityStackingTemporarilyDisabled(boolean disabled) {
        this.isEntityStackingTemporarilyDisabled = disabled;
    }

    /**
     * Toggles entity unstacking as temporarily disabled to allow for entity manipulation without stacks
     * automatically unstacking.
     *
     * @param disabled true to disable, otherwise false to enable
     */
    public void setEntityUnstackingTemporarilyDisabled(boolean disabled) {
        this.isEntityUnstackingTemporarilyDisabled = disabled;
    }

    /**
     * @return true if instant entity stacking is temporarily disabled, otherwise false
     */
    public boolean isEntityStackingTemporarilyDisabled() {
        return this.isEntityStackingTemporarilyDisabled || (this.stackingBasedOnPerformance && !this.isEntityStackingEnabledForPerformance);
    }

    /**
     * @return true if entity unstacking is temporarily disabled, otherwise false
     */
    public boolean isEntityUnstackingTemporarilyDisabled() {
        return this.isEntityUnstackingTemporarilyDisabled;
    }

    /**
     * @return the current entity data storage type for newly created entity stacks
     */
    public StackedEntityDataStorageType getEntityDataStorageType(EntityType entityType) {
        EntityStackSettings entityStackSettings = this.rosePlugin.getManager(StackSettingManager.class).getEntityStackSettings(entityType);
        if (entityStackSettings != null && entityStackSettings.getStackedEntityDataStorageType() != null)
            return entityStackSettings.getStackedEntityDataStorageType();
        return this.entityDataStorageType;
    }

    /**
     * @return the current entity data storage type for newly created entity stacks
     * @deprecated use {@link #getEntityDataStorageType(EntityType)} as this can now be changed per entity type
     */
    @Deprecated
    public StackedEntityDataStorageType getEntityDataStorageType() {
        return this.entityDataStorageType;
    }

}
