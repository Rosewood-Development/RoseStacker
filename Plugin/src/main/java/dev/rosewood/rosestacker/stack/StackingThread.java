package dev.rosewood.rosestacker.stack;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.compatibility.CompatibilityAdapter;
import dev.rosewood.rosegarden.scheduler.task.ScheduledTask;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.event.EntityStackClearEvent;
import dev.rosewood.rosestacker.event.EntityStackEvent;
import dev.rosewood.rosestacker.event.EntityUnstackEvent;
import dev.rosewood.rosestacker.event.ItemStackClearEvent;
import dev.rosewood.rosestacker.event.ItemStackEvent;
import dev.rosewood.rosestacker.event.PreDropStackedItemsEvent;
import dev.rosewood.rosestacker.hook.NPCsHook;
import dev.rosewood.rosestacker.hook.WorldGuardHook;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import dev.rosewood.rosestacker.manager.HologramManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.storage.EntityDataEntry;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorage;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.ItemStackSettings;
import dev.rosewood.rosestacker.utils.DataUtils;
import dev.rosewood.rosestacker.utils.EntityUtils;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import dev.rosewood.rosestacker.utils.VersionUtils;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.bukkit.util.Vector;

public class StackingThread implements StackingLogic, AutoCloseable {

    private final static String NEW_METADATA = "RS_new";

    private final static Cache<UUID, Boolean> REMOVED_ENTITIES = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();

    private final RosePlugin rosePlugin;
    private final StackManager stackManager;
    private final EntityCacheManager entityCacheManager;
    private final HologramManager hologramManager;
    private final World targetWorld;
    private final boolean disabled;

    private ScheduledTask entityStackTask, itemStackTask, nametagTask, hologramTask;
    private ScheduledTask entityUnstackTask, entityCleanupTask;

    private final Map<UUID, StackedEntity> stackedEntities;
    private final Map<UUID, StackedItem> stackedItems;
    private final Map<Chunk, StackChunkData> stackChunkData;

    private final boolean dynamicEntityTags, dynamicItemTags;
    private final double entityDynamicViewRangeSqrd, itemDynamicViewRangeSqrd;
    private final boolean entityDynamicWallDetection, itemDynamicWallDetection;

    public StackingThread(RosePlugin rosePlugin, StackManager stackManager, World targetWorld) {
        this.rosePlugin = rosePlugin;
        this.stackManager = stackManager;
        this.entityCacheManager = this.rosePlugin.getManager(EntityCacheManager.class);
        this.hologramManager = this.rosePlugin.getManager(HologramManager.class);
        this.targetWorld = targetWorld;
        this.disabled = this.stackManager.isWorldDisabled(targetWorld);

        if (!this.disabled) {
            this.entityStackTask = rosePlugin.getScheduler().runTaskTimerAsync(this::stackEntities, 5L, SettingKey.STACK_FREQUENCY.get());
            this.itemStackTask = rosePlugin.getScheduler().runTaskTimerAsync(this::stackItems, 5L, SettingKey.ITEM_STACK_FREQUENCY.get());
            this.nametagTask = rosePlugin.getScheduler().runTaskTimerAsync(this::processNametags, 5L, SettingKey.NAMETAG_UPDATE_FREQUENCY.get());
            this.hologramTask = rosePlugin.getScheduler().runTaskTimerAsync(this::updateHolograms, 5L, SettingKey.HOLOGRAM_UPDATE_FREQUENCY.get());

            long unstackFrequency = SettingKey.UNSTACK_FREQUENCY.get();
            if (unstackFrequency > 0)
                this.entityUnstackTask = rosePlugin.getScheduler().runTaskTimerAsync(this::unstackEntities, 5L, unstackFrequency);

            long cleanupFrequency = SettingKey.ENTITY_RESCAN_FREQUENCY.get();
            if (cleanupFrequency > 0)
                this.entityCleanupTask = rosePlugin.getScheduler().runTaskTimer(this::cleanupOrphanedEntities, 5L, cleanupFrequency);
        }

        this.stackedEntities = new ConcurrentHashMap<>();
        this.stackedItems = new ConcurrentHashMap<>();
        this.stackChunkData = new ConcurrentHashMap<>();

        this.dynamicEntityTags = SettingKey.ENTITY_DISPLAY_TAGS.get() && SettingKey.ENTITY_DYNAMIC_TAG_VIEW_RANGE_ENABLED.get();
        this.dynamicItemTags = SettingKey.ITEM_DISPLAY_TAGS.get() && SettingKey.ITEM_DYNAMIC_TAG_VIEW_RANGE_ENABLED.get();

        double entityDynamicViewRange = SettingKey.ENTITY_DYNAMIC_TAG_VIEW_RANGE.get();
        double itemDynamicViewRange = SettingKey.ITEM_DYNAMIC_TAG_VIEW_RANGE.get();

        this.entityDynamicViewRangeSqrd = entityDynamicViewRange * entityDynamicViewRange;
        this.itemDynamicViewRangeSqrd = itemDynamicViewRange * itemDynamicViewRange;

        this.entityDynamicWallDetection = SettingKey.ENTITY_DYNAMIC_TAG_VIEW_RANGE_WALL_DETECTION_ENABLED.get();
        this.itemDynamicWallDetection = SettingKey.ITEM_DYNAMIC_TAG_VIEW_RANGE_WALL_DETECTION_ENABLED.get();

        if (this.disabled)
            return;

        NMSAdapter.getHandler().hijackRandomSource(targetWorld);

        // Load chunk data for all stacks in the world
        for (Chunk chunk : this.targetWorld.getLoadedChunks()) {
            this.loadChunkEntities(Arrays.asList(chunk.getEntities()));
            this.loadChunkBlocks(chunk);
        }

        // Disable AI for all existing stacks in the target world
        this.targetWorld.getLivingEntities().forEach(PersistentDataUtils::applyDisabledAi);
    }

    private void stackEntities() {
        boolean entityStackingEnabled = this.stackManager.isEntityStackingEnabled();
        if (!entityStackingEnabled || this.stackManager.isEntityStackingTemporarilyDisabled())
            return;

        for (StackedEntity stackedEntity : this.stackedEntities.values()) {
            LivingEntity livingEntity = stackedEntity.getEntity();
            if (this.isRemoved(livingEntity)) {
                this.removeEntityStack(stackedEntity);
                continue;
            }

            this.tryStackEntity(stackedEntity);
        }
    }

    private void unstackEntities() {
        boolean entityStackingEnabled = this.stackManager.isEntityStackingEnabled();
        if (!entityStackingEnabled || this.stackManager.isEntityUnstackingTemporarilyDisabled())
            return;

        for (StackedEntity stackedEntity : this.stackedEntities.values())
            this.tryUnstackEntity(stackedEntity);
    }

    @Override
    public void tryUnstackEntity(StackedEntity stackedEntity) {
        LivingEntity entity = stackedEntity.getEntity();
        if (entity == null || stackedEntity.getStackSize() <= 1 || !entity.isValid())
            return;

        if (!stackedEntity.shouldStayStacked()) {
            ThreadUtils.runSync(() -> {
                if (stackedEntity.getStackSize() > 1)
                    this.splitEntityStack(stackedEntity);
            });
        } else if (SettingKey.ENTITY_MIN_SPLIT_IF_LOWER.get() && stackedEntity.getStackSize() < stackedEntity.getStackSettings().getMinStackSize()) {
            this.splitEntireStack(stackedEntity);
        }
    }

    private void splitEntireStack(StackedEntity stackedEntity) {
        LivingEntity entity = stackedEntity.getEntity();
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        StackedEntityDataStorage nbt = stackedEntity.getDataStorage();
        stackedEntity.setDataStorage(nmsHandler.createEntityDataStorage(entity, this.stackManager.getEntityDataStorageType(entity.getType())));
        ThreadUtils.runSync(() -> {
            for (EntityDataEntry entityDataEntry : nbt.getAll())
                entityDataEntry.createEntity(stackedEntity.getLocation(), true, entity.getType());
        });
    }

    private void cleanupOrphanedEntities() {
        for (Entity entity : this.targetWorld.getEntities()) {
            if (this.isRemoved(entity))
                continue;

            if (entity instanceof LivingEntity livingEntity && entity.getType() != EntityType.ARMOR_STAND && entity.getType() != EntityType.PLAYER && !this.isEntityStacked(livingEntity)) {
                if (!this.stackManager.isAreaDisabled(entity.getLocation()))
                    this.createEntityStack(livingEntity, false);
            } else if (entity.getType() == VersionUtils.ITEM) {
                Item item = (Item) entity;
                if (!this.isItemStacked(item) && !this.stackManager.isAreaDisabled(entity.getLocation()))
                    this.createItemStack(item, false);
            }
        }
    }

    private void stackItems() {
        boolean itemStackingEnabled = this.stackManager.isItemStackingEnabled();
        if (!itemStackingEnabled)
            return;

        boolean updateItemNametags = SettingKey.ITEM_DISPLAY_DESPAWN_TIMER_PLACEHOLDER.get();

        // Auto stack items
        for (StackedItem stackedItem : this.stackedItems.values()) {
            Item item = stackedItem.getItem();
            if (item == null || this.isRemoved(item)) {
                this.removeItemStack(stackedItem);
                continue;
            }

            if (updateItemNametags)
                stackedItem.updateDisplay();

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

        List<LivingEntity> entities = null;
        if (this.dynamicEntityTags) {
            entities = this.stackedEntities.values().stream()
                    .map(StackedEntity::getEntity)
                    .filter(Objects::nonNull)
                    .toList();
        }

        List<Item> items = null;
        if (this.dynamicItemTags) {
            items = this.stackedItems.values().stream()
                    .map(StackedItem::getItem)
                    .toList();
        }

        for (Player player : players) {
            if (!player.getWorld().equals(this.targetWorld))
                continue;

            ItemStack itemStack = player.getInventory().getItemInMainHand();
            boolean displayStackingToolParticles = ItemUtils.isStackingTool(itemStack);

            if (this.dynamicEntityTags) {
                for (LivingEntity entity : entities) {
                    double distanceSqrd;
                    try { // The locations can end up comparing cross-world if the player/entity switches worlds mid-loop due to being async
                        distanceSqrd = player.getLocation().distanceSquared(entity.getLocation());
                    } catch (Exception e) {
                        continue;
                    }

                    if (distanceSqrd > StackerUtils.ASSUMED_ENTITY_VISIBILITY_RANGE)
                        continue;

                    boolean visible = distanceSqrd < this.entityDynamicViewRangeSqrd;
                    if (this.entityDynamicWallDetection)
                        visible &= EntityUtils.hasLineOfSight(player, entity, 0.75, true);

                    StackedEntity stackedEntity = this.getStackedEntity(entity);
                    if (stackedEntity != null)
                        nmsHandler.updateEntityNameTagForPlayer(player, entity, stackedEntity.getDisplayName(), stackedEntity.isDisplayNameVisible() && visible);

                    // Spawn particles for holding the stacking tool
                    if (visible && displayStackingToolParticles) {
                        Location location = entity.getLocation().add(0, entity.getEyeHeight(true) + 0.75, 0);
                        DustOptions dustOptions;
                        if (PersistentDataUtils.isUnstackable(entity)) {
                            dustOptions = StackerUtils.UNSTACKABLE_DUST_OPTIONS;
                        } else {
                            dustOptions = StackerUtils.STACKABLE_DUST_OPTIONS;
                        }
                        player.spawnParticle(VersionUtils.DUST, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
                    }
                }
            }

            if (this.dynamicItemTags) {
                for (Item item : items) {
                    if (!item.isCustomNameVisible())
                        continue;

                    double distanceSqrd;
                    try { // The locations can end up comparing cross-world if the player/entity switches worlds mid-loop due to being async
                        distanceSqrd = player.getLocation().distanceSquared(item.getLocation());
                    } catch (Exception e) {
                        continue;
                    }

                    if (distanceSqrd > StackerUtils.ASSUMED_ENTITY_VISIBILITY_RANGE)
                        continue;

                    boolean visible = distanceSqrd < this.itemDynamicViewRangeSqrd;
                    if (this.itemDynamicWallDetection)
                        visible &= EntityUtils.hasLineOfSight(player, item, 0.75, true);

                    nmsHandler.updateEntityNameTagVisibilityForPlayer(player, item, visible);
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

        if (this.entityUnstackTask != null)
            this.entityUnstackTask.cancel();

        if (this.entityCleanupTask != null)
            this.entityCleanupTask.cancel();

        // Save all data
        this.saveAllData(true);
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
                .filter(x -> x.getStackSize() != 1 || SettingKey.MISC_CLEARALL_REMOVE_SINGLE.get())
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
    public void updateStackedEntityKey(LivingEntity oldKey, StackedEntity stackedEntity) {
        this.stackedEntities.remove(oldKey.getUniqueId());
        this.stackedEntities.put(stackedEntity.getEntity().getUniqueId(), stackedEntity);
    }

    @Override
    public StackedEntity splitEntityStack(StackedEntity stackedEntity) {
        EntityUnstackEvent entityUnstackEvent = new EntityUnstackEvent(stackedEntity, new StackedEntity(stackedEntity.getEntity()));
        Bukkit.getPluginManager().callEvent(entityUnstackEvent);
        if (entityUnstackEvent.isCancelled())
            return null;

        LivingEntity oldEntity = stackedEntity.getEntity();
        if (SettingKey.SPAWNER_DISABLE_MOB_AI_OPTIONS_REENABLE_AI_ON_SPLIT.get())
            PersistentDataUtils.reenableEntityAi(oldEntity);

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

        if (tryStack && this.canEntityInstantStack()) {
            livingEntity.setMetadata(NEW_METADATA, new FixedMetadataValue(this.rosePlugin, true));
            this.tryStackEntity(newStackedEntity);
            livingEntity.removeMetadata(NEW_METADATA, this.rosePlugin);
        }

        return newStackedEntity;
    }

    private boolean canEntityInstantStack() {
        return SettingKey.ENTITY_INSTANT_STACK.get() && (!NPCsHook.mythicMobsEnabled() || SettingKey.MISC_MYTHICMOBS_ALLOW_STACKING.get());
    }

    @Override
    public StackedItem createItemStack(Item item, boolean tryStack) {
        if (!this.stackManager.isItemStackingEnabled())
            return null;

        ItemStackSettings itemStackSettings = this.rosePlugin.getManager(StackSettingManager.class).getItemStackSettings(item);
        if (itemStackSettings != null && !itemStackSettings.isStackingEnabled())
            return null;

        StackedItem newStackedItem = new StackedItem(item.getItemStack().getAmount(), item, false);
        this.stackedItems.put(item.getUniqueId(), newStackedItem);

        if (tryStack && SettingKey.ITEM_INSTANT_STACK.get()) {
            item.setMetadata(NEW_METADATA, new FixedMetadataValue(this.rosePlugin, true));
            this.tryStackItem(newStackedItem);
            item.removeMetadata(NEW_METADATA, this.rosePlugin);
        }

        // Only update the display after stacking to avoid needing to calculate the name unnecessarily
        if (newStackedItem.getStackSize() > 0)
            newStackedItem.updateDisplay();

        return newStackedItem;
    }

    @Override
    public StackedBlock createBlockStack(Block block, int amount) {
        if (!this.stackManager.isBlockStackingEnabled() || !this.stackManager.isBlockTypeStackable(block))
            return null;

        StackChunkData stackChunkData = this.stackChunkData.computeIfAbsent(block.getChunk(), x -> new StackChunkData());
        StackedBlock newStackedBlock = new StackedBlock(amount, block);
        stackChunkData.addBlock(newStackedBlock);
        return newStackedBlock;
    }

    @Override
    public StackedSpawner createSpawnerStack(Block block, int amount, boolean placedByPlayer) {
        if (block.getType() != Material.SPAWNER)
            return null;

        CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
        EntityType spawnedType = CompatibilityAdapter.getCreatureSpawnerHandler().getSpawnedType(creatureSpawner);
        if (!this.stackManager.isSpawnerStackingEnabled() || !this.stackManager.isSpawnerTypeStackable(spawnedType))
            return null;

        StackChunkData stackChunkData = this.stackChunkData.computeIfAbsent(block.getChunk(), x -> new StackChunkData());
        StackedSpawner newStackedSpawner = new StackedSpawner(amount, block, placedByPlayer);
        stackChunkData.addSpawner(newStackedSpawner);
        return newStackedSpawner;
    }

    @Override
    public void addEntityStack(StackedEntity stackedEntity) {
        if (!this.stackManager.isEntityStackingEnabled() || NPCsHook.isNPC(stackedEntity.getEntity()))
            return;

        this.stackedEntities.put(stackedEntity.getEntity().getUniqueId(), stackedEntity);

        if (this.canEntityInstantStack())
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
            boolean removeAi = stackSettings.isMobAIDisabled();

            Collection<Entity> nearbyEntities = this.entityCacheManager.getNearbyEntities(location, stackSettings.getMergeRadius(), x -> x.getType() == entityType);
            Set<StackedEntity> nearbyStackedEntities = new HashSet<>();
            for (Entity entity : nearbyEntities) {
                StackedEntity stackedEntity = this.stackManager.getStackedEntity((LivingEntity) entity);
                if (stackedEntity != null)
                    nearbyStackedEntities.add(stackedEntity);
            }

            Set<StackedEntity> updatedEntities = new HashSet<>();
            Set<StackedEntity> newStackedEntities = new HashSet<>();
            switch (this.stackManager.getEntityDataStorageType(entityType)) {
                case NBT -> {
                    for (int i = 0; i < amount; i++) {
                        StackedEntity newStack = this.createNewEntity(nmsHandler, entityType, location, spawnReason, removeAi);
                        Optional<StackedEntity> matchingEntity = nearbyStackedEntities.stream().filter(x ->
                                stackSettings.testCanStackWith(x, newStack, false, true)).findFirst();
                        if (matchingEntity.isPresent()) {
                            matchingEntity.get().increaseStackSize(newStack.getEntity(), false);
                            updatedEntities.add(matchingEntity.get());
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
                            updatedEntities.add(matchingEntity.get());
                            i -= amountToIncrease;
                        } else {
                            StackedEntity newStack = this.createNewEntity(nmsHandler, entityType, location, spawnReason, removeAi);
                            nearbyStackedEntities.add(newStack);
                            newStackedEntities.add(newStack);
                        }
                    }
                }
            }

            updatedEntities.forEach(StackedEntity::updateDisplay);

            ThreadUtils.runSync(() -> {
                this.stackManager.setEntityStackingTemporarilyDisabled(true);
                for (StackedEntity stackedEntity : newStackedEntities) {
                    LivingEntity entity = stackedEntity.getEntity();
                    this.entityCacheManager.preCacheEntity(entity);
                    nmsHandler.spawnExistingEntity(stackedEntity.getEntity(), spawnReason, SettingKey.SPAWNER_BYPASS_REGION_SPAWNING_RULES.get());
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
    public void preStackItems(Collection<ItemStack> items, Location location, boolean dropNaturally) {
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
                    if (dropNaturally) {
                        location.getWorld().dropItemNaturally(location, toDrop);
                    } else {
                        location.getWorld().dropItem(location, toDrop);
                    }
                }
            }
            return;
        }

        // Drop all the items stacked with the correct amounts
        this.stackManager.setEntityStackingTemporarilyDisabled(true);

        for (Map.Entry<ItemStack, Integer> entry : itemStackAmounts.entrySet()) {
            if (entry.getValue() <= 0)
                continue;

            Item item;
            if (dropNaturally) {
                item = location.getWorld().dropItemNaturally(location, entry.getKey());
            } else {
                item = location.getWorld().dropItem(location, entry.getKey());
            }

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

        if (!this.stackManager.isItemStackingEnabled()) {
            ItemStack clone = itemStack.clone();
            clone.setAmount(amount);
            this.preStackItems(List.of(clone), location, dropNaturally);
            return null;
        }

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

        if (!stackedSpawners.isEmpty() || !stackedBlocks.isEmpty()) {
            this.stackChunkData.put(chunk, new StackChunkData(stackedSpawners, stackedBlocks));
            ThreadUtils.runAsync(() -> {
                stackedSpawners.values().forEach(StackedSpawner::updateDisplay);
                stackedBlocks.values().forEach(StackedBlock::updateDisplay);
            });
        }
    }

    @Override
    public void loadChunkEntities(List<Entity> entities) {
        if (entities.isEmpty())
            return;

        List<StackedEntity> stackedEntities = new ArrayList<>();
        if (this.stackManager.isEntityStackingEnabled()) {
            for (Entity entity : entities) {
                if (!(entity instanceof LivingEntity livingEntity) || entity.getType() == EntityType.ARMOR_STAND || entity.getType() == EntityType.PLAYER)
                    continue;

                StackedEntity stackedEntity = DataUtils.readStackedEntity(livingEntity, this.stackManager.getEntityDataStorageType(entity.getType()));
                if (stackedEntity != null) {
                    this.stackedEntities.put(stackedEntity.getEntity().getUniqueId(), stackedEntity);
                    stackedEntities.add(stackedEntity);
                } else {
                    this.createEntityStack(livingEntity, true);
                }
            }
        }

        List<StackedItem> stackedItems = new ArrayList<>();
        if (this.stackManager.isItemStackingEnabled()) {
            for (Entity entity : entities) {
                if (entity.getType() != VersionUtils.ITEM)
                    continue;

                Item item = (Item) entity;
                StackedItem stackedItem = DataUtils.readStackedItem(item);
                if (stackedItem != null) {
                    this.stackedItems.put(stackedItem.getItem().getUniqueId(), stackedItem);
                    stackedItems.add(stackedItem);
                } else {
                    this.createItemStack(item, true);
                }
            }
        }

        if (!stackedEntities.isEmpty() || !stackedItems.isEmpty()) {
            ThreadUtils.runAsync(() -> {
                stackedEntities.forEach(StackedEntity::updateDisplay);
                stackedItems.forEach(StackedItem::updateDisplay);
            });
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
    public void saveChunkEntities(List<Entity> entities, boolean clearStored) {
        List<Stack<?>> stacks = new ArrayList<>(entities.size());
        if (this.stackManager.isEntityStackingEnabled()) {
            stacks.addAll(entities.stream()
                    .filter(x -> x instanceof LivingEntity && x.getType() != EntityType.ARMOR_STAND && x.getType() != EntityType.PLAYER)
                    .map(x -> this.stackedEntities.get(x.getUniqueId()))
                    .filter(Objects::nonNull)
                    .toList());
        }

        if (this.stackManager.isItemStackingEnabled()) {
            stacks.addAll(entities.stream()
                    .filter(x -> x.getType() == VersionUtils.ITEM)
                    .map(x -> this.stackedItems.get(x.getUniqueId()))
                    .filter(Objects::nonNull)
                    .toList());
        }

        this.saveChunkEntityStacks(stacks, clearStored);
    }

    @Override
    public <T extends Stack<?>> void saveChunkEntityStacks(List<T> stacks, boolean clearStored) {
        if (this.stackManager.isEntityStackingEnabled()) {
            List<StackedEntity> stackedEntities = stacks.stream()
                    .filter(x -> x instanceof StackedEntity)
                    .map(x -> (StackedEntity) x)
                    .toList();

            stackedEntities.forEach(DataUtils::writeStackedEntity);

            if (clearStored)
                stackedEntities.stream().map(StackedEntity::getEntity).map(Entity::getUniqueId).forEach(this.stackedEntities::remove);
        }

        if (this.stackManager.isItemStackingEnabled()) {
            List<StackedItem> stackedItems = stacks.stream()
                    .filter(x -> x instanceof StackedItem)
                    .map(x -> (StackedItem) x)
                    .toList();

            stackedItems.forEach(DataUtils::writeStackedItem);

            if (clearStored)
                stackedItems.stream().map(StackedItem::getItem).map(Entity::getUniqueId).forEach(this.stackedItems::remove);
        }
    }

    @Override
    public void saveAllData(boolean clearStored) {
        // Save stacked blocks and spawners
        for (Chunk chunk : this.stackChunkData.keySet())
            this.saveChunkBlocks(chunk, false);

        // Save stacked entities and items
        List<Stack<?>> stacks = new ArrayList<>(this.stackedEntities.size() + this.stackedItems.size());
        stacks.addAll(this.stackedEntities.values());
        stacks.addAll(this.stackedItems.values());
        this.saveChunkEntityStacks(stacks, false);

        if (clearStored) {
            this.stackChunkData.clear();
            this.stackedEntities.clear();
            this.stackedItems.clear();
        }
    }

    /**
     * Tries to stack a StackedEntity with all other StackedEntities
     *
     * @param stackedEntity the StackedEntity to try to stack
     */
    @Override
    public void tryStackEntity(StackedEntity stackedEntity) {
        if (this.disabled)
            return;

        EntityStackSettings stackSettings = stackedEntity.getStackSettings();
        if (stackSettings == null)
            return;

        if (stackedEntity.checkNPC())
            return;

        LivingEntity entity = stackedEntity.getEntity();
        if (this.isRemoved(entity) || !stackedEntity.hasMoved())
            return;

        if (!WorldGuardHook.testLocation(entity.getLocation()))
            return;

        Collection<Entity> nearbyEntities;
        Predicate<Entity> predicate = x -> x.getType() == entity.getType();
        if (!SettingKey.ENTITY_MERGE_ENTIRE_CHUNK.get()) {
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
                    && (!SettingKey.ENTITY_REQUIRE_LINE_OF_SIGHT.get() || EntityUtils.hasLineOfSight(entity, otherEntity, 0.75, false))
                    && WorldGuardHook.testLocation(otherEntity.getLocation()))
                targetEntities.add(other);
        }

        StackedEntity increased;
        int totalSize;
        List<StackedEntity> removable = new ArrayList<>(targetEntities.size());
        if (!SettingKey.ENTITY_MIN_STACK_COUNT_ONLY_INDIVIDUALS.get()) {
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

        ThreadUtils.runOnPrimary(() -> removable.stream().map(StackedEntity::getEntity).forEach(Entity::remove));
    }

    @Override
    public void tryStackItem(StackedItem stackedItem) {
        if (this.disabled)
            return;

        ItemStackSettings stackSettings = stackedItem.getStackSettings();
        Item item = stackedItem.getItem();
        if (stackSettings == null
                || !stackSettings.isStackingEnabled()
                || item.getPickupDelay() > 40
                || !stackedItem.hasMoved()
                || PersistentDataUtils.isUnstackable(item))
            return;

        if (this.isRemoved(item))
            return;

        Predicate<Entity> predicate = x -> x.getType() == VersionUtils.ITEM;
        Set<Item> nearbyItems = this.entityCacheManager.getNearbyEntities(stackedItem.getLocation(), SettingKey.ITEM_MERGE_RADIUS.get(), predicate)
                .stream()
                .map(x -> (Item) x)
                .collect(Collectors.toSet());

        Set<StackedItem> targetItems = new HashSet<>();
        for (Item otherItem : nearbyItems) {
            if (item == otherItem
                    || otherItem.getPickupDelay() > 40
                    || !item.getItemStack().isSimilar(otherItem.getItemStack())
                    || !Objects.equals(item.getOwner(), otherItem.getOwner())
                    || PersistentDataUtils.isUnstackable(otherItem)
                    || this.isRemoved(otherItem))
                continue;

            StackedItem other = this.stackedItems.get(otherItem.getUniqueId());
            if (other != null)
                targetItems.add(other);
        }

        if (targetItems.isEmpty())
            return;

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

            increased.increaseStackSize(removed.getStackSize(), false);
            removed.increaseStackSize(-removed.getStackSize(), false);
            if (SettingKey.ITEM_RESET_DESPAWN_TIMER_ON_MERGE.get())
                increased.getItem().setTicksLived(1); // Reset the 5 minute pickup timer

            increased.getItem().setPickupDelay(Math.max(increased.getItem().getPickupDelay(), removed.getItem().getPickupDelay()));
            removed.getItem().setPickupDelay(100); // Don't allow the item we just merged to get picked up or stacked

            ThreadUtils.runOnPrimary(() -> removed.getItem().remove());
            this.removeItemStack(removed);
        }

        headStack.updateDisplay();
    }

    public void transferExistingEntityStack(UUID entityUUID, StackedEntity stackedEntity, StackingThread toThread) {
        this.stackedEntities.remove(entityUUID);
        toThread.loadExistingEntityStack(entityUUID, stackedEntity);
    }

    public void transferExistingItemStack(UUID entityUUID, StackedItem stackedItem, StackingThread toThread) {
        this.stackedItems.remove(entityUUID);
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
        return entity == null || (!entity.hasMetadata(NEW_METADATA) && !entity.isValid()) || REMOVED_ENTITIES.getIfPresent(entity.getUniqueId()) != null;
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
