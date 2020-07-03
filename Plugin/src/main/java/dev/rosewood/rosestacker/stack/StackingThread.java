package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.ConversionManager;
import dev.rosewood.rosestacker.manager.DataManager;
import dev.rosewood.rosestacker.manager.HologramManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSUtil;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.ItemStackSettings;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class StackingThread implements StackingLogic, Runnable, AutoCloseable {

    private final static int CLEANUP_TIMER_TARGET = 100;

    private final RoseStacker roseStacker;
    private final StackManager stackManager;
    private final StackSettingManager stackSettingManager;
    private final HologramManager hologramManager;
    private final ConversionManager conversionManager;
    private final World targetWorld;

    private final BukkitTask stackTask;
    private final BukkitTask pendingChunkTask;
    private final Set<Chunk> pendingLoadChunks;
    private final Set<Chunk> pendingUnloadChunks;

    private final Map<UUID, StackedEntity> stackedEntities;
    private final Map<UUID, StackedItem> stackedItems;
    private final Map<Block, StackedBlock> stackedBlocks;
    private final Map<Block, StackedSpawner> stackedSpawners;

    private int cleanupTimer;

    public StackingThread(RoseStacker roseStacker, StackManager stackManager, World targetWorld) {
        this.roseStacker = roseStacker;
        this.stackManager = stackManager;
        this.stackSettingManager = this.roseStacker.getManager(StackSettingManager.class);
        this.hologramManager = this.roseStacker.getManager(HologramManager.class);
        this.conversionManager = this.roseStacker.getManager(ConversionManager.class);
        this.targetWorld = targetWorld;

        this.stackTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.roseStacker, this, 5L, Setting.STACK_FREQUENCY.getLong());
        this.pendingChunkTask = Bukkit.getScheduler().runTaskTimer(this.roseStacker, this::processPendingChunks, 0L, 3L);
        this.pendingLoadChunks = new HashSet<>();
        this.pendingUnloadChunks = new HashSet<>();

        this.stackedEntities = new ConcurrentHashMap<>();
        this.stackedItems = new ConcurrentHashMap<>();
        this.stackedBlocks = new ConcurrentHashMap<>();
        this.stackedSpawners = new ConcurrentHashMap<>();

        this.cleanupTimer = 0;

        // Load all existing stacks in the target world
        this.pendingLoadChunks.addAll(Arrays.asList(this.targetWorld.getLoadedChunks()));
    }

    @Override
    public void run() {
        // Auto stack items
        if (this.stackManager.isItemStackingEnabled()) {
            for (StackedItem stackedItem : new HashSet<>(this.stackedItems.values())) {
                Item item = stackedItem.getItem();
                if (item == null || !item.isValid()) {
                    this.removeItemStack(stackedItem);
                    continue;
                }

                this.tryStackItem(stackedItem);
            }
        }

        // Auto stack entities
        if (!this.stackManager.isEntityStackingEnabled())
            return;

        for (StackedEntity stackedEntity : new HashSet<>(this.stackedEntities.values())) {
            LivingEntity livingEntity = stackedEntity.getEntity();
            if (livingEntity == null || !livingEntity.isValid()) {
                this.removeEntityStack(stackedEntity);
                continue;
            }

            this.tryStackEntity(stackedEntity);
        }

        // Auto unstack entities
        for (StackedEntity stackedEntity : new HashSet<>(this.stackedEntities.values()))
            if (!stackedEntity.shouldStayStacked())
                Bukkit.getScheduler().runTask(this.roseStacker, () -> this.splitEntityStack(stackedEntity));

        // Cleans up entities that aren't stacked
        this.cleanupTimer++;
        if (this.cleanupTimer >= CLEANUP_TIMER_TARGET) {
            for (Entity entity : this.targetWorld.getEntities()) {
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if (!this.isEntityStacked(livingEntity))
                        this.createEntityStack(livingEntity, true);
                } else if (entity instanceof Item) {
                    Item item = (Item) entity;
                    if (!this.isItemStacked(item))
                        this.createItemStack(item, true);
                }
            }
            this.cleanupTimer = 0;
        }

        // Handle dynamic stack tags
        boolean dynamicEntityTags = Setting.ENTITY_DISPLAY_TAGS.getBoolean() && Setting.ENTITY_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE_ENABLED.getBoolean();
        boolean dynamicItemTags = Setting.ITEM_DISPLAY_TAGS.getBoolean() && Setting.ITEM_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE_ENABLED.getBoolean();
        boolean dynamicBlockTags = Setting.BLOCK_DISPLAY_TAGS.getBoolean() && Setting.BLOCK_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE_ENABLED.getBoolean();
        boolean dynamicSpawnerTags = Setting.SPAWNER_DISPLAY_TAGS.getBoolean() && Setting.SPAWNER_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE_ENABLED.getBoolean();

        if (!(dynamicEntityTags || dynamicItemTags || dynamicBlockTags || dynamicSpawnerTags))
            return;

        double entityItemDynamicViewRange = Setting.ENTITY_ITEM_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE.getDouble();
        double blockSpawnerDynamicViewRange = Setting.BLOCK_SPAWNER_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE.getDouble();

        double entityItemDynamicViewRangeSqrd = entityItemDynamicViewRange * entityItemDynamicViewRange;
        double blockSpawnerDynamicViewRangeSqrd = blockSpawnerDynamicViewRange * blockSpawnerDynamicViewRange;

        boolean entityItemDynamicWallDetection = Setting.ENTITY_ITEM_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE_WALL_DETECTION_ENABLED.getBoolean();
        boolean blockSpawnerDynamicWallDetection = Setting.BLOCK_SPAWNER_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE_WALL_DETECTION_ENABLED.getBoolean();

        double maxEntityRenderDistanceSqrd = 75 * 75;
        Set<EntityType> validEntities = StackerUtils.getStackableEntityTypes();
        for (Player player : this.targetWorld.getPlayers()) {
            if (player.getWorld() != this.targetWorld)
                continue;

            for (Entity entity : this.targetWorld.getEntities()) {
                if (entity.getType() == EntityType.PLAYER)
                    continue;

                double distanceSqrd;
                try { // The locations can end up comparing cross-world if the player/entity switches worlds mid-loop due to being async
                    distanceSqrd = player.getLocation().distanceSquared(entity.getLocation());
                } catch (Exception e) {
                    continue;
                }

                if (distanceSqrd > maxEntityRenderDistanceSqrd)
                    continue;

                boolean visible;
                if ((validEntities.contains(entity.getType()) || entity.getType() == EntityType.DROPPED_ITEM) && entity.isCustomNameVisible()) {
                    visible = distanceSqrd < entityItemDynamicViewRangeSqrd;
                    if (entityItemDynamicWallDetection)
                        visible &= StackerUtils.hasLineOfSight(player, entity, 0.75, true);
                } else if (entity.getType() == EntityType.ARMOR_STAND && this.hologramManager.isHologram(entity)) {
                    visible = distanceSqrd < blockSpawnerDynamicViewRangeSqrd;
                    if (blockSpawnerDynamicWallDetection)
                        visible &= StackerUtils.hasLineOfSight(player, entity, 0.75, true);
                } else continue;

                NMSUtil.getHandler().toggleEntityNameTagForPlayer(player, entity, visible);
            }
        }
    }

    @Override
    public void close() {
        DataManager dataManager = this.roseStacker.getManager(DataManager.class);

        // Restore custom names
        for (StackedEntity stackedEntity : this.stackedEntities.values())
            stackedEntity.getEntity().setCustomName(stackedEntity.getOriginalCustomName());

        // Save anything that's loaded
        if (this.stackManager.isEntityStackingEnabled())
            dataManager.createOrUpdateStackedEntities(this.stackedEntities.values());

        if (this.stackManager.isItemStackingEnabled())
            dataManager.createOrUpdateStackedItems(this.stackedItems.values());

        if (this.stackManager.isBlockStackingEnabled())
            dataManager.createOrUpdateStackedBlocksOrSpawners(this.stackedBlocks.values());

        if (this.stackManager.isSpawnerStackingEnabled())
            dataManager.createOrUpdateStackedBlocksOrSpawners(this.stackedSpawners.values());

        if (this.stackTask != null)
            this.stackTask.cancel();

        if (this.pendingChunkTask != null)
            this.pendingChunkTask.cancel();
    }

    @Override
    public Map<UUID, StackedEntity> getStackedEntities() {
        return this.stackedEntities;
    }

    @Override
    public Map<UUID, StackedItem> getStackedItems() {
        return this.stackedItems;
    }

    @Override
    public Map<Block, StackedBlock> getStackedBlocks() {
        return this.stackedBlocks;
    }

    @Override
    public Map<Block, StackedSpawner> getStackedSpawners() {
        return this.stackedSpawners;
    }

    @Override
    public StackedEntity getStackedEntity(LivingEntity livingEntity) {
        return this.stackedEntities.get(livingEntity.getUniqueId());
    }

    @Override
    public StackedItem getStackedItem(Item item) {
        return this.stackedItems.get(item.getUniqueId());
    }

    @Override
    public StackedBlock getStackedBlock(Block block) {
        return this.stackedBlocks.get(block);
    }

    @Override
    public StackedSpawner getStackedSpawner(Block block) {
        return this.stackedSpawners.get(block);
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
        LivingEntity entity = stackedEntity.getEntity();
        if (entity != null) {
            UUID key = stackedEntity.getEntity().getUniqueId();
            if (this.stackedEntities.containsKey(key)) {
                this.stackedEntities.remove(key);
                this.stackManager.markStackDeleted(stackedEntity);
            }
        } else {
            // Entity is null so we have to remove by value instead
            for (UUID key : this.stackedEntities.keySet()) {
                if (this.stackedEntities.get(key) == stackedEntity) {
                    this.stackedEntities.remove(key);
                    this.stackManager.markStackDeleted(stackedEntity);
                    return;
                }
            }
        }
    }

    @Override
    public void removeItemStack(StackedItem stackedItem) {
        Item item = stackedItem.getItem();
        if (item != null) {
            UUID key = stackedItem.getItem().getUniqueId();
            if (this.stackedItems.containsKey(key)) {
                this.stackedItems.remove(key);
                this.stackManager.markStackDeleted(stackedItem);
            }
        } else {
            // Item is null so we have to remove by value instead
            for (UUID key : this.stackedItems.keySet()) {
                if (this.stackedItems.get(key) == stackedItem) {
                    this.stackedItems.remove(key);
                    this.stackManager.markStackDeleted(stackedItem);
                    return;
                }
            }
        }

    }

    @Override
    public void removeBlockStack(StackedBlock stackedBlock) {
        Block key = stackedBlock.getBlock();
        if (this.stackedBlocks.containsKey(key)) {
            this.stackedBlocks.remove(key);
            this.stackManager.markStackDeleted(stackedBlock);
        }
    }

    @Override
    public void removeSpawnerStack(StackedSpawner stackedSpawner) {
        Block key = stackedSpawner.getSpawner().getBlock();
        if (this.stackedSpawners.containsKey(key)) {
            this.stackedSpawners.remove(key);
            this.stackManager.markStackDeleted(stackedSpawner);
        }
    }

    @Override
    public int removeAllEntityStacks() {
        int total = this.stackedEntities.size();
        this.stackedEntities.values().forEach(this.stackManager::markStackDeleted);
        this.stackedEntities.values().stream().map(StackedEntity::getEntity).forEach(LivingEntity::remove);
        this.stackedEntities.clear();
        return total;
    }

    @Override
    public int removeAllItemStacks() {
        int total = this.stackedItems.size();
        this.stackedItems.values().forEach(this.stackManager::markStackDeleted);
        this.stackedItems.values().stream().map(StackedItem::getItem).forEach(Item::remove);
        this.stackedItems.clear();
        return total;
    }

    @Override
    public void updateStackedEntityKey(LivingEntity oldKey, LivingEntity newKey) {
        StackedEntity stackedEntity = this.stackedEntities.get(oldKey.getUniqueId());
        if (stackedEntity != null) {
            this.stackedEntities.remove(oldKey.getUniqueId());
            this.stackedEntities.put(newKey.getUniqueId(), stackedEntity);
        }
    }

    @Override
    public StackedEntity splitEntityStack(StackedEntity stackedEntity) {
        StackedEntity newlySplit = stackedEntity.split();
        this.stackedEntities.put(newlySplit.getEntity().getUniqueId(), newlySplit);
        return newlySplit;
    }

    @Override
    public StackedItem splitItemStack(StackedItem stackedItem, int newSize) {
        World world = stackedItem.getLocation().getWorld();
        if (world == null)
            return null;

        ItemStack oldItemStack = stackedItem.getItem().getItemStack();
        ItemStack newItemStack = oldItemStack.clone();

        newItemStack.setAmount(newSize);

        stackedItem.getItem().setPickupDelay(60);
        stackedItem.getItem().setTicksLived(1);

        Item newItem = world.dropItemNaturally(stackedItem.getLocation(), newItemStack);
        newItem.setPickupDelay(0);

        StackedItem newStackedItem = new StackedItem(newSize, newItem);
        this.stackedItems.put(newItem.getUniqueId(), newStackedItem);
        stackedItem.increaseStackSize(-newSize);
        return newStackedItem;
    }

    @Override
    public StackedEntity createEntityStack(LivingEntity livingEntity, boolean tryStack) {
        if (!this.stackManager.isEntityStackingEnabled())
            return null;

        if (livingEntity instanceof Player || livingEntity instanceof ArmorStand)
            return null;

        StackedEntity newStackedEntity = new StackedEntity(livingEntity);
        this.stackedEntities.put(livingEntity.getUniqueId(), newStackedEntity);

        if (tryStack)
            this.tryStackEntity(newStackedEntity);

        return newStackedEntity;
    }

    @Override
    public StackedItem createItemStack(Item item, boolean tryStack) {
        if (!this.stackManager.isItemStackingEnabled())
            return null;

        StackedItem newStackedItem = new StackedItem(item.getItemStack().getAmount(), item);
        this.stackedItems.put(item.getUniqueId(), newStackedItem);

        if (tryStack)
            this.tryStackItem(newStackedItem);

        return newStackedItem;
    }

    @Override
    public StackedBlock createBlockStack(Block block, int amount) {
        if (!this.stackManager.isBlockStackingEnabled() || !this.stackManager.isBlockTypeStackable(block))
            return null;

        StackedBlock newStackedBlock = new StackedBlock(amount, block);
        this.stackedBlocks.put(block, newStackedBlock);
        return newStackedBlock;
    }

    @Override
    public StackedSpawner createSpawnerStack(Block block, int amount) {
        if (block.getType() != Material.SPAWNER)
            return null;

        CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
        if (!this.stackManager.isSpawnerStackingEnabled() || !this.stackManager.isSpawnerTypeStackable(creatureSpawner.getSpawnedType()))
            return null;

        StackedSpawner newStackedSpawner = new StackedSpawner(amount, creatureSpawner);
        this.stackedSpawners.put(block, newStackedSpawner);
        return newStackedSpawner;
    }

    @Override
    public void addEntityStack(StackedEntity stackedEntity) {
        this.stackedEntities.put(stackedEntity.getEntity().getUniqueId(), stackedEntity);
        this.tryStackEntity(stackedEntity);
    }

    @Override
    public void addItemStack(StackedItem stackedItem) {
        this.stackedItems.put(stackedItem.getItem().getUniqueId(), stackedItem);
        this.tryStackItem(stackedItem);
    }

    @Override
    public void preStackEntities(EntityType entityType, int amount, Location location) {
        World world = location.getWorld();
        if (world == null)
            return;

//        this.stackManager.setEntityStackingTemporarilyDisabled(true);
//
//        EntityStackSettings stackSettings = this.stackSettingManager.getEntityStackSettings(entityType);
//        Set<StackedEntity> stackedEntities = new HashSet<>();
//        NMSHandler nmsHandler = NMSUtil.getHandler();
//        for (int i = 0; i < amount; i++) {
//            LivingEntity entity = nmsHandler.createEntityUnspawned(entityType, location);
//            System.out.println(nmsHandler.getEntityAsNBT())
//            Optional<StackedEntity> matchingEntity = stackedEntities.stream().filter(x -> stackSettings.canStackWith(x, new StackedEntity(entity), false)).findFirst();
//            if (matchingEntity.isPresent()) {
//                matchingEntity.get().increaseStackSize(entity);
//            } else {
//                entity.setVelocity(Vector.getRandom().multiply(0.01)); // Move the entities slightly so they don't all bunch together
//                LivingEntity spawnedEntity = nmsHandler.spawnEntityFromNBT(nmsHandler.getEntityAsNBT(entity, Setting.ENTITY_SAVE_ATTRIBUTES.getBoolean()), location);
//                stackedEntities.add(new StackedEntity(spawnedEntity));
//            }
//        }
//
//        stackedEntities.forEach(this::addEntityStack);
//        this.stackManager.setEntityStackingTemporarilyDisabled(false);

        // Couldn't get the above to apply entity variants (such as sheep color)
        for (int i = 0; i < amount; i++)
            world.spawnEntity(location, entityType);
    }

    @Override
    public void preStackItems(Collection<ItemStack> items, Location location) {
        if (location.getWorld() == null)
            return;

        this.stackManager.setEntityStackingTemporarilyDisabled(true);

        Set<StackedItem> stackedItems = new HashSet<>();
        for (ItemStack itemStack : items) {
            Optional<StackedItem> matchingItem = stackedItems.stream().filter(x -> x.getItem().getItemStack().isSimilar(itemStack)).findFirst();
            if (matchingItem.isPresent()) {
                matchingItem.get().increaseStackSize(itemStack.getAmount());
            } else {
                Item item = location.getWorld().dropItemNaturally(location, itemStack);
                stackedItems.add(new StackedItem(item.getItemStack().getAmount(), item));
            }
        }

        stackedItems.forEach(this::addItemStack);
        this.stackManager.setEntityStackingTemporarilyDisabled(false);
    }

    @Override
    public void loadChunk(Chunk chunk) {
        this.pendingLoadChunks.add(chunk);
    }

    @Override
    public void unloadChunk(Chunk chunk) {
        this.pendingUnloadChunks.add(chunk);
    }

    private boolean containsChunk(Set<Chunk> chunks, Stack stack) {
        int stackChunkX = stack.getLocation().getBlockX() >> 4;
        int stackChunkZ = stack.getLocation().getBlockZ() >> 4;
        for (Chunk chunk : chunks)
            if (chunk.getX() == stackChunkX && chunk.getZ() == stackChunkZ)
                return true;
        return false;
    }

    /**
     * Tries to stack a StackedEntity with all other StackedEntities
     *
     * @param stackedEntity the StackedEntity to try to stack
     * @return a StackedEntity that was stacked into, or null if none
     */
    private StackedEntity tryStackEntity(StackedEntity stackedEntity) {
        double maxEntityStackDistanceSqrd = Setting.ENTITY_MERGE_RADIUS.getDouble() * Setting.ENTITY_MERGE_RADIUS.getDouble();

        for (StackedEntity other : this.stackedEntities.values()) {
            if (stackedEntity == other
                    || other.getEntity() == null
                    || !other.getEntity().isValid()
                    || this.stackManager.isMarkedAsDeleted(stackedEntity)
                    || this.stackManager.isMarkedAsDeleted(other)
                    || stackedEntity.getLocation().getWorld() != other.getLocation().getWorld()
                    || stackedEntity.getEntity() == other.getEntity()
                    || stackedEntity.getEntity().getType() != other.getEntity().getType())
                continue;

            if (!Setting.ENTITY_MERGE_ENTIRE_CHUNK.getBoolean()) {
                if (stackedEntity.getLocation().distanceSquared(other.getLocation()) > maxEntityStackDistanceSqrd)
                    continue;
            } else {
                if (stackedEntity.getLocation().getChunk() != other.getLocation().getChunk())
                    continue;
            }

            // Check if we should merge the stacks
            EntityStackSettings stackSettings = this.stackSettingManager.getEntityStackSettings(stackedEntity.getEntity());
            if (stackSettings == null || !stackSettings.canStackWith(stackedEntity, other, false))
                continue;

            if (Setting.ENTITY_REQUIRE_LINE_OF_SIGHT.getBoolean() && !StackerUtils.hasLineOfSight(stackedEntity.getEntity(), other.getEntity(), 0.75, false))
                continue;

            Set<StackedEntity> targetEntities = new HashSet<>();
            targetEntities.add(stackedEntity);
            targetEntities.add(other);

            int minStackSize = stackSettings.getMinStackSize();
            if (minStackSize > 2) {
                if (!Setting.ENTITY_MERGE_ENTIRE_CHUNK.getBoolean()) {
                    for (StackedEntity nearbyStackedEntity : this.stackedEntities.values()) {
                        if (nearbyStackedEntity.getEntity().getType() == stackedEntity.getEntity().getType()
                                && stackedEntity.getLocation().distanceSquared(nearbyStackedEntity.getLocation()) <= maxEntityStackDistanceSqrd
                                && stackSettings.canStackWith(stackedEntity, nearbyStackedEntity, false))
                            targetEntities.add(nearbyStackedEntity);
                    }
                } else {
                    for (StackedEntity nearbyStackedEntity : this.stackedEntities.values()) {
                        if (nearbyStackedEntity.getEntity().getType() == stackedEntity.getEntity().getType()
                                && nearbyStackedEntity.getLocation().getChunk() == stackedEntity.getLocation().getChunk()
                                && stackSettings.canStackWith(stackedEntity, nearbyStackedEntity, false))
                            targetEntities.add(nearbyStackedEntity);
                    }
                }

                if (targetEntities.stream().mapToInt(StackedEntity::getStackSize).sum() < minStackSize)
                    continue;
            }

            StackedEntity increased = targetEntities.stream().max(StackedEntity::compareTo).orElse(stackedEntity);
            targetEntities.remove(increased);

            Set<StackedEntity> removed = new HashSet<>();
            for (StackedEntity toStack : targetEntities) {
                if (!stackSettings.canStackWith(increased, toStack, false))
                    continue;

                if (toStack.getOriginalCustomName() != null) {
                    toStack.getEntity().setCustomName(toStack.getOriginalCustomName());
                } else {
                    toStack.getEntity().setCustomName(null);
                    toStack.getEntity().setCustomNameVisible(false);
                }

                increased.increaseStackSize(toStack.getEntity());
                increased.increaseStackSize(toStack.getStackedEntityNBT());

                removed.add(toStack);
            }

            if (Bukkit.isPrimaryThread()) {
                removed.stream().map(StackedEntity::getEntity).forEach(Entity::remove);
            } else {
                Bukkit.getScheduler().runTask(this.roseStacker, () ->
                        removed.stream().map(StackedEntity::getEntity).forEach(Entity::remove));
            }

            removed.forEach(this::removeEntityStack);

            return increased;
        }

        return null;
    }

    /**
     * Tries to stack a StackedItem with all other StackedItems
     *
     * @param stackedItem the StackedItem to try to stack
     * @return a deleted StackedItem, or null if none
     */
    private StackedItem tryStackItem(StackedItem stackedItem) {
        if (this.stackManager.isMarkedAsDeleted(stackedItem) || stackedItem.getItem().getPickupDelay() > 40)
            return null;

        double maxItemStackDistanceSqrd = Setting.ITEM_MERGE_RADIUS.getDouble() * Setting.ITEM_MERGE_RADIUS.getDouble();

        for (StackedItem other : this.stackedItems.values()) {
            if (stackedItem == other
                    || this.stackManager.isMarkedAsDeleted(other)
                    || stackedItem.getLocation().getWorld() != other.getLocation().getWorld()
                    || !stackedItem.getItem().getItemStack().isSimilar(other.getItem().getItemStack())
                    || other.getItem().getPickupDelay() > 40
                    || stackedItem.getStackSize() + other.getStackSize() > Setting.ITEM_MAX_STACK_SIZE.getInt()
                    || stackedItem.getLocation().distanceSquared(other.getLocation()) > maxItemStackDistanceSqrd)
                continue;

            // Check if we should merge the stacks
            ItemStackSettings stackSettings = this.stackSettingManager.getItemStackSettings(stackedItem.getItem());
            if (stackSettings == null)
                continue;

            if (stackSettings.canStackWith(stackedItem, other, false)) {
                StackedItem increased = stackedItem.compareTo(other) > 0 ? stackedItem : other;
                StackedItem removed = increased == stackedItem ? other : stackedItem;

                increased.increaseStackSize(removed.getStackSize());
                increased.getItem().setTicksLived(1);

                if (Bukkit.isPrimaryThread()) {
                    removed.getItem().remove();
                } else {
                    Bukkit.getScheduler().runTask(this.roseStacker, removed.getItem()::remove);
                }

                this.removeItemStack(removed);

                return removed;
            }
        }

        return null;
    }

    public void transferExistingEntityStack(UUID entityUUID, StackedEntity stackedEntity, StackingThread toThread) {
        this.stackedEntities.remove(entityUUID);
        toThread.loadExistingEntityStack(entityUUID, stackedEntity);
    }

    public void transferExistingItemStack(UUID itemUUID, StackedItem stackedItem, StackingThread toThread) {
        this.stackedItems.remove(itemUUID);
        toThread.loadExistingItemStack(itemUUID, stackedItem);
    }

    private void loadExistingEntityStack(UUID entityUUID, StackedEntity stackedEntity) {
        stackedEntity.updateEntity();
        this.stackedEntities.put(entityUUID, stackedEntity);
    }

    private void loadExistingItemStack(UUID itemUUID, StackedItem stackedItem) {
        stackedItem.updateItem();
        this.stackedItems.put(itemUUID, stackedItem);
    }

    private void processPendingChunks() {
        this.pendingLoadChunks.removeIf(this.pendingUnloadChunks::contains);
        this.pendingUnloadChunks.removeIf(this.pendingLoadChunks::contains);

        if (!this.pendingLoadChunks.isEmpty()) {
            Set<Chunk> chunks = new HashSet<>(this.pendingLoadChunks);
            this.pendingLoadChunks.clear();
            Bukkit.getScheduler().runTaskAsynchronously(this.roseStacker, () -> {
                this.conversionManager.convertChunks(chunks);
                this.loadChunks(chunks);
            });
        }

        if (!this.pendingUnloadChunks.isEmpty()) {
            Set<Chunk> chunks = new HashSet<>(this.pendingUnloadChunks);
            this.pendingUnloadChunks.clear();
            Bukkit.getScheduler().runTaskAsynchronously(this.roseStacker, () -> this.unloadChunks(chunks));
        }
    }

    private void loadChunks(Set<Chunk> chunks) {
        DataManager dataManager = this.roseStacker.getManager(DataManager.class);

        if (this.stackManager.isEntityStackingEnabled())
            dataManager.getStackedEntities(chunks, (stack) -> stack.forEach(x -> this.stackedEntities.put(x.getEntity().getUniqueId(), x)));

        if (this.stackManager.isItemStackingEnabled())
            dataManager.getStackedItems(chunks, (stack) -> stack.forEach(x -> this.stackedItems.put(x.getItem().getUniqueId(), x)));

        if (this.stackManager.isBlockStackingEnabled())
            dataManager.getStackedBlocks(chunks, (stack) -> stack.forEach(x -> this.stackedBlocks.put(x.getBlock(), x)));

        if (this.stackManager.isSpawnerStackingEnabled())
            dataManager.getStackedSpawners(chunks, (stack) -> stack.forEach(x -> this.stackedSpawners.put(x.getSpawner().getBlock(), x)));
    }

    private void unloadChunks(Set<Chunk> chunks) {
        DataManager dataManager = this.roseStacker.getManager(DataManager.class);

        if (this.stackManager.isEntityStackingEnabled()) {
            Map<UUID, StackedEntity> stackedEntities = this.stackedEntities.entrySet().stream().filter(x -> this.containsChunk(chunks, x.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // Restore custom names
            for (StackedEntity stackedEntity : stackedEntities.values())
                stackedEntity.getEntity().setCustomName(stackedEntity.getOriginalCustomName());

            dataManager.createOrUpdateStackedEntities(stackedEntities.values());
            stackedEntities.keySet().forEach(this.stackedEntities::remove);
        }

        if (this.stackManager.isItemStackingEnabled()) {
            Map<UUID, StackedItem> stackedItems = this.stackedItems.entrySet().stream().filter(x -> this.containsChunk(chunks, x.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            dataManager.createOrUpdateStackedItems(stackedItems.values());
            stackedItems.keySet().forEach(this.stackedItems::remove);
        }

        if (this.stackManager.isBlockStackingEnabled()) {
            Map<Block, StackedBlock> stackedBlocks = this.stackedBlocks.entrySet().stream().filter(x -> this.containsChunk(chunks, x.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            dataManager.createOrUpdateStackedBlocksOrSpawners(stackedBlocks.values());
            stackedBlocks.keySet().forEach(this.stackedBlocks::remove);
        }

        if (this.stackManager.isSpawnerStackingEnabled()) {
            Map<Block, StackedSpawner> stackedSpawners = this.stackedSpawners.entrySet().stream().filter(x -> this.containsChunk(chunks, x.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            dataManager.createOrUpdateStackedBlocksOrSpawners(stackedSpawners.values());
            stackedSpawners.keySet().forEach(this.stackedSpawners::remove);
        }
    }

    /**
     * @return the world that this StackingThread is acting on
     */
    public World getTargetWorld() {
        return this.targetWorld;
    }

}
