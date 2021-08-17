package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.event.EntityStackClearEvent;
import dev.rosewood.rosestacker.event.EntityStackEvent;
import dev.rosewood.rosestacker.event.EntityUnstackEvent;
import dev.rosewood.rosestacker.event.ItemStackClearEvent;
import dev.rosewood.rosestacker.event.ItemStackEvent;
import dev.rosewood.rosestacker.hook.NPCsHook;
import dev.rosewood.rosestacker.hook.WorldGuardHook;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.ItemStackSettings;
import dev.rosewood.rosestacker.utils.DataUtils;
import dev.rosewood.rosestacker.utils.EntityUtils;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
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
import java.util.function.Function;
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
    private final static String REMOVED_METADATA = "RS_removed";

    private final RosePlugin rosePlugin;
    private final StackManager stackManager;
    private final EntityCacheManager entityCacheManager;
    private final World targetWorld;

    private final BukkitTask entityStackTask, itemStackTask, nametagTask;

    private final Map<UUID, StackedEntity> stackedEntities;
    private final Map<UUID, StackedItem> stackedItems;
    private final Map<Chunk, StackChunkData> stackChunkData;

    private boolean entityStackSwitch;
    private int cleanupTimer;

    boolean dynamicEntityTags, dynamicItemTags, dynamicBlockTags;
    double entityDynamicViewRangeSqrd, itemDynamicViewRangeSqrd, blockSpawnerDynamicViewRangeSqrd;
    boolean entityDynamicWallDetection, itemDynamicWallDetection, blockDynamicWallDetection;

    public StackingThread(RosePlugin rosePlugin, StackManager stackManager, World targetWorld) {
        this.rosePlugin = rosePlugin;
        this.stackManager = stackManager;
        this.entityCacheManager = this.rosePlugin.getManager(EntityCacheManager.class);
        this.targetWorld = targetWorld;

        long entityStackDelay = (long) Math.max(1, Setting.STACK_FREQUENCY.getLong() / 2.0);
        this.entityStackTask = Bukkit.getScheduler().runTaskTimer(this.rosePlugin, this::stackEntities, 5L, entityStackDelay);
        this.itemStackTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.rosePlugin, this::stackItems, 5L, Setting.ITEM_STACK_FREQUENCY.getLong());
        this.nametagTask = Bukkit.getScheduler().runTaskTimer(this.rosePlugin, this::processNametags, 5L, Setting.NAMETAG_UPDATE_FREQUENCY.getLong());

        this.stackedEntities = new ConcurrentHashMap<>();
        this.stackedItems = new ConcurrentHashMap<>();
        this.stackChunkData = new ConcurrentHashMap<>();

        this.cleanupTimer = 0;

        this.dynamicEntityTags = Setting.ENTITY_DISPLAY_TAGS.getBoolean() && Setting.ENTITY_DYNAMIC_TAG_VIEW_RANGE_ENABLED.getBoolean();
        this.dynamicItemTags = Setting.ITEM_DISPLAY_TAGS.getBoolean() && Setting.ITEM_DYNAMIC_TAG_VIEW_RANGE_ENABLED.getBoolean();
        this.dynamicBlockTags = Setting.BLOCK_DISPLAY_TAGS.getBoolean() && Setting.BLOCK_DYNAMIC_TAG_VIEW_RANGE_ENABLED.getBoolean();

        double entityDynamicViewRange = Setting.ENTITY_DYNAMIC_TAG_VIEW_RANGE.getDouble();
        double itemDynamicViewRange = Setting.ITEM_DYNAMIC_TAG_VIEW_RANGE.getDouble();
        double blockSpawnerDynamicViewRange = Setting.BLOCK_DYNAMIC_TAG_VIEW_RANGE.getDouble();

        this.entityDynamicViewRangeSqrd = entityDynamicViewRange * entityDynamicViewRange;
        this.itemDynamicViewRangeSqrd = itemDynamicViewRange * itemDynamicViewRange;
        this.blockSpawnerDynamicViewRangeSqrd = blockSpawnerDynamicViewRange * blockSpawnerDynamicViewRange;

        this.entityDynamicWallDetection = Setting.ENTITY_DYNAMIC_TAG_VIEW_RANGE_WALL_DETECTION_ENABLED.getBoolean();
        this.itemDynamicWallDetection = Setting.ITEM_DYNAMIC_TAG_VIEW_RANGE_WALL_DETECTION_ENABLED.getBoolean();
        this.blockDynamicWallDetection = Setting.BLOCK_DYNAMIC_TAG_VIEW_RANGE_WALL_DETECTION_ENABLED.getBoolean();

        // Disable AI for all existing stacks in the target world
        this.targetWorld.getLivingEntities().forEach(PersistentDataUtils::applyDisabledAi);
    }

    private void stackEntities() {
        boolean itemStackingEnabled = this.stackManager.isItemStackingEnabled();
        boolean entityStackingEnabled = this.stackManager.isEntityStackingEnabled();
        if (!entityStackingEnabled)
            return;

        // Auto stack entities
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
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

            // Auto unstack entities
            if (!this.stackManager.isEntityUnstackingTemporarilyDisabled())
                for (StackedEntity stackedEntity : this.stackedEntities.values())
                    if (!stackedEntity.shouldStayStacked())
                        Bukkit.getScheduler().runTask(this.rosePlugin, () -> {
                            if (stackedEntity.getStackSize() > 1)
                                this.splitEntityStack(stackedEntity);
                        });
        });

        // Run entity stacking half as often as the unstacking
        this.entityStackSwitch = !this.entityStackSwitch;

        // Cleans up entities/items that aren't stacked
        this.cleanupTimer++;
        if (this.cleanupTimer >= CLEANUP_TIMER_TARGET) {
            for (Entity entity : this.targetWorld.getEntities()) {
                if (this.isRemoved(entity))
                    continue;

                if (entity instanceof LivingEntity && entity.getType() != EntityType.ARMOR_STAND && entity.getType() != EntityType.PLAYER) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if (!this.isEntityStacked(livingEntity))
                        this.createEntityStack(livingEntity, false);
                } else if (itemStackingEnabled && entity.getType() == EntityType.DROPPED_ITEM) {
                    Item item = (Item) entity;
                    if (!this.isItemStacked(item))
                        this.createItemStack(item, false);
                }
            }
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
        if (!this.dynamicEntityTags && !this.dynamicItemTags && !this.dynamicBlockTags)
            return;

        // Handle dynamic stack tags
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        Set<EntityType> validEntities = StackerUtils.getStackableEntityTypes();
        List<Entity> entities = this.targetWorld.getEntities();

        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            for (Player player : this.targetWorld.getPlayers()) {
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
                    if (this.dynamicEntityTags && (validEntities.contains(entity.getType()))) {
                        visible = distanceSqrd < this.entityDynamicViewRangeSqrd;
                        if (this.entityDynamicWallDetection)
                            visible &= EntityUtils.hasLineOfSight(player, entity, 0.75, true);
                    } else if (this.dynamicItemTags && entity.getType() == EntityType.DROPPED_ITEM) {
                        visible = distanceSqrd < this.itemDynamicViewRangeSqrd;
                        if (this.itemDynamicWallDetection)
                            visible &= EntityUtils.hasLineOfSight(player, entity, 0.75, true);
                    } else if (this.dynamicBlockTags && entity.getType() == EntityType.ARMOR_STAND) {
                        visible = distanceSqrd < this.blockSpawnerDynamicViewRangeSqrd;
                        if (this.blockDynamicWallDetection)
                            visible &= EntityUtils.hasLineOfSight(player, entity, 0.75, true);
                    } else continue;

                    if (entity.getType() != EntityType.ARMOR_STAND && entity instanceof LivingEntity) {
                        LivingEntity livingEntity = (LivingEntity) entity;
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
        });
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
    public List<StackedSpawner> getStackedSpawnersList() {
        List<StackedSpawner> stackedSpawners = new ArrayList<>();
        for (StackChunkData stackChunkData : this.stackChunkData.values())
            stackedSpawners.addAll(stackChunkData.getSpawners().values());
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
        Block key = stackedSpawner.getSpawner().getBlock();
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
                .collect(Collectors.toList());

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

        if (tryStack && Setting.ENTITY_INSTANT_STACK.getBoolean())
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

        StackedSpawner newStackedSpawner = new StackedSpawner(amount, creatureSpawner, placedByPlayer);

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

        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            EntityStackSettings stackSettings = this.rosePlugin.getManager(StackSettingManager.class).getEntityStackSettings(entityType);
            Set<StackedEntity> stackedEntities = new HashSet<>();
            NMSHandler nmsHandler = NMSAdapter.getHandler();
            for (int i = 0; i < amount; i++) {
                LivingEntity entity = nmsHandler.createNewEntityUnspawned(entityType, location);
                StackedEntity newStack = new StackedEntity(entity);
                Optional<StackedEntity> matchingEntity = stackedEntities.stream().filter(x ->
                        stackSettings.testCanStackWith(x, newStack, false, true)).findFirst();
                if (matchingEntity.isPresent()) {
                    matchingEntity.get().increaseStackSize(entity, false);
                } else {
                    stackedEntities.add(newStack);
                }
            }

            Bukkit.getScheduler().runTask(this.rosePlugin, () -> {
                this.stackManager.setEntityStackingTemporarilyDisabled(true);
                for (StackedEntity stackedEntity : stackedEntities) {
                    LivingEntity entity = stackedEntity.getEntity();
                    this.entityCacheManager.preCacheEntity(entity);
                    nmsHandler.spawnExistingEntity(stackedEntity.getEntity(), SpawnReason.SPAWNER_EGG);
                    entity.setVelocity(Vector.getRandom().multiply(0.01));
                    this.addEntityStack(stackedEntity);
                    stackedEntity.updateDisplay();
                }
                this.stackManager.setEntityStackingTemporarilyDisabled(false);
            });
        });
    }

    @Override
    public void preStackEntities(EntityType entityType, int amount, Location location) {
        this.preStackEntities(entityType, amount, location, SpawnReason.CUSTOM);
    }

    @Override
    public void preStackItems(Collection<ItemStack> items, Location location) {
        if (location.getWorld() == null)
            return;

        if (!this.stackManager.isItemStackingEnabled()) {
            for (ItemStack item : items)
                location.getWorld().dropItemNaturally(location, item);
            return;
        }

        this.stackManager.setEntityStackingTemporarilyDisabled(true);

        Set<StackedItem> stackedItems = new HashSet<>();
        for (ItemStack itemStack : items) {
            Optional<StackedItem> matchingItem = stackedItems.stream().filter(x ->
                    x.getItem().getItemStack().isSimilar(itemStack) && x.getStackSize() + itemStack.getAmount() <= x.getStackSettings().getMaxStackSize()).findFirst();
            if (matchingItem.isPresent()) {
                matchingItem.get().increaseStackSize(itemStack.getAmount(), false);
            } else {
                Item item = location.getWorld().dropItemNaturally(location, itemStack);
                stackedItems.add(new StackedItem(item.getItemStack().getAmount(), item));
            }
        }

        for (StackedItem stackedItem : stackedItems) {
            this.addItemStack(stackedItem);
            stackedItem.updateDisplay();
        }

        this.stackManager.setEntityStackingTemporarilyDisabled(false);
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
            increased.increaseStackSize(toStack.getStackedEntityNBT());
            this.removeEntityStack(toStack);
        }

        Runnable removeTask = () -> removable.stream().map(StackedEntity::getEntity).forEach(Entity::remove);
        if (Bukkit.isPrimaryThread()) {
            removeTask.run();
        } else {
            Bukkit.getScheduler().runTask(this.rosePlugin, removeTask);
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
            increased.getItem().setTicksLived(1); // Reset the 5 minute pickup timer
            removed.getItem().setPickupDelay(100); // Don't allow the item we just merged to get picked up or stacked
            increased.getItem().setPickupDelay(5);

            Runnable removeTask = () -> removed.getItem().remove();
            if (Bukkit.isPrimaryThread()) {
                removeTask.run();
            } else {
                Bukkit.getScheduler().runTask(this.rosePlugin, removeTask);
            }

            this.removeItemStack(removed);
        }
    }

    public void transferExistingEntityStack(UUID entityUUID, StackedEntity stackedEntity, StackingThread toThread) {
        this.stackedEntities.remove(entityUUID);
        toThread.loadExistingEntityStack(entityUUID, stackedEntity);
    }

    private void loadExistingEntityStack(UUID entityUUID, StackedEntity stackedEntity) {
        stackedEntity.updateEntity();
        this.stackedEntities.put(entityUUID, stackedEntity);
    }

    private boolean isRemoved(Entity entity) {
        return entity == null || !entity.isValid() || entity.hasMetadata(REMOVED_METADATA);
    }

    private void setRemoved(Entity entity) {
        entity.setMetadata(REMOVED_METADATA, new FixedMetadataValue(this.rosePlugin, true));
    }

    public void loadChunk(Chunk chunk, Entity[] entities) {
        if (!chunk.isLoaded())
            return;

        if (this.stackManager.isEntityStackingEnabled()) {
            for (Entity entity : entities) {
                if (!(entity instanceof LivingEntity) || entity.getType() == EntityType.ARMOR_STAND || entity.getType() == EntityType.PLAYER)
                    continue;

                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.removeMetadata(REMOVED_METADATA, this.rosePlugin);
                StackedEntity stackedEntity = DataUtils.readStackedEntity(livingEntity);
                if (stackedEntity != null)
                    this.stackedEntities.put(stackedEntity.getEntity().getUniqueId(), stackedEntity);
            }
        }

        if (this.stackManager.isItemStackingEnabled()) {
            for (Entity entity : entities) {
                if (entity.getType() != EntityType.DROPPED_ITEM)
                    continue;

                Item item = (Item) entity;
                item.removeMetadata(REMOVED_METADATA, this.rosePlugin);
                StackedItem stackedItem = DataUtils.readStackedItem(item);
                if (stackedItem != null)
                    this.stackedItems.put(stackedItem.getItem().getUniqueId(), stackedItem);
            }
        }

        Map<Block, StackedSpawner> stackedSpawners = new ConcurrentHashMap<>();
        if (this.stackManager.isSpawnerStackingEnabled())
            for (StackedSpawner stackedSpawner : DataUtils.readStackedSpawners(chunk))
                stackedSpawners.put(stackedSpawner.getSpawner().getBlock(), stackedSpawner);

        Map<Block, StackedBlock> stackedBlocks = new ConcurrentHashMap<>();
        if (this.stackManager.isBlockStackingEnabled())
            for (StackedBlock stackedBlock : DataUtils.readStackedBlocks(chunk))
                stackedBlocks.put(stackedBlock.getBlock(), stackedBlock);

        if (!stackedSpawners.isEmpty() || !stackedBlocks.isEmpty())
            this.stackChunkData.put(chunk, new StackChunkData(stackedSpawners, stackedBlocks));
    }

    public void saveChunk(Chunk chunk, boolean clearStored) {
        Entity[] entities = chunk.getEntities();
        if (this.stackManager.isEntityStackingEnabled()) {
            List<StackedEntity> stackedEntities = Arrays.stream(entities)
                    .filter(x -> x instanceof LivingEntity && x.getType() != EntityType.ARMOR_STAND && x.getType() != EntityType.PLAYER)
                    .map(x -> this.stackedEntities.get(x.getUniqueId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            stackedEntities.forEach(DataUtils::writeStackedEntity);

            if (clearStored)
                stackedEntities.stream().map(StackedEntity::getEntity).map(Entity::getUniqueId).forEach(this.stackedItems::remove);
        }

        if (this.stackManager.isItemStackingEnabled()) {
            List<StackedItem> stackedItems = Arrays.stream(entities)
                    .filter(x -> x.getType() == EntityType.DROPPED_ITEM)
                    .map(x -> this.stackedItems.get(x.getUniqueId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            stackedItems.forEach(DataUtils::writeStackedItem);

            if (clearStored)
                stackedItems.stream().map(StackedItem::getItem).map(Entity::getUniqueId).forEach(this.stackedItems::remove);
        }

        StackChunkData stackChunkData = this.stackChunkData.get(chunk);
        if (stackChunkData == null)
            return;

        if (this.stackManager.isSpawnerStackingEnabled())
            DataUtils.writeStackedSpawners(stackChunkData.getSpawners().values(), chunk);

        if (this.stackManager.isBlockStackingEnabled())
            DataUtils.writeStackedBlocks(stackChunkData.getBlocks().values(), chunk);

        if (clearStored)
            this.stackChunkData.remove(chunk);
    }

    /**
     * Used to add a StackedEntity loaded from the database
     *
     * @param stackedEntity to load
     */
    public void putStackedEntity(StackedEntity stackedEntity) {
        this.stackedEntities.put(stackedEntity.getEntity().getUniqueId(), stackedEntity);
    }

    /**
     * Used to add a StackedItem loaded from the database
     *
     * @param stackedItem to load
     */
    public void putStackedItem(StackedItem stackedItem) {
        this.stackedItems.put(stackedItem.getItem().getUniqueId(), stackedItem);
    }

    /**
     * Used to add a StackedBlock loaded from the database
     *
     * @param stackedBlock to load
     */
    public void putStackedBlock(StackedBlock stackedBlock) {
        Block block = stackedBlock.getBlock();
        StackChunkData stackChunkData = this.stackChunkData.get(block.getChunk());
        if (stackChunkData == null) {
            stackChunkData = new StackChunkData(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
            this.stackChunkData.put(block.getChunk(), stackChunkData);
        }

        stackChunkData.addBlock(stackedBlock);
    }

    /**
     * Used to add a StackedSpawner loaded from the database
     *
     * @param stackedSpawner to load
     */
    public void putStackedSpawner(StackedSpawner stackedSpawner) {
        Block block = stackedSpawner.getSpawner().getBlock();
        StackChunkData stackChunkData = this.stackChunkData.get(block.getChunk());
        if (stackChunkData == null) {
            stackChunkData = new StackChunkData(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
            this.stackChunkData.put(block.getChunk(), stackChunkData);
        }

        stackChunkData.addSpawner(stackedSpawner);
    }

    /**
     * @return the world that this StackingThread is acting on
     */
    public World getTargetWorld() {
        return this.targetWorld;
    }

}
