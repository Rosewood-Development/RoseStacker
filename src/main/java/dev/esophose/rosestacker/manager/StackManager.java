package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.stack.Stack;
import dev.esophose.rosestacker.stack.StackedBlock;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.StackedItem;
import dev.esophose.rosestacker.stack.StackedSpawner;
import dev.esophose.rosestacker.stack.settings.BlockStackSettings;
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
import org.bukkit.entity.Flying;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class StackManager extends Manager implements Runnable {

    private BukkitTask task;
    private BukkitTask cleanupTask;

    private final Map<Block, StackedBlock> stackedBlocks;
    private final Map<UUID, StackedEntity> stackedEntities;
    private final Map<UUID, StackedItem> stackedItems;
    private final Map<Block, StackedSpawner> stackedSpawners;

    private final Set<Stack> deletedStacks;

    private boolean isEntityStackingDisabled;

    // Cached, as we will be using it a lot
    private StackSettingManager stackSettingManager;

    public StackManager(RoseStacker roseStacker) {
        super(roseStacker);

        this.stackedItems = new ConcurrentHashMap<>();
        this.stackedBlocks = new ConcurrentHashMap<>();
        this.stackedSpawners = new ConcurrentHashMap<>();
        this.stackedEntities = new ConcurrentHashMap<>();

        this.deletedStacks = new HashSet<>();

        this.isEntityStackingDisabled = false;
    }

    @Override
    public void reload() {
        if (this.task != null)
            this.task.cancel();

        if (this.cleanupTask != null)
            this.cleanupTask.cancel();

        this.task = Bukkit.getScheduler().runTaskTimer(this.roseStacker, this, 0, Setting.STACK_FREQUENCY.getInt());
        this.stackSettingManager = this.roseStacker.getStackSettingManager();

        DataManager dataManager = this.roseStacker.getDataManager();

        this.deleteStacks();

        // Restore custom names
        for (StackedEntity stackedEntity : this.stackedEntities.values())
            stackedEntity.getEntity().setCustomName(stackedEntity.getOriginalCustomName());

        // Save anything that's loaded
        dataManager.createOrUpdateStackedBlocksOrSpawners(this.stackedBlocks.values(), false);
        dataManager.createOrUpdateStackedEntities(this.stackedEntities.values(), false);
        dataManager.createOrUpdateStackedItems(this.stackedItems.values(), false);
        dataManager.createOrUpdateStackedBlocksOrSpawners(this.stackedSpawners.values(), false);

        // Clear existing stacks
        this.stackedBlocks.clear();
        this.stackedEntities.clear();
        this.stackedItems.clear();
        this.stackedSpawners.clear();

        // Load all existing entities in the worlds that are saved
        Set<Chunk> chunks = new HashSet<>();
        for (World world : Bukkit.getWorlds())
            chunks.addAll(Arrays.asList(world.getLoadedChunks()));

        dataManager.getStackedBlocks(chunks, false, (stacks) -> stacks.forEach(x -> this.stackedBlocks.put(x.getBlock(), x)));
        dataManager.getStackedSpawners(chunks, false, (stacks) -> stacks.forEach(x -> this.stackedSpawners.put(x.getSpawner().getBlock(), x)));
        dataManager.getStackedEntities(chunks, false, (stacks) -> stacks.forEach(x -> this.stackedEntities.put(x.getEntity().getUniqueId(), x)));
        dataManager.getStackedItems(chunks, false, (stacks) -> stacks.forEach(x -> this.stackedItems.put(x.getItem().getUniqueId(), x)));

        // Cleans up entities that aren't stacked
        this.cleanupTask = Bukkit.getScheduler().runTaskTimer(this.roseStacker, () -> {
            for (World world : Bukkit.getWorlds()) {
                if (this.isWorldDisabled(world))
                    continue;

                for (Entity entity : world.getEntities())
                    if ((entity instanceof LivingEntity || entity instanceof Item) && !this.isEntityStacked(entity))
                        this.createStackFromEntity(entity, true);
            }
        }, 100L, 100L);
    }

    @Override
    public void disable() {
        this.task.cancel();
        this.cleanupTask.cancel();

        this.deleteStacks();

        // Restore custom names
        for (StackedEntity stackedEntity : this.stackedEntities.values())
            stackedEntity.getEntity().setCustomName(stackedEntity.getOriginalCustomName());

        // Save anything that's loaded
        DataManager dataManager = this.roseStacker.getDataManager();
        dataManager.createOrUpdateStackedBlocksOrSpawners(this.stackedBlocks.values(), false);
        dataManager.createOrUpdateStackedEntities(this.stackedEntities.values(), false);
        dataManager.createOrUpdateStackedItems(this.stackedItems.values(), false);
        dataManager.createOrUpdateStackedBlocksOrSpawners(this.stackedSpawners.values(), false);
    }

    public StackedItem getStackedItem(Item item) {
        if (!Setting.ITEM_STACKING_ENABLED.getBoolean() || this.isWorldDisabled(item))
            return null;

        return this.stackedItems.get(item.getUniqueId());
    }

    public StackedBlock getStackedBlock(Block block) {
        if (!Setting.BLOCK_STACKING_ENABLED.getBoolean() || this.isWorldDisabled(block))
            return null;

        return this.stackedBlocks.get(block);
    }

    public StackedSpawner getStackedSpawner(Block block) {
        if (!Setting.SPAWNER_STACKING_ENABLED.getBoolean() || this.isWorldDisabled(block))
            return null;

        return this.stackedSpawners.get(block);
    }

    public StackedEntity getStackedEntity(LivingEntity entity) {
        if (!Setting.ENTITY_STACKING_ENABLED.getBoolean() || this.isWorldDisabled(entity))
            return null;

        return this.stackedEntities.get(entity.getUniqueId());
    }

    /**
     * Checks if a given block is either part of a block stack or spawner stack
     *
     * @param block The block to check
     * @return true if the block is part of a block or spawner stack, otherwise false
     */
    public boolean isBlockStacked(Block block) {
        return this.getStackedBlock(block) != null || this.getStackedSpawner(block) != null;
    }

    /**
     * Checks if a given entity is either part of an item stack or entity stack
     *
     * @param entity The entity to check
     * @return true if the entity is part of an item stack or entity stack, otherwise false
     */
    public boolean isEntityStacked(Entity entity) {
        if (entity instanceof Item)
            return this.getStackedItem((Item) entity) != null;
        if (entity instanceof LivingEntity)
            return this.getStackedEntity((LivingEntity) entity) != null;
        return false;
    }

    /**
     * Checks if a given block type is able to be stacked
     *
     * @param block The block to check
     * @return true if the block is stackable, otherwise false
     */
    public boolean isBlockTypeStackable(Block block) {
        BlockStackSettings blockStackSettings = this.roseStacker.getStackSettingManager().getBlockStackSettings(block);
        return blockStackSettings.isStackingEnabled() || block.getType() == Material.SPAWNER;
    }

    public boolean isWorldDisabled(Entity entity) {
        return this.isWorldDisabled(entity.getLocation().getWorld());
    }

    public boolean isWorldDisabled(Block block) {
        return this.isWorldDisabled(block.getLocation());
    }

    public boolean isWorldDisabled(Location location) {
        return this.isWorldDisabled(location.getWorld());
    }

    public boolean isWorldDisabled(World world) {
        if (world == null)
            return true;
        return Setting.DISABLED_WORLDS.getStringList().stream().anyMatch(x -> x.equalsIgnoreCase(world.getName()));
    }

    public void removeItem(StackedItem stackedItem) {
        if (!this.isEntityStacked(stackedItem.getItem()))
            return;

        this.deletedStacks.add(stackedItem);
    }

    public void removeEntity(StackedEntity stackedEntity) {
        if (!this.isEntityStacked(stackedEntity.getEntity()))
            return;

        this.deletedStacks.add(stackedEntity);
    }

    public void removeBlock(StackedBlock stackedBlock) {
        if (!this.isBlockStacked(stackedBlock.getBlock()))
            return;

        this.deletedStacks.add(stackedBlock);
    }

    public void removeSpawner(StackedSpawner stackedSpawner) {
        if (!this.isBlockStacked(stackedSpawner.getSpawner().getBlock()))
            return;

        this.deletedStacks.add(stackedSpawner);
    }

    /**
     * Removes all stacked entities
     *
     * @return the number of entities removed
     */
    public int removeAllEntities() {
        int removed = 0;
        for (StackedEntity stackedEntity : this.stackedEntities.values()) {
            stackedEntity.getEntity().remove();
            this.removeEntity(stackedEntity);
            removed++;
        }
        return removed;
    }

    /**
     * Removes all stacked items
     *
     * @return the number of items removed
     */
    public int removeAllItems() {
        int removed = 0;
        for (StackedItem stackedItem : this.stackedItems.values()) {
            stackedItem.getItem().remove();
            this.removeItem(stackedItem);
            removed++;
        }
        return removed;
    }

    public void updateStackedEntityKey(LivingEntity oldKey, LivingEntity newKey) {
        StackedEntity value = this.stackedEntities.get(oldKey.getUniqueId());
        if (value != null) {
            this.stackedEntities.remove(oldKey.getUniqueId());
            this.stackedEntities.put(newKey.getUniqueId(), value);
        }
    }

    /**
     * Splits a StackedItem into two stacks
     *
     * @param stackedItem The StackedItem to split
     * @param newSize The size of the new StackedItem to create
     * @return the new StackedItem split from the old one
     */
    public StackedItem splitItem(StackedItem stackedItem, int newSize) {
        ItemStack oldItemStack = stackedItem.getItem().getItemStack();
        ItemStack newItemStack = oldItemStack.clone();

        newItemStack.setAmount(newSize);

        stackedItem.getItem().setPickupDelay(30);
        stackedItem.getItem().setTicksLived(1);

        Item newItem = stackedItem.getLocation().getWorld().spawn(stackedItem.getLocation(), Item.class, (entity) -> {
            entity.setItemStack(newItemStack);
            entity.setPickupDelay(0);
        });

        StackedItem newStackedItem = new StackedItem(newSize, newItem);
        this.stackedItems.put(newItem.getUniqueId(), newStackedItem);
        stackedItem.increaseStackSize(-newSize);
        return newStackedItem;
    }

    public StackedEntity splitEntity(StackedEntity stackedEntity) {
        StackedEntity newlySplit = stackedEntity.split();
        this.stackedEntities.put(newlySplit.getEntity().getUniqueId(), newlySplit);
        return newlySplit;
    }

    /**
     * Creates a StackedEntity or StackedItem from the given entity
     *
     * @param entity The entity to create a stack from
     * @param tryStack Whether or not to try to stack the mob instantly
     * @return The newly created stack, or null if one wasn't created
     */
    public Stack createStackFromEntity(Entity entity, boolean tryStack) {
        if (this.isWorldDisabled(entity))
            return null;

        Stack newStack = null;

        if (entity instanceof Item) {
            if (!Setting.ITEM_STACKING_ENABLED.getBoolean())
                return null;

            Item item = (Item) entity;
            StackedItem newStackedItem = new StackedItem(item.getItemStack().getAmount(), item);
            this.stackedItems.put(item.getUniqueId(), newStackedItem);
            newStack = newStackedItem;
        } else if (entity instanceof LivingEntity) {
            if (!Setting.ENTITY_STACKING_ENABLED.getBoolean())
                return null;

            LivingEntity livingEntity = (LivingEntity) entity;
            if (livingEntity instanceof Player || livingEntity instanceof ArmorStand)
                return null;

            StackedEntity newStackedEntity = new StackedEntity(livingEntity, new LinkedList<>());
            this.stackedEntities.put(livingEntity.getUniqueId(), newStackedEntity);
            newStack = newStackedEntity;
        }

        if (newStack != null && tryStack)
            this.tryStackEntity(newStack);

        return newStack;
    }

    public Stack createStackFromBlock(Block block, int amount) {
        if (this.isWorldDisabled(block))
            return null;

        Stack newStack;

        if (block.getType() == Material.SPAWNER) {
            if (!Setting.SPAWNER_STACKING_ENABLED.getBoolean())
                return null;

            StackedSpawner stackedSpawner = new StackedSpawner(amount, (CreatureSpawner) block.getState());
            this.stackedSpawners.put(block, stackedSpawner);
            newStack = stackedSpawner;
        } else {
            if (!Setting.BLOCK_STACKING_ENABLED.getBoolean())
                return null;

            StackedBlock stackedBlock = new StackedBlock(amount, block);
            this.stackedBlocks.put(block, stackedBlock);
            newStack = stackedBlock;
        }

        return newStack;
    }

    /**
     * Pre-stacks a collection of ItemStacks and spawns StackedEntities at the given location
     *
     * @param items The items to stack and spawn
     * @param location The location to spawn at
     */
    public void preStackItems(Collection<ItemStack> items, Location location) {
        if (location.getWorld() == null)
            return;

        this.setEntityStackingDisabled(true);

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

        this.setEntityStackingDisabled(false);
    }

    public void loadChunk(Chunk chunk) {
        DataManager dataManager = this.roseStacker.getDataManager();

        Set<Chunk> singletonChunk = Collections.singleton(chunk);

        dataManager.getStackedBlocks(singletonChunk, true, (stack) -> stack.forEach(x -> this.stackedBlocks.put(x.getBlock(), x)));
        dataManager.getStackedEntities(singletonChunk, true, (stack) -> stack.forEach(x -> this.stackedEntities.put(x.getEntity().getUniqueId(), x)));
        dataManager.getStackedItems(singletonChunk, true, (stack) -> stack.forEach(x -> this.stackedItems.put(x.getItem().getUniqueId(), x)));
        dataManager.getStackedSpawners(singletonChunk, true, (stack) -> stack.forEach(x -> this.stackedSpawners.put(x.getSpawner().getBlock(), x)));
    }

    public void unloadChunk(Chunk chunk) {
        DataManager dataManager = this.roseStacker.getDataManager();

        Map<Block, StackedBlock> stackedBlocks = this.stackedBlocks.entrySet().stream().filter(x -> x.getValue().getLocation().getChunk() == chunk).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<UUID, StackedEntity> stackedEntities = this.stackedEntities.entrySet().stream().filter(x -> x.getValue().getLocation().getChunk() == chunk).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<UUID, StackedItem> stackedItems = this.stackedItems.entrySet().stream().filter(x -> x.getValue().getLocation().getChunk() == chunk).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<Block, StackedSpawner> stackedSpawners = this.stackedSpawners.entrySet().stream().filter(x -> x.getValue().getLocation().getChunk() == chunk).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Restore custom names
        for (StackedEntity stackedEntity : stackedEntities.values())
            stackedEntity.getEntity().setCustomName(stackedEntity.getOriginalCustomName());

        dataManager.createOrUpdateStackedBlocksOrSpawners(stackedBlocks.values(), true);
        dataManager.createOrUpdateStackedEntities(stackedEntities.values(), true);
        dataManager.createOrUpdateStackedItems(stackedItems.values(), true);
        dataManager.createOrUpdateStackedBlocksOrSpawners(stackedSpawners.values(), true);

        stackedBlocks.keySet().forEach(this.stackedBlocks::remove);
        stackedEntities.keySet().forEach(this.stackedEntities::remove);
        stackedItems.keySet().forEach(this.stackedItems::remove);
        stackedSpawners.keySet().forEach(this.stackedSpawners::remove);
    }

    @Override
    public void run() {
        Set<Stack> removed = new HashSet<>();

        // Auto stack items
        for (StackedItem stackedItem : new HashSet<>(this.stackedItems.values())) {
            if (removed.contains(stackedItem))
                continue;

            if (!stackedItem.getItem().isValid()) {
                this.removeItem(stackedItem);
                continue;
            }

            Stack removedStack = this.tryStackEntity(stackedItem);
            if (removedStack != null)
                removed.add(removedStack);
        }

        // Auto stack entities
        for (StackedEntity stackedEntity : new HashSet<>(this.stackedEntities.values())) {
            if (removed.contains(stackedEntity))
                continue;

            if (!stackedEntity.getEntity().isValid()) {
                this.removeEntity(stackedEntity);
                continue;
            }

            Stack removedStack = this.tryStackEntity(stackedEntity);
            if (removedStack != null)
                removed.add(removedStack);
        }

        // Delete removed stacks
        this.deleteStacks();

        // Auto unstack entities
        for (StackedEntity stackedEntity : new HashSet<>(this.stackedEntities.values()))
            if (!stackedEntity.shouldStayStacked())
                this.splitEntity(stackedEntity);
    }

    private void deleteStacks() {
        this.roseStacker.getDataManager().deleteStacks(new HashSet<>(this.deletedStacks));
        for (Stack stack : this.deletedStacks) {
            if (stack instanceof StackedBlock) {
                this.stackedBlocks.remove(((StackedBlock) stack).getBlock());
            } else if (stack instanceof StackedEntity) {
                this.stackedEntities.remove(((StackedEntity) stack).getEntity().getUniqueId());
            } else if (stack instanceof StackedItem) {
                this.stackedItems.remove(((StackedItem) stack).getItem().getUniqueId());
            } else if (stack instanceof StackedSpawner) {
                this.stackedSpawners.remove(((StackedSpawner) stack).getSpawner().getBlock());
            }
        }
        this.deletedStacks.clear();
    }

    /**
     * Tries to stack a stack with all other stacks
     *
     * @param stack The stack to try stacking
     * @return if a stack was deleted, the stack that was deleted, otherwise null
     */
    private Stack tryStackEntity(Stack stack) {
        // TODO: Group by world

        if (stack instanceof StackedItem) {
            StackedItem stackedItem = (StackedItem) stack;

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
                if (stackSettings.canStackWith(stackedItem, other, false)) {
                    StackedItem increased = (StackedItem) this.getPreferredEntityStack(stackedItem, other);
                    StackedItem removed = increased == stackedItem ? other : stackedItem;

                    increased.increaseStackSize(removed.getStackSize());
                    increased.getItem().setTicksLived(1);
                    removed.getItem().remove();
                    this.removeItem(removed);

                    return removed;
                }
            }
        } else if (stack instanceof StackedEntity) {
            StackedEntity stackedEntity = (StackedEntity) stack;

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
                if (stackSettings.canStackWith(stackedEntity, other, false)) {
                    if (Setting.ENTITY_REQUIRE_LINE_OF_SIGHT.getBoolean() && !StackerUtils.hasLineOfSight(stackedEntity.getEntity(), other.getEntity()))
                        continue;

                    int minStackSize = stackSettings.getMinStackSize();
                    if (minStackSize > 2) {
                        int nearbyEntities = 0;
                        if (!Setting.ENTITY_MERGE_ENTIRE_CHUNK.getBoolean()) {
                            for (StackedEntity nearbyStackedEntity : this.stackedEntities.values()) {
                                if (this.deletedStacks.contains(nearbyStackedEntity))
                                    continue;

                                if (nearbyStackedEntity.getEntity().getType() == stackedEntity.getEntity().getType()
                                        && stackedEntity.getLocation().distanceSquared(nearbyStackedEntity.getLocation()) <= maxEntityStackDistanceSqrd
                                        && stackSettings.canStackWith(stackedEntity, nearbyStackedEntity, false))
                                    nearbyEntities += nearbyStackedEntity.getStackSize();
                            }
                        } else {
                            for (StackedEntity nearbyStackedEntity : this.stackedEntities.values()) {
                                if (this.deletedStacks.contains(nearbyStackedEntity))
                                    continue;

                                if (nearbyStackedEntity.getEntity().getType() == stackedEntity.getEntity().getType()
                                        && nearbyStackedEntity.getLocation().getChunk() == stackedEntity.getLocation().getChunk()
                                        && stackSettings.canStackWith(stackedEntity, nearbyStackedEntity, false))
                                    nearbyEntities += nearbyStackedEntity.getStackSize();
                            }
                        }

                        if (nearbyEntities < minStackSize)
                            continue;
                    }

                    StackedEntity increased = (StackedEntity) this.getPreferredEntityStack(stackedEntity, other);
                    StackedEntity removed = increased == stackedEntity ? other : stackedEntity;

                    removed.getEntity().setCustomName(removed.getOriginalCustomName());
                    increased.increaseStackSize(removed.getEntity());
                    increased.increaseStackSize(removed.getStackedEntityNBTStrings());
                    removed.getEntity().remove();
                    this.removeEntity(removed);

                    return removed;
                }
            }
        }

        return null;
    }

    private Stack getPreferredEntityStack(Stack stack1, Stack stack2) {
        Entity entity1, entity2;
        if (stack1 instanceof StackedItem) {
            entity1 = ((StackedItem) stack1).getItem();
            entity2 = ((StackedItem) stack2).getItem();
        } else if (stack1 instanceof StackedEntity) {
            entity1 = ((StackedEntity) stack1).getEntity();
            entity2 = ((StackedEntity) stack2).getEntity();
        } else {
            return null;
        }

        if (Setting.ENTITY_STACK_FLYING_DOWNWARDS.getBoolean() && entity1 instanceof Flying)
            return entity1.getLocation().getY() < entity2.getLocation().getY() ? stack1 : stack2;

        if (stack1.getStackSize() == stack2.getStackSize())
            return entity1.getTicksLived() > entity2.getTicksLived() ? stack1 : stack2;

        return stack1.getStackSize() > stack2.getStackSize() ? stack1 : stack2;
    }

    public void setEntityStackingDisabled(boolean disabled) {
        this.isEntityStackingDisabled = disabled;
    }

    public boolean isEntityStackingDisabled() {
        return this.isEntityStackingDisabled;
    }

    protected Map<Block, StackedSpawner> getStackedSpawners() {
        return Collections.unmodifiableMap(this.stackedSpawners);
    }

}
