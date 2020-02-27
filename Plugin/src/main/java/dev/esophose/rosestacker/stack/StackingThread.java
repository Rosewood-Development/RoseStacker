package dev.esophose.rosestacker.stack;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.manager.DataManager;
import dev.esophose.rosestacker.manager.HologramManager;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.manager.StackSettingManager;
import dev.esophose.rosestacker.nms.NMSUtil;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import dev.esophose.rosestacker.stack.settings.ItemStackSettings;
import dev.esophose.rosestacker.utils.StackerUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.bukkit.entity.Flying;
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
    private final World targetWorld;
    private final BukkitTask task;

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
        this.targetWorld = targetWorld;
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this.roseStacker, this, 5L, Setting.STACK_FREQUENCY.getLong());

        this.stackedEntities = new ConcurrentHashMap<>();
        this.stackedItems = new ConcurrentHashMap<>();
        this.stackedBlocks = new ConcurrentHashMap<>();
        this.stackedSpawners = new ConcurrentHashMap<>();

        this.cleanupTimer = 0;

        // Load all existing stacks in the target world
        Set<Chunk> chunks = new HashSet<>(Arrays.asList(this.targetWorld.getLoadedChunks()));

        // Load stacks
        DataManager dataManager = this.roseStacker.getManager(DataManager.class);
        dataManager.getStackedEntities(chunks, stacks -> stacks.forEach(x -> this.stackedEntities.put(x.getEntity().getUniqueId(), x)));
        dataManager.getStackedItems(chunks, stacks -> stacks.forEach(x -> this.stackedItems.put(x.getItem().getUniqueId(), x)));
        dataManager.getStackedBlocks(chunks, stacks -> stacks.forEach(x -> this.stackedBlocks.put(x.getBlock(), x)));
        dataManager.getStackedSpawners(chunks, stacks -> stacks.forEach(x -> this.stackedSpawners.put(x.getSpawner().getBlock(), x)));
    }

    @Override
    public void run() {
        Set<Stack> removed = new HashSet<>();

        // Auto stack items
        for (StackedItem stackedItem : new HashSet<>(this.stackedItems.values())) {
            if (removed.contains(stackedItem))
                continue;

            Item item = stackedItem.getItem();
            if (item == null || !item.isValid()) {
                this.removeItemStack(stackedItem);
                continue;
            }

            StackedItem removedStack = this.tryStackItem(stackedItem);
            if (removedStack != null)
                removed.add(removedStack);
        }

        // Auto stack entities
        for (StackedEntity stackedEntity : new HashSet<>(this.stackedEntities.values())) {
            if (removed.contains(stackedEntity))
                continue;

            LivingEntity livingEntity = stackedEntity.getEntity();
            if (livingEntity == null || !livingEntity.isValid()) {
                this.removeEntityStack(stackedEntity);
                continue;
            }

            StackedEntity removedStack = this.tryStackEntity(stackedEntity);
            if (removedStack != null)
                removed.add(removedStack);
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
        dataManager.createOrUpdateStackedEntities(this.stackedEntities.values());
        dataManager.createOrUpdateStackedItems(this.stackedItems.values());
        dataManager.createOrUpdateStackedBlocksOrSpawners(this.stackedBlocks.values());
        dataManager.createOrUpdateStackedBlocksOrSpawners(this.stackedSpawners.values());

        this.task.cancel();
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

        stackedItem.getItem().setPickupDelay(30);
        stackedItem.getItem().setTicksLived(1);

        Item newItem = world.spawn(stackedItem.getLocation(), Item.class, (entity) -> {
            entity.setItemStack(newItemStack);
            entity.setPickupDelay(0);
        });

        StackedItem newStackedItem = new StackedItem(newSize, newItem);
        this.stackedItems.put(newItem.getUniqueId(), newStackedItem);
        stackedItem.increaseStackSize(-newSize);
        return newStackedItem;
    }

    @Override
    public StackedEntity createEntityStack(LivingEntity livingEntity, boolean tryStack) {
        if (!Setting.ENTITY_STACKING_ENABLED.getBoolean())
            return null;

        if (livingEntity instanceof Player || livingEntity instanceof ArmorStand)
            return null;

        StackedEntity newStackedEntity = new StackedEntity(livingEntity, Collections.synchronizedList(new LinkedList<>()));
        this.stackedEntities.put(livingEntity.getUniqueId(), newStackedEntity);

        if (tryStack)
            this.tryStackEntity(newStackedEntity);

        return newStackedEntity;
    }

    @Override
    public StackedItem createItemStack(Item item, boolean tryStack) {
        if (!Setting.ITEM_STACKING_ENABLED.getBoolean())
            return null;

        StackedItem newStackedItem = new StackedItem(item.getItemStack().getAmount(), item);
        this.stackedItems.put(item.getUniqueId(), newStackedItem);

        if (tryStack)
            this.tryStackItem(newStackedItem);

        return newStackedItem;
    }

    @Override
    public StackedBlock createBlockStack(Block block, int amount) {
        if (!Setting.BLOCK_STACKING_ENABLED.getBoolean() || !this.stackManager.isBlockTypeStackable(block))
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
        if (!Setting.SPAWNER_STACKING_ENABLED.getBoolean() || !this.stackManager.isSpawnerTypeStackable(creatureSpawner.getSpawnedType()))
            return null;

        StackedSpawner newStackedSpawner = new StackedSpawner(amount, creatureSpawner);
        this.stackedSpawners.put(block, newStackedSpawner);
        return newStackedSpawner;
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
        stackedItems.forEach(x -> this.stackedItems.put(x.getItem().getUniqueId(), x));

        this.stackManager.setEntityStackingTemporarilyDisabled(false);
    }

    @Override
    public void loadChunk(Chunk chunk) {
        DataManager dataManager = this.roseStacker.getManager(DataManager.class);

        Set<Chunk> singletonChunk = Collections.singleton(chunk);

        dataManager.getStackedEntities(singletonChunk, (stack) -> stack.forEach(x -> this.stackedEntities.put(x.getEntity().getUniqueId(), x)));
        dataManager.getStackedItems(singletonChunk, (stack) -> stack.forEach(x -> this.stackedItems.put(x.getItem().getUniqueId(), x)));
        dataManager.getStackedBlocks(singletonChunk, (stack) -> stack.forEach(x -> this.stackedBlocks.put(x.getBlock(), x)));
        dataManager.getStackedSpawners(singletonChunk, (stack) -> stack.forEach(x -> this.stackedSpawners.put(x.getSpawner().getBlock(), x)));
    }

    @Override
    public void unloadChunk(Chunk chunk) {
        DataManager dataManager = this.roseStacker.getManager(DataManager.class);

        Map<Block, StackedBlock> stackedBlocks = this.stackedBlocks.entrySet().stream().filter(x -> x.getValue().getLocation().getChunk() == chunk).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<UUID, StackedEntity> stackedEntities = this.stackedEntities.entrySet().stream().filter(x -> x.getValue().getLocation().getChunk() == chunk).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<UUID, StackedItem> stackedItems = this.stackedItems.entrySet().stream().filter(x -> x.getValue().getLocation().getChunk() == chunk).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<Block, StackedSpawner> stackedSpawners = this.stackedSpawners.entrySet().stream().filter(x -> x.getValue().getLocation().getChunk() == chunk).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Restore custom names
        for (StackedEntity stackedEntity : stackedEntities.values())
            stackedEntity.getEntity().setCustomName(stackedEntity.getOriginalCustomName());

        dataManager.createOrUpdateStackedBlocksOrSpawners(stackedBlocks.values());
        dataManager.createOrUpdateStackedEntities(stackedEntities.values());
        dataManager.createOrUpdateStackedItems(stackedItems.values());
        dataManager.createOrUpdateStackedBlocksOrSpawners(stackedSpawners.values());

        stackedBlocks.keySet().forEach(this.stackedBlocks::remove);
        stackedEntities.keySet().forEach(this.stackedEntities::remove);
        stackedItems.keySet().forEach(this.stackedItems::remove);
        stackedSpawners.keySet().forEach(this.stackedSpawners::remove);
    }

    /**
     * Tries to stack a StackedEntity with all other StackedEntities
     *
     * @param stackedEntity the StackedEntity to try to stack
     * @return a deleted StackedEntity, or null if none
     */
    private StackedEntity tryStackEntity(StackedEntity stackedEntity) {
        double maxEntityStackDistanceSqrd = Setting.ENTITY_MERGE_RADIUS.getDouble() * Setting.ENTITY_MERGE_RADIUS.getDouble();

        for (StackedEntity other : this.stackedEntities.values()) {
            if (stackedEntity == other
                    || other.getEntity() == null
                    || !other.getEntity().isValid()
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
            if (stackSettings == null)
                continue;

            if (stackSettings.canStackWith(stackedEntity, other, false)) {
                if (Setting.ENTITY_REQUIRE_LINE_OF_SIGHT.getBoolean() && !StackerUtils.hasLineOfSight(stackedEntity.getEntity(), other.getEntity(), 0.75, false))
                    continue;

                int minStackSize = stackSettings.getMinStackSize();
                if (minStackSize > 2) {
                    int nearbyEntities = 0;
                    if (!Setting.ENTITY_MERGE_ENTIRE_CHUNK.getBoolean()) {
                        for (StackedEntity nearbyStackedEntity : this.stackedEntities.values()) {
                            if (nearbyStackedEntity.getEntity().getType() == stackedEntity.getEntity().getType()
                                    && stackedEntity.getLocation().distanceSquared(nearbyStackedEntity.getLocation()) <= maxEntityStackDistanceSqrd
                                    && stackSettings.canStackWith(stackedEntity, nearbyStackedEntity, false))
                                nearbyEntities += nearbyStackedEntity.getStackSize();
                        }
                    } else {
                        for (StackedEntity nearbyStackedEntity : this.stackedEntities.values()) {
                            if (nearbyStackedEntity.getEntity().getType() == stackedEntity.getEntity().getType()
                                    && nearbyStackedEntity.getLocation().getChunk() == stackedEntity.getLocation().getChunk()
                                    && stackSettings.canStackWith(stackedEntity, nearbyStackedEntity, false))
                                nearbyEntities += nearbyStackedEntity.getStackSize();
                        }
                    }

                    if (nearbyEntities < minStackSize)
                        continue;
                }

                StackedEntity increased = this.getPreferredEntityStack(stackedEntity, other);
                StackedEntity removed = increased == stackedEntity ? other : stackedEntity;

                removed.getEntity().setCustomName(removed.getOriginalCustomName());
                increased.increaseStackSize(removed.getEntity());
                increased.increaseStackSize(removed.getStackedEntityNBTStrings());

                if (Bukkit.isPrimaryThread()) {
                    removed.getEntity().remove();
                } else {
                    Bukkit.getScheduler().runTask(this.roseStacker, removed.getEntity()::remove);
                }

                this.removeEntityStack(removed);

                return removed;
            }
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
        if (stackedItem.getItem().getPickupDelay() > stackedItem.getItem().getPickupDelay())
            return null;

        double maxItemStackDistanceSqrd = Setting.ITEM_MERGE_RADIUS.getDouble() * Setting.ITEM_MERGE_RADIUS.getDouble();

        for (StackedItem other : this.stackedItems.values()) {
            if (stackedItem == other
                    || !other.getItem().isValid()
                    || stackedItem.getLocation().getWorld() != other.getLocation().getWorld()
                    || !stackedItem.getItem().getItemStack().isSimilar(other.getItem().getItemStack())
                    || other.getItem().getPickupDelay() > other.getItem().getTicksLived()
                    || stackedItem.getStackSize() + other.getStackSize() > Setting.ITEM_MAX_STACK_SIZE.getInt()
                    || stackedItem.getLocation().distanceSquared(other.getLocation()) > maxItemStackDistanceSqrd)
                continue;

            // Check if we should merge the stacks
            ItemStackSettings stackSettings = this.stackSettingManager.getItemStackSettings(stackedItem.getItem());
            if (stackSettings == null)
                continue;

            if (stackSettings.canStackWith(stackedItem, other, false)) {
                StackedItem increased = this.getPreferredItemStack(stackedItem, other);
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

    /**
     * Gets the StackedEntity that two stacks should stack into
     *
     * @param stack1 the first StackedEntity
     * @param stack2 the second StackedEntity
     * @return the StackedEntity that should be stacked into
     */
    private StackedEntity getPreferredEntityStack(StackedEntity stack1, StackedEntity stack2) {
        Entity entity1 = stack1.getEntity();
        Entity entity2 = stack2.getEntity();

        if (Setting.ENTITY_STACK_FLYING_DOWNWARDS.getBoolean() && entity1 instanceof Flying)
            return entity1.getLocation().getY() < entity2.getLocation().getY() ? stack1 : stack2;

        if (stack1.getStackSize() == stack2.getStackSize())
            return entity1.getTicksLived() > entity2.getTicksLived() ? stack1 : stack2;

        return stack1.getStackSize() > stack2.getStackSize() ? stack1 : stack2;
    }

    /**
     * Gets the StackedItem that two stacks should stack into
     *
     * @param stack1 the first StackedItem
     * @param stack2 the second StackedItem
     * @return the StackedItem that should be stacked into
     */
    private StackedItem getPreferredItemStack(StackedItem stack1, StackedItem stack2) {
        Entity entity1 = stack1.getItem();
        Entity entity2 = stack2.getItem();

        if (stack1.getStackSize() == stack2.getStackSize())
            return entity1.getTicksLived() > entity2.getTicksLived() ? stack1 : stack2;

        return stack1.getStackSize() > stack2.getStackSize() ? stack1 : stack2;
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

    /**
     * @return the world that this StackingThread is acting on
     */
    public World getTargetWorld() {
        return this.targetWorld;
    }

}
