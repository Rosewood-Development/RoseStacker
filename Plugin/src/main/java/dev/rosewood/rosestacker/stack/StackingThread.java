package dev.rosewood.rosestacker.stack;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.event.EntityStackClearEvent;
import dev.rosewood.rosestacker.event.EntityStackEvent;
import dev.rosewood.rosestacker.event.EntityUnstackEvent;
import dev.rosewood.rosestacker.event.ItemStackClearEvent;
import dev.rosewood.rosestacker.event.ItemStackEvent;
import dev.rosewood.rosestacker.event.PreDropStackedItemsEvent;
import dev.rosewood.rosestacker.hook.NPCsHook;
import dev.rosewood.rosestacker.hook.WorldGuardHook;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import dev.rosewood.rosestacker.manager.HologramManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataEntry;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorage;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.ItemStackSettings;
import dev.rosewood.rosestacker.utils.DataUtils;
import dev.rosewood.rosestacker.utils.EntityUtils;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class StackingThread implements StackingLogic, AutoCloseable {

    private final static int CLEANUP_TIMER_TARGET = 10;
    private final static String NEW_METADATA = "RS_new";

    private final static Cache<UUID, Boolean> REMOVED_ENTITIES = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();

    private final RosePlugin rosePlugin;
    private final StackManager stackManager;
    private final EntityCacheManager entityCacheManager;
    private final HologramManager hologramManager;
    private final World targetWorld;

    private final BukkitTask entityStackTask, itemStackTask, nametagTask, hologramTask;

    private final Map<UUID, StackedEntity> stackedEntities;
    private final Map<UUID, StackedItem> stackedItems;
    private final Map<Chunk, StackChunkData> stackChunkData;

    private boolean entityStackSwitch;
    private int cleanupTimer;

    boolean dynamicEntityTags, dynamicItemTags;
    double entityDynamicViewRangeSqrd, itemDynamicViewRangeSqrd;
    boolean entityDynamicWallDetection, itemDynamicWallDetection;

    public StackingThread(RosePlugin rosePlugin, StackManager stackManager, World targetWorld) {
        this.rosePlugin = rosePlugin;
        this.stackManager = stackManager;
        this.entityCacheManager = this.rosePlugin.getManager(EntityCacheManager.class);
        this.hologramManager = this.rosePlugin.getManager(HologramManager.class);
        this.targetWorld = targetWorld;

        long entityStackDelay = (long) Math.max(1, Setting.STACK_FREQUENCY.getLong() / 2.0);
        this.entityStackTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.rosePlugin, this::stackEntities, 5L, entityStackDelay);
        this.itemStackTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.rosePlugin, this::stackItems, 5L, Setting.ITEM_STACK_FREQUENCY.getLong());
        this.nametagTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.rosePlugin, this::processNametags, 5L, Setting.NAMETAG_UPDATE_FREQUENCY.getLong());
        this.hologramTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.rosePlugin, this::updateHolograms, 5L, Setting.HOLOGRAM_UPDATE_FREQUENCY.getLong());

        this.stackedEntities = new ConcurrentHashMap<>();
        this.stackedItems = new ConcurrentHashMap<>();
        this.stackChunkData = new ConcurrentHashMap<>();

        this.cleanupTimer = 0;

        this.dynamicEntityTags = Setting.ENTITY_DISPLAY_TAGS.getBoolean() && Setting.ENTITY_DYNAMIC_TAG_VIEW_RANGE_ENABLED.getBoolean();
        this.dynamicItemTags = Setting.ITEM_DISPLAY_TAGS.getBoolean() && Setting.ITEM_DYNAMIC_TAG_VIEW_RANGE_ENABLED.getBoolean();

        double entityDynamicViewRange = Setting.ENTITY_DYNAMIC_TAG_VIEW_RANGE.getDouble();
        double itemDynamicViewRange = Setting.ITEM_DYNAMIC_TAG_VIEW_RANGE.getDouble();

        this.entityDynamicViewRangeSqrd = entityDynamicViewRange * entityDynamicViewRange;
        this.itemDynamicViewRangeSqrd = itemDynamicViewRange * itemDynamicViewRange;

        this.entityDynamicWallDetection = Setting.ENTITY_DYNAMIC_TAG_VIEW_RANGE_WALL_DETECTION_ENABLED.getBoolean();
        this.itemDynamicWallDetection = Setting.ITEM_DYNAMIC_TAG_VIEW_RANGE_WALL_DETECTION_ENABLED.getBoolean();

        NMSAdapter.getHandler().hijackRandomSource(targetWorld);

        // Load chunk data for all stacks in the world
        for (Chunk chunk : this.targetWorld.getLoadedChunks()) {
            this.loadChunkEntities(chunk, List.of(chunk.getEntities()));
            this.loadChunkBlocks(chunk);
        }

        // Disable AI for all existing stacks in the target world
        this.targetWorld.getLivingEntities().forEach(PersistentDataUtils::applyDisabledAi);
    }

    private void stackEntities() {
        boolean itemStackingEnabled = this.stackManager.isItemStackingEnabled();
        boolean entityStackingEnabled = this.stackManager.isEntityStackingEnabled();
        if (!entityStackingEnabled)
            return;

        // Auto unstack entities
        if (!this.stackManager.isEntityUnstackingTemporarilyDisabled()) {
            boolean minSplitIfLower = Setting.ENTITY_MIN_SPLIT_IF_LOWER.getBoolean();
            for (StackedEntity stackedEntity : this.stackedEntities.values()) {
                if (!stackedEntity.shouldStayStacked() && stackedEntity.getEntity().isValid()) {
                    ThreadUtils.runSync(() -> {
                        if (stackedEntity.getStackSize() > 1)
                            this.splitEntityStack(stackedEntity);
                    });
                } else if (minSplitIfLower && stackedEntity.getStackSize() < stackedEntity.getStackSettings().getMinStackSize()) {
                    NMSHandler nmsHandler = NMSAdapter.getHandler();
                    StackedEntityDataStorage nbt = stackedEntity.getDataStorage();
                    stackedEntity.setDataStorage(nmsHandler.createEntityDataStorage(stackedEntity.getEntity(), this.stackManager.getEntityDataStorageType()));
                    ThreadUtils.runSync(() -> {
                        for (StackedEntityDataEntry<?> stackedEntityDataEntry : nbt.getAll())
                            nmsHandler.createEntityFromNBT(stackedEntityDataEntry, stackedEntity.getLocation(), true, stackedEntity.getEntity().getType());
                    });
                }
            }
        }

        // Auto stack entities
        if (this.entityStackSwitch) {
            for (StackedEntity stackedEntity : this.stackedEntities.values()) {
                LivingEntity livingEntity = stackedEntity.getEntity();
                if (this.isRemoved(livingEntity)) {
                    this.removeEntityStack(stackedEntity);
                    continue;
                }

                this.tryStackEntity(stackedEntity);
            }
        }

        // Run entity stacking half as often as the unstacking
        this.entityStackSwitch = !this.entityStackSwitch;

        // Cleans up entities/items that aren't stacked
        this.cleanupTimer++;
        if (this.cleanupTimer >= CLEANUP_TIMER_TARGET) {
            ThreadUtils.runSync(() -> {
                for (Entity entity : this.targetWorld.getEntities()) {
                    if (this.isRemoved(entity))
                        continue;

                    if (entity instanceof LivingEntity livingEntity && entity.getType() != EntityType.ARMOR_STAND && entity.getType() != EntityType.PLAYER && !this.isEntityStacked(livingEntity)) {
                        this.createEntityStack(livingEntity, false);
                    } else if (itemStackingEnabled && entity.getType() == EntityType.DROPPED_ITEM) {
                        Item item = (Item) entity;
                        if (!this.isItemStacked(item))
                            this.createItemStack(item, false);
                    }
                }
            });
            this.cleanupTimer = 0;
        }
    }

    private void stackItems() {
        boolean itemStackingEnabled = this.stackManager.isItemStackingEnabled();
        if (!itemStackingEnabled)
            return;

        // Auto stack items
        for (StackedItem stackedItem : this.stackedItems.values()) {
            Item item = stackedItem.getItem();
            if (item == null || this.isRemoved(item)) {
                this.removeItemStack(stackedItem);
                continue;
            }

            this.tryStackItem(stackedItem);
        }
    }

    public void processNametags() {
        if (!this.dynamicEntityTags && !this.dynamicItemTags)
            return;

        List<Player> players = this.targetWorld.getPlayers();
        if (players.isEmpty())
            return;

        // Handle dynamic stack tags
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        Set<EntityType> validEntities = StackerUtils.getStackableEntityTypes();
        boolean displaySingleEntityTags = Setting.ENTITY_DISPLAY_TAGS_SINGLE.getBoolean();
        boolean displaySingleItemTags = Setting.ITEM_DISPLAY_TAGS_SINGLE.getBoolean();

        List<Entity> entities = new ArrayList<>();
        entities.addAll(this.stackedEntities.values().stream()
                .filter(x -> x.getStackSize() > 1 || displaySingleEntityTags)
                .map(StackedEntity::getEntity)
                .filter(Objects::nonNull)
                .filter(x -> validEntities.contains(x.getType()))
                .toList());
        entities.addAll(this.stackedItems.values().stream()
                .filter(x -> x.getStackSize() > 1 || displaySingleItemTags)
                .map(StackedItem::getItem)
                .toList());

        for (Player player : players) {
            if (player.getWorld() != this.targetWorld)
                continue;

            ItemStack itemStack = player.getInventory().getItemInMainHand();
            boolean displayStackingToolParticles = ItemUtils.isStackingTool(itemStack);

            for (Entity entity : entities) {
                if (entity.getType() == EntityType.PLAYER)
                    continue;

                if ((entity.getType() == EntityType.DROPPED_ITEM || entity.getType() == EntityType.ARMOR_STAND)
                        && (entity.getCustomName() == null || !entity.isCustomNameVisible()))
                    continue;

                double distanceSqrd;
                try { // The locations can end up comparing cross-world if the player/entity switches worlds mid-loop due to being async
                    distanceSqrd = player.getLocation().distanceSquared(entity.getLocation());
                } catch (Exception e) {
                    continue;
                }

                if (distanceSqrd > StackerUtils.ASSUMED_ENTITY_VISIBILITY_RANGE)
                    continue;

                boolean visible;
                if (this.dynamicItemTags && entity.getType() == EntityType.DROPPED_ITEM) {
                    visible = distanceSqrd < this.itemDynamicViewRangeSqrd;
                    if (this.itemDynamicWallDetection)
                        visible &= EntityUtils.hasLineOfSight(player, entity, 0.75, true);
                } else if (this.dynamicEntityTags) {
                     visible = distanceSqrd < this.entityDynamicViewRangeSqrd;
                     if (this.entityDynamicWallDetection)
                         visible &= EntityUtils.hasLineOfSight(player, entity, 0.75, true);
                 } else continue;

                if (entity.getType() != EntityType.ARMOR_STAND && entity instanceof LivingEntity livingEntity) {
                    StackedEntity stackedEntity = this.getStackedEntity(livingEntity);
                    if (stackedEntity != null)
                        nmsHandler.updateEntityNameTagForPlayer(player, entity, stackedEntity.getDisplayName(), stackedEntity.isDisplayNameVisible() && visible);

                    // Spawn particles for holding the stacking tool
                    if (visible && displayStackingToolParticles) {
                        Location location = entity.getLocation().add(0, livingEntity.getEyeHeight(true) + 0.75, 0);
                        DustOptions dustOptions;
                        if (PersistentDataUtils.isUnstackable(livingEntity)) {
                            dustOptions = StackerUtils.UNSTACKABLE_DUST_OPTIONS;
                        } else {
                            dustOptions = StackerUtils.STACKABLE_DUST_OPTIONS;
                        }
                        player.spawnParticle(Particle.REDSTONE, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
                    }
                } else {
                    nmsHandler.updateEntityNameTagVisibilityForPlayer(player, entity, visible);
                }
            }
        }
    }

    private void updateHolograms() {
        this.stackChunkData.values().stream().flatMap(x -> x.getSpawners().values().stream()).forEach(StackedSpawner::updateDisplay);
    }

    @Override
    public void close() {
        // Cancel tasks
        if (this.entityStackTask != null)
            this.entityStackTask.cancel();

        if (this.itemStackTask != null)
            this.itemStackTask.cancel();

        if (this.nametagTask != null)
            this.nametagTask.cancel();

        if (this.hologramTask != null)
            this.hologramTask.cancel();
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
        Map<Block, StackedBlock> stackedBlocks = new HashMap<>();
        for (StackChunkData stackChunkData : this.stackChunkData.values())
            stackedBlocks.putAll(stackChunkData.getBlocks());
        return stackedBlocks;
    }

    @Override
    public Map<Block, StackedSpawner> getStackedSpawners() {
        Map<Block, StackedSpawner> stackedSpawners = new HashMap<>();
        for (StackChunkData stackChunkData : this.stackChunkData.values())
            stackedSpawners.putAll(stackChunkData.getSpawners());
        return stackedSpawners;
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
        StackChunkData stackChunkData = this.stackChunkData.get(block.getChunk());
        if (stackChunkData == null)
            return null;
        return stackChunkData.getBlock(block);
    }

    @Override
    public StackedSpawner getStackedSpawner(Block block) {
        StackChunkData stackChunkData = this.stackChunkData.get(block.getChunk());
        if (stackChunkData == null)
            return null;
        return stackChunkData.getSpawner(block);
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
            this.stackedEntities.remove(key);
            this.setRemoved(entity);
        } else {
            // Entity is null so we have to remove by value instead
            for (Entry<UUID, StackedEntity> entry : this.stackedEntities.entrySet()) {
                if (entry.getValue() == stackedEntity) {
                    this.stackedEntities.remove(entry.getKey());
                    break;
                }
            }
        }
    }

    @Override
    public void removeItemStack(StackedItem stackedItem) {
        Item item = stackedItem.getItem();
        if (item != null) {
            UUID key = stackedItem.getItem().getUniqueId();
            this.stackedItems.remove(key);
            this.setRemoved(item);
        } else {
            // Item is null so we have to remove by value instead
            for (Entry<UUID, StackedItem> entry : this.stackedItems.entrySet()) {
                if (entry.getValue() == stackedItem) {
                    this.stackedItems.remove(entry.getKey());
                    break;
                }
            }
        }
    }

    @Override
    public void removeBlockStack(StackedBlock stackedBlock) {
        Block key = stackedBlock.getBlock();
        stackedBlock.kickOutGuiViewers();

        StackChunkData stackChunkData = this.stackChunkData.get(key.getChunk());
        if (stackChunkData != null)
            stackChunkData.removeBlock(stackedBlock);
    }

    @Override
    public void removeSpawnerStack(StackedSpawner stackedSpawner) {
        Block key = stackedSpawner.getBlock();
        stackedSpawner.kickOutGuiViewers();

        StackChunkData stackChunkData = this.stackChunkData.get(key.getChunk());
        if (stackChunkData != null)
            stackChunkData.removeSpawner(stackedSpawner);
    }

    @Override
    public int removeAllEntityStacks() {
        List<StackedEntity> toRemove = this.stackedEntities.values().stream()
                .filter(x -> x.getEntity() != null && x.getEntity().getType() != EntityType.PLAYER)
                .filter(x -> x.getStackSize() != 1 || Setting.MISC_CLEARALL_REMOVE_SINGLE.getBoolean())
                .toList();

        EntityStackClearEvent entityStackClearEvent = new EntityStackClearEvent(this.targetWorld, toRemove);
        Bukkit.getPluginManager().callEvent(entityStackClearEvent);
        if (entityStackClearEvent.isCancelled())
            return 0;

        toRemove.stream().map(StackedEntity::getEntity).forEach(this::setRemoved);
        toRemove.stream().map(StackedEntity::getEntity).forEach(LivingEntity::remove);
        this.stackedEntities.values().removeIf(toRemove::contains);

        return toRemove.size();
    }

    @Override
    public int removeAllItemStacks() {
        List<StackedItem> toRemove = new ArrayList<>(this.stackedItems.values());

        ItemStackClearEvent itemStackClearEvent = new ItemStackClearEvent(this.targetWorld, toRemove);
        Bukkit.getPluginManager().callEvent(itemStackClearEvent);
        if (itemStackClearEvent.isCancelled())
            return 0;

        toRemove.stream().map(StackedItem::getItem).forEach(this::setRemoved);
        toRemove.stream().map(StackedItem::getItem).forEach(Item::remove);
        this.stackedItems.values().removeIf(toRemove::contains);

        return toRemove.size();
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
        EntityUnstackEvent entityUnstackEvent = new EntityUnstackEvent(stackedEntity, new StackedEntity(stackedEntity.getEntity()));
        Bukkit.getPluginManager().callEvent(entityUnstackEvent);
        if (entityUnstackEvent.isCancelled())
            return null;

        StackedEntity newlySplit = stackedEntity.decreaseStackSize();
        this.stackedEntities.put(newlySplit.getEntity().getUniqueId(), newlySplit);
        this.tryStackEntity(newlySplit);
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
        stackedItem.increaseStackSize(-newSize, true);
        return newStackedItem;
    }

    @Override
    public StackedEntity createEntityStack(LivingEntity livingEntity, boolean tryStack) {
        if (!this.stackManager.isEntityStackingEnabled())
            return null;

        if (livingEntity instanceof Player || livingEntity instanceof ArmorStand || NPCsHook.isNPC(livingEntity))
            return null;

        StackedEntity newStackedEntity = new StackedEntity(livingEntity);
        this.stackedEntities.put(livingEntity.getUniqueId(), newStackedEntity);

        if (tryStack && Setting.ENTITY_INSTANT_STACK.getBoolean()) {
            livingEntity.setMetadata(NEW_METADATA, new FixedMetadataValue(this.rosePlugin, true));
            this.tryStackEntity(newStackedEntity);
            livingEntity.removeMetadata(NEW_METADATA, this.rosePlugin);
        }

        return newStackedEntity;
    }

    @Override
    public StackedItem createItemStack(Item item, boolean tryStack) {
        if (!this.stackManager.isItemStackingEnabled())
            return null;

        ItemStackSettings itemStackSettings = this.rosePlugin.getManager(StackSettingManager.class).getItemStackSettings(item);
        if (itemStackSettings != null && !itemStackSettings.isStackingEnabled())
            return null;

        StackedItem newStackedItem = new StackedItem(item.getItemStack().getAmount(), item);
        this.stackedItems.put(item.getUniqueId(), newStackedItem);

        if (tryStack) {
            item.setMetadata(NEW_METADATA, new FixedMetadataValue(this.rosePlugin, true));
            this.tryStackItem(newStackedItem);
            item.removeMetadata(NEW_METADATA, this.rosePlugin);
        }

        return newStackedItem;
    }

    @Override
    public StackedBlock createBlockStack(Block block, int amount) {
        if (!this.stackManager.isBlockStackingEnabled() || !this.stackManager.isBlockTypeStackable(block))
            return null;

        StackedBlock newStackedBlock = new StackedBlock(amount, block);

        StackChunkData stackChunkData = this.stackChunkData.get(block.getChunk());
        if (stackChunkData == null) {
            stackChunkData = new StackChunkData(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
            this.stackChunkData.put(block.getChunk(), stackChunkData);
        }

        stackChunkData.addBlock(newStackedBlock);
        return newStackedBlock;
    }

    @Override
    public StackedSpawner createSpawnerStack(Block block, int amount, boolean placedByPlayer) {
        if (block.getType() != Material.SPAWNER)
            return null;

        CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
        if (!this.stackManager.isSpawnerStackingEnabled() || !this.stackManager.isSpawnerTypeStackable(creatureSpawner.getSpawnedType()))
            return null;

        StackedSpawner newStackedSpawner = new StackedSpawner(amount, block, placedByPlayer);

        StackChunkData stackChunkData = this.stackChunkData.get(block.getChunk());
        if (stackChunkData == null) {
            stackChunkData = new StackChunkData(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
            this.stackChunkData.put(block.getChunk(), stackChunkData);
        }

        stackChunkData.addSpawner(newStackedSpawner);
        return newStackedSpawner;
    }

    @Override
    public void addEntityStack(StackedEntity stackedEntity) {
        if (!this.stackManager.isEntityStackingEnabled() || NPCsHook.isNPC(stackedEntity.getEntity()))
            return;

        this.stackedEntities.put(stackedEntity.getEntity().getUniqueId(), stackedEntity);

        if (Setting.ENTITY_INSTANT_STACK.getBoolean())
            this.tryStackEntity(stackedEntity);
    }

    @Override
    public void addItemStack(StackedItem stackedItem) {
        if (!this.stackManager.isItemStackingEnabled())
            return;

        this.stackedItems.put(stackedItem.getItem().getUniqueId(), stackedItem);
        this.tryStackItem(stackedItem);
    }

    @Override
    public void preStackEntities(EntityType entityType, int amount, Location location, SpawnReason spawnReason) {
        World world = location.getWorld();
        if (world == null)
            return;

        ThreadUtils.runAsync(() -> {
            EntityStackSettings stackSettings = this.rosePlugin.getManager(StackSettingManager.class).getEntityStackSettings(entityType);
            NMSHandler nmsHandler = NMSAdapter.getHandler();
            boolean removeAi = Setting.ENTITY_DISABLE_ALL_MOB_AI.getBoolean();

            Collection<Entity> nearbyEntities = this.entityCacheManager.getNearbyEntities(location, stackSettings.getMergeRadius(), x -> x.getType() == entityType);
            Set<StackedEntity> nearbyStackedEntities = new HashSet<>();
            for (Entity entity : nearbyEntities) {
                StackedEntity stackedEntity = this.stackManager.getStackedEntity((LivingEntity) entity);
                if (stackedEntity != null)
                    nearbyStackedEntities.add(stackedEntity);
            }

            Set<StackedEntity> newStackedEntities = new HashSet<>();
            switch (this.stackManager.getEntityDataStorageType()) {
                case NBT -> {
                    for (int i = 0; i < amount; i++) {
                        StackedEntity newStack = this.createNewEntity(nmsHandler, entityType, location, spawnReason, removeAi);
                        Optional<StackedEntity> matchingEntity = nearbyStackedEntities.stream().filter(x ->
                                stackSettings.testCanStackWith(x, newStack, false, true)).findFirst();
                        if (matchingEntity.isPresent()) {
                            matchingEntity.get().increaseStackSize(newStack.getEntity(), false);
                        } else {
                            nearbyStackedEntities.add(newStack);
                            newStackedEntities.add(newStack);
                        }
                    }
                }

                case SIMPLE -> {
                    for (int i = amount; i > 0; i--) {
                        Optional<StackedEntity> matchingEntity = nearbyStackedEntities.stream().filter(x -> stackSettings.testCanStackWith(x, x, false, true)).findFirst();
                        if (matchingEntity.isPresent()) {
                            // Increase stack size by as much as we can
                            int amountToIncrease = Math.min(i, stackSettings.getMaxStackSize() - matchingEntity.get().getStackSize());
                            matchingEntity.get().increaseStackSize(amountToIncrease, false);
                            i -= amountToIncrease;
                        } else {
                            StackedEntity newStack = this.createNewEntity(nmsHandler, entityType, location, spawnReason, removeAi);
                            nearbyStackedEntities.add(newStack);
                            newStackedEntities.add(newStack);
                        }
                    }
                }
            }

            ThreadUtils.runSync(() -> {
                this.stackManager.setEntityStackingTemporarilyDisabled(true);
                for (StackedEntity stackedEntity : newStackedEntities) {
                    LivingEntity entity = stackedEntity.getEntity();
                    this.entityCacheManager.preCacheEntity(entity);
                    nmsHandler.spawnExistingEntity(stackedEntity.getEntity(), spawnReason, Setting.SPAWNER_BYPASS_REGION_SPAWNING_RULES.getBoolean());
                    if (removeAi)
                        PersistentDataUtils.removeEntityAi(entity);
                    entity.setVelocity(Vector.getRandom().multiply(0.01));
                    this.addEntityStack(stackedEntity);
                    stackedEntity.updateDisplay();
                }
                this.stackManager.setEntityStackingTemporarilyDisabled(false);
            });
        });
    }

    private StackedEntity createNewEntity(NMSHandler nmsHandler, EntityType entityType, Location location, SpawnReason spawnReason, boolean removeAi) {
        LivingEntity entity = nmsHandler.createNewEntityUnspawned(entityType, location, spawnReason);
        if (removeAi)
            PersistentDataUtils.removeEntityAi(entity);

        return new StackedEntity(entity);
    }

    @Override
    public void preStackEntities(EntityType entityType, int amount, Location location) {
        this.preStackEntities(entityType, amount, location, SpawnReason.CUSTOM);
    }

    @Override
    public void preStackItems(Collection<ItemStack> items, Location location) {
        if (location.getWorld() == null)
            return;

        // Merge items and store their amounts
        Map<ItemStack, Integer> itemStackAmounts = ItemUtils.reduceItemsByCounts(items);

        // Fire the event to allow other plugins to manipulate the items before we stack and drop them
        PreDropStackedItemsEvent event = new PreDropStackedItemsEvent(itemStackAmounts, location);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        if (itemStackAmounts.isEmpty())
            return;

        // If stacking is disabled, drop the items separated by their max stack size
        if (!this.stackManager.isItemStackingEnabled()) {
            for (Map.Entry<ItemStack, Integer> entry : itemStackAmounts.entrySet()) {
                ItemStack itemStack = entry.getKey();
                int amount = entry.getValue();
                while (amount > 0) {
                    int maxStackSize = itemStack.getMaxStackSize();
                    int stackSize = Math.min(amount, maxStackSize);
                    amount -= stackSize;
                    ItemStack toDrop = itemStack.clone();
                    toDrop.setAmount(stackSize);
                    location.getWorld().dropItemNaturally(location, toDrop);
                }
            }
            return;
        }

        // Drop all the items stacked with the correct amounts
        this.stackManager.setEntityStackingTemporarilyDisabled(true);

        for (Map.Entry<ItemStack, Integer> entry : itemStackAmounts.entrySet()) {
            if (entry.getValue() <= 0)
                continue;

            Item item = location.getWorld().dropItemNaturally(location, entry.getKey());
            StackedItem stackedItem = new StackedItem(entry.getValue(), item);
            this.addItemStack(stackedItem);
            stackedItem.updateDisplay();
        }

        this.stackManager.setEntityStackingTemporarilyDisabled(false);
    }

    @Override
    public StackedItem dropItemStack(ItemStack itemStack, int amount, Location location, boolean dropNaturally) {
        if (location.getWorld() == null)
            return null;

        this.stackManager.setEntityStackingTemporarilyDisabled(true);

        Item item;
        if (dropNaturally) {
            item = location.getWorld().dropItemNaturally(location, itemStack);
        } else {
            item = location.getWorld().dropItem(location, itemStack);
        }

        StackedItem stackedItem = this.createItemStack(item, false);
        if (stackedItem != null)
            stackedItem.setStackSize(amount);

        this.stackManager.setEntityStackingTemporarilyDisabled(false);

        return stackedItem;
    }

    @Override
    public void loadChunkBlocks(Chunk chunk) {
        if (!chunk.isLoaded())
            return;

        Map<Block, StackedSpawner> stackedSpawners = new ConcurrentHashMap<>();
        if (this.stackManager.isSpawnerStackingEnabled())
            for (StackedSpawner stackedSpawner : DataUtils.readStackedSpawners(chunk))
                stackedSpawners.put(stackedSpawner.getBlock(), stackedSpawner);

        Map<Block, StackedBlock> stackedBlocks = new ConcurrentHashMap<>();
        if (this.stackManager.isBlockStackingEnabled())
            for (StackedBlock stackedBlock : DataUtils.readStackedBlocks(chunk))
                stackedBlocks.put(stackedBlock.getBlock(), stackedBlock);

        if (!stackedSpawners.isEmpty() || !stackedBlocks.isEmpty())
            this.stackChunkData.put(chunk, new StackChunkData(stackedSpawners, stackedBlocks));
    }

    @Override
    public void loadChunkEntities(Chunk chunk, List<Entity> entities) {
        if (entities.isEmpty())
            return;

        if (this.stackManager.isEntityStackingEnabled()) {
            for (Entity entity : entities) {
                if (!(entity instanceof LivingEntity livingEntity) || entity.getType() == EntityType.ARMOR_STAND || entity.getType() == EntityType.PLAYER)
                    continue;

                StackedEntity stackedEntity = DataUtils.readStackedEntity(livingEntity, this.stackManager.getEntityDataStorageType());
                if (stackedEntity != null) {
                    this.stackedEntities.put(stackedEntity.getEntity().getUniqueId(), stackedEntity);
                } else {
                    this.createEntityStack(livingEntity, true);
                }
            }
        }

        if (this.stackManager.isItemStackingEnabled()) {
            for (Entity entity : entities) {
                if (entity.getType() != EntityType.DROPPED_ITEM)
                    continue;

                Item item = (Item) entity;
                StackedItem stackedItem = DataUtils.readStackedItem(item);
                if (stackedItem != null) {
                    this.stackedItems.put(stackedItem.getItem().getUniqueId(), stackedItem);
                } else {
                    this.createItemStack(item, true);
                }
            }
        }
    }

    @Override
    public void saveChunkBlocks(Chunk chunk, boolean clearStored) {
        StackChunkData stackChunkData = this.stackChunkData.get(chunk);
        if (stackChunkData == null)
            return;

        if (this.stackManager.isSpawnerStackingEnabled()) {
            DataUtils.writeStackedSpawners(stackChunkData.getSpawners().values(), chunk);
            if (clearStored)
                stackChunkData.getSpawners().values().stream().map(StackedSpawner::getHologramLocation).forEach(this.hologramManager::deleteHologram);
        }

        if (this.stackManager.isBlockStackingEnabled()) {
            DataUtils.writeStackedBlocks(stackChunkData.getBlocks().values(), chunk);
            if (clearStored)
                stackChunkData.getBlocks().values().stream().map(StackedBlock::getHologramLocation).forEach(this.hologramManager::deleteHologram);
        }

        if (clearStored)
            this.stackChunkData.remove(chunk);
    }

    @Override
    public void saveChunkEntities(Chunk chunk, List<Entity> entities, boolean clearStored) {
        if (this.stackManager.isEntityStackingEnabled()) {
            List<StackedEntity> stackedEntities = entities.stream()
                    .filter(x -> x instanceof LivingEntity && x.getType() != EntityType.ARMOR_STAND && x.getType() != EntityType.PLAYER)
                    .map(x -> this.stackedEntities.get(x.getUniqueId()))
                    .filter(Objects::nonNull)
                    .toList();

            stackedEntities.forEach(DataUtils::writeStackedEntity);

            if (clearStored)
                stackedEntities.stream().map(StackedEntity::getEntity).map(Entity::getUniqueId).forEach(this.stackedEntities::remove);
        }

        if (this.stackManager.isItemStackingEnabled()) {
            List<StackedItem> stackedItems = entities.stream()
                    .filter(x -> x.getType() == EntityType.DROPPED_ITEM)
                    .map(x -> this.stackedItems.get(x.getUniqueId()))
                    .filter(Objects::nonNull)
                    .toList();

            stackedItems.forEach(DataUtils::writeStackedItem);

            if (clearStored)
                stackedItems.stream().map(StackedItem::getItem).map(Entity::getUniqueId).forEach(this.stackedItems::remove);
        }
    }

    /**
     * Tries to stack a StackedEntity with all other StackedEntities
     *
     * @param stackedEntity the StackedEntity to try to stack
     */
    private void tryStackEntity(StackedEntity stackedEntity) {
        EntityStackSettings stackSettings = stackedEntity.getStackSettings();
        if (stackSettings == null)
            return;

        if (stackedEntity.checkNPC()) {
            this.removeEntityStack(stackedEntity);
            return;
        }

        LivingEntity entity = stackedEntity.getEntity();
        if (this.isRemoved(entity))
            return;

        if (!WorldGuardHook.testLocation(entity.getLocation()))
            return;

        Collection<Entity> nearbyEntities;
        Predicate<Entity> predicate = x -> x.getType() == entity.getType();
        if (!Setting.ENTITY_MERGE_ENTIRE_CHUNK.getBoolean()) {
            nearbyEntities = this.entityCacheManager.getNearbyEntities(entity.getLocation(), stackSettings.getMergeRadius(), predicate);
        } else {
            nearbyEntities = this.entityCacheManager.getEntitiesInChunk(entity.getLocation(), predicate);
        }

        Set<StackedEntity> targetEntities = new HashSet<>();
        targetEntities.add(stackedEntity);

        for (Entity otherEntity : nearbyEntities) {
            if (entity == otherEntity || this.isRemoved(otherEntity))
                continue;

            StackedEntity other = this.stackedEntities.get(otherEntity.getUniqueId());
            if (other == null)
                continue;

            if (stackSettings.testCanStackWith(stackedEntity, other, false)
                    && (!Setting.ENTITY_REQUIRE_LINE_OF_SIGHT.getBoolean() || EntityUtils.hasLineOfSight(entity, otherEntity, 0.75, false))
                    && WorldGuardHook.testLocation(otherEntity.getLocation()))
                targetEntities.add(other);
        }

        StackedEntity increased;
        int totalSize;
        List<StackedEntity> removable = new ArrayList<>(targetEntities.size());
        if (!Setting.ENTITY_MIN_STACK_COUNT_ONLY_INDIVIDUALS.getBoolean()) {
            increased = targetEntities.stream().max(StackedEntity::compareTo).orElse(stackedEntity);
            targetEntities.remove(increased);
            totalSize = increased.getStackSize();
            for (StackedEntity target : targetEntities) {
                if (totalSize + target.getStackSize() <= stackSettings.getMaxStackSize()) {
                    totalSize += target.getStackSize();
                    removable.add(target);
                }
            }
        } else {
            increased = stackedEntity;
            targetEntities.remove(increased);
            totalSize = 1;
            int totalStackSize = increased.getStackSize();
            for (StackedEntity target : targetEntities) {
                if (totalStackSize + target.getStackSize() <= stackSettings.getMaxStackSize()) {
                    totalSize++;
                    totalStackSize += target.getStackSize();
                    removable.add(target);
                }
            }
        }

        if (removable.isEmpty() || totalSize < stackSettings.getMinStackSize())
            return;

        EntityStackEvent entityStackEvent = new EntityStackEvent(removable, increased);
        Bukkit.getPluginManager().callEvent(entityStackEvent);
        if (entityStackEvent.isCancelled())
            return;

        for (StackedEntity toStack : removable) {
            stackSettings.applyStackProperties(toStack.getEntity(), increased.getEntity());
            increased.increaseStackSize(toStack.getEntity());
            increased.increaseStackSize(toStack.getDataStorage());
            this.removeEntityStack(toStack);
        }

        Runnable removeTask = () -> removable.stream().map(StackedEntity::getEntity).forEach(Entity::remove);
        if (Bukkit.isPrimaryThread()) {
            removeTask.run();
        } else {
            ThreadUtils.runSync(removeTask);
        }
    }

    /**
     * Tries to stack a StackedItem with all other StackedItems
     *
     * @param stackedItem the StackedItem to try to stack
     */
    private void tryStackItem(StackedItem stackedItem) {
        ItemStackSettings stackSettings = stackedItem.getStackSettings();
        if (stackSettings == null
                || !stackSettings.isStackingEnabled()
                || stackedItem.getItem().getPickupDelay() > 40)
            return;

        Item item = stackedItem.getItem();
        if (this.isRemoved(item))
            return;

        Predicate<Entity> predicate = x -> x.getType() == EntityType.DROPPED_ITEM;
        Set<Item> nearbyItems = this.entityCacheManager.getNearbyEntities(stackedItem.getLocation(), Setting.ITEM_MERGE_RADIUS.getDouble(), predicate)
                .stream()
                .map(x -> (Item) x)
                .collect(Collectors.toSet());

        Set<StackedItem> targetItems = new HashSet<>();
        for (Item otherItem : nearbyItems) {
            if (item == otherItem || otherItem.getPickupDelay() > 40 || !item.getItemStack().isSimilar(otherItem.getItemStack()) || this.isRemoved(otherItem))
                continue;

            StackedItem other = this.stackedItems.get(otherItem.getUniqueId());
            if (other != null)
                targetItems.add(other);
        }

        int totalSize = stackedItem.getStackSize();
        Set<StackedItem> removable = new HashSet<>();
        for (StackedItem target : targetItems) {
            if (totalSize + target.getStackSize() <= stackSettings.getMaxStackSize()) {
                totalSize += target.getStackSize();
                removable.add(target);
            }
        }

        StackedItem headStack = stackedItem;
        for (StackedItem other : removable) {
            StackedItem increased = headStack.compareTo(other) > 0 ? headStack : other;
            StackedItem removed = increased == headStack ? other : headStack;

            headStack = increased;

            ItemStackEvent itemStackEvent = new ItemStackEvent(removed, increased);
            Bukkit.getPluginManager().callEvent(itemStackEvent);
            if (itemStackEvent.isCancelled())
                continue;

            increased.increaseStackSize(removed.getStackSize(), true);
            if (Setting.ITEM_RESET_DESPAWN_TIMER_ON_MERGE.getBoolean())
                increased.getItem().setTicksLived(1); // Reset the 5 minute pickup timer
            removed.getItem().setPickupDelay(100); // Don't allow the item we just merged to get picked up or stacked
            increased.getItem().setPickupDelay(5);

            Runnable removeTask = () -> removed.getItem().remove();
            if (Bukkit.isPrimaryThread()) {
                removeTask.run();
            } else {
                ThreadUtils.runSync(removeTask);
            }

            this.removeItemStack(removed);
        }
    }

    public void transferExistingEntityStack(UUID entityUUID, StackedEntity stackedEntity, StackingThread toThread) {
        this.stackedEntities.remove(entityUUID);
        toThread.loadExistingEntityStack(entityUUID, stackedEntity);
    }

    public void transferExistingEntityStack(UUID entityUUID, StackedItem stackedItem, StackingThread toThread) {
        this.stackedEntities.remove(entityUUID);
        toThread.loadExistingItemStack(entityUUID, stackedItem);
    }

    private void loadExistingEntityStack(UUID entityUUID, StackedEntity stackedEntity) {
        stackedEntity.updateEntity();
        this.stackedEntities.put(entityUUID, stackedEntity);
    }

    private void loadExistingItemStack(UUID entityUUID, StackedItem stackedItem) {
        stackedItem.updateItem();
        this.stackedItems.put(entityUUID, stackedItem);
    }

    private boolean isRemoved(Entity entity) {
        return entity == null || (!entity.isValid() && !entity.hasMetadata(NEW_METADATA)) || REMOVED_ENTITIES.getIfPresent(entity.getUniqueId()) != null;
    }

    private void setRemoved(Entity entity) {
        REMOVED_ENTITIES.put(entity.getUniqueId(), true);
    }

    /**
     * @return the world that this StackingThread is acting on
     */
    public World getTargetWorld() {
        return this.targetWorld;
    }

}
