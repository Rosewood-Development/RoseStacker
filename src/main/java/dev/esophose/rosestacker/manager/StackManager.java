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
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class StackManager extends Manager implements Runnable {

    private BukkitTask task;

    private final Set<StackedBlock> stackedBlocks;
    private final Set<StackedEntity> stackedEntities;
    private final Set<StackedItem> stackedItems;
    private final Set<StackedSpawner> stackedSpawners;

    private final Set<Stack> deletedStacks;

    private boolean isEntityStackingDisabled;

    // Cached, as we will be using it a lot
    private StackSettingManager stackSettingManager;

    public StackManager(RoseStacker roseStacker) {
        super(roseStacker);

        this.stackedItems = Collections.synchronizedSet(new HashSet<>());
        this.stackedBlocks = Collections.synchronizedSet(new HashSet<>());
        this.stackedSpawners = Collections.synchronizedSet(new HashSet<>());
        this.stackedEntities = Collections.synchronizedSet(new HashSet<>());

        this.deletedStacks = new HashSet<>();

        this.isEntityStackingDisabled = false;
    }

    @Override
    public void reload() {
        if (this.task != null)
            this.task.cancel();

        this.task = Bukkit.getScheduler().runTaskTimer(this.roseStacker, this, 0, 5);
        this.stackSettingManager = this.roseStacker.getStackSettingManager();

        DataManager dataManager = this.roseStacker.getDataManager();

        // Save anything that's loaded
        dataManager.createOrUpdateStackedBlocksOrSpawners(this.stackedBlocks, false);
        dataManager.createOrUpdateStackedEntities(this.stackedEntities, false);
        dataManager.createOrUpdateStackedItems(this.stackedItems, false);
        dataManager.createOrUpdateStackedBlocksOrSpawners(this.stackedSpawners, false);

        // Clear existing stacks
        this.stackedBlocks.clear();
        this.stackedEntities.clear();
        this.stackedItems.clear();
        this.stackedSpawners.clear();

        // Load all existing entities in the worlds that are saved
        Set<Chunk> chunks = new HashSet<>();
        for (World world : Bukkit.getWorlds())
            chunks.addAll(Arrays.asList(world.getLoadedChunks()));

        dataManager.getStackedBlocks(chunks, false, this.stackedBlocks::addAll);
        dataManager.getStackedSpawners(chunks, false, this.stackedSpawners::addAll);

        // Due to using lambdas, we need to use an AtomicBoolean and listen for when both getting entities and items are finished
        AtomicBoolean allEntitiesLoaded = new AtomicBoolean(false);
        dataManager.getStackedEntities(chunks, false, (stacks) -> {
            this.stackedEntities.addAll(stacks);
            if (allEntitiesLoaded.getAndSet(true))
                this.populateUnstackedEntities();
        });
        dataManager.getStackedItems(chunks, false, (stacks) -> {
            this.stackedItems.addAll(stacks);
            if (allEntitiesLoaded.getAndSet(true))
                this.populateUnstackedEntities();
        });
    }

    private void populateUnstackedEntities() {
        // Create stacks of all existing entities that aren't stacks yet
        for (World world : Bukkit.getWorlds())
            for (Entity entity : world.getEntities())
                if (!this.isEntityStacked(entity))
                    this.createStackFromEntity(entity, true);
    }

    @Override
    public void disable() {
        this.task.cancel();

        // Save anything that's loaded
        DataManager dataManager = this.roseStacker.getDataManager();
        dataManager.createOrUpdateStackedBlocksOrSpawners(this.stackedBlocks, false);
        dataManager.createOrUpdateStackedEntities(this.stackedEntities, false);
        dataManager.createOrUpdateStackedItems(this.stackedItems, false);
        dataManager.createOrUpdateStackedBlocksOrSpawners(this.stackedSpawners, false);
    }

    public StackedItem getStackedItem(Item item) {
        if (!Setting.ITEM_STACKING_ENABLED.getBoolean() || this.isWorldDisabled(item))
            return null;

        for (StackedItem stackedItem : this.stackedItems)
            if (stackedItem.getItem().getUniqueId().equals(item.getUniqueId()))
                return stackedItem;
        return null;
    }

    public StackedBlock getStackedBlock(Block block) {
        if (!Setting.BLOCK_STACKING_ENABLED.getBoolean() || this.isWorldDisabled(block))
            return null;

        for (StackedBlock stackedBlock : this.stackedBlocks)
            if (stackedBlock.getBlock().equals(block))
                return stackedBlock;
        return null;
    }

    public StackedSpawner getStackedSpawner(Block block) {
        if (!Setting.SPAWNER_STACKING_ENABLED.getBoolean() || this.isWorldDisabled(block))
            return null;

        for (StackedSpawner stackedSpawner : this.stackedSpawners)
            if (stackedSpawner.getSpawner().getBlock().equals(block))
                return stackedSpawner;
        return null;
    }

    public StackedEntity getStackedEntity(Entity entity) {
        if (!Setting.ENTITY_STACKING_ENABLED.getBoolean() || this.isWorldDisabled(entity))
            return null;

        for (StackedEntity stackedEntity : this.stackedEntities)
            if (stackedEntity.getEntity().getUniqueId().equals(entity.getUniqueId()))
                return stackedEntity;
        return null;
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
        return this.getStackedEntity(entity) != null;
    }

    /**
     * Checks if a given block type is able to be stacked
     *
     * @param block The block to check
     * @return true if the block is stackable, otherwise false
     */
    public boolean isBlockTypeStackable(Block block) {
        BlockStackSettings blockStackSettings = this.roseStacker.getStackSettingManager().getBlockStackSettings(block);
        return blockStackSettings.isStackingEnabled();
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
        this.deletedStacks.add(stackedItem);
    }

    public void removeEntity(StackedEntity stackedEntity) {
        this.deletedStacks.add(stackedEntity);
    }

    public void removeBlock(Block block) {
        if (!this.isBlockStacked(block))
            return;

        if (block.getType() == Material.SPAWNER) {
            StackedSpawner stackedSpawner = this.getStackedSpawner(block);
            this.deletedStacks.add(stackedSpawner);
        } else {
            StackedBlock stackedBlock = this.getStackedBlock(block);
            this.deletedStacks.add(stackedBlock);
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
        this.stackedItems.add(newStackedItem);
        stackedItem.increaseStackSize(-newSize);
        return newStackedItem;
    }

    public StackedEntity splitEntity(StackedEntity stackedEntity) {
        StackedEntity newlySplit = stackedEntity.split();
        this.stackedEntities.add(newlySplit);
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
            this.stackedItems.add(newStackedItem);
            newStack = newStackedItem;
        } else if (entity instanceof LivingEntity) {
            if (!Setting.ENTITY_STACKING_ENABLED.getBoolean())
                return null;

            LivingEntity livingEntity = (LivingEntity) entity;
            if (livingEntity instanceof Player || livingEntity instanceof ArmorStand)
                return null;

            StackedEntity newStackedEntity = new StackedEntity(livingEntity, new LinkedList<>());
            this.stackedEntities.add(newStackedEntity);
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
            this.stackedSpawners.add(stackedSpawner);
            newStack = stackedSpawner;
        } else {
            if (!Setting.BLOCK_STACKING_ENABLED.getBoolean())
                return null;

            StackedBlock stackedBlock = new StackedBlock(amount, block);
            this.stackedBlocks.add(stackedBlock);
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
        this.stackedItems.addAll(stackedItems);

        this.setEntityStackingDisabled(false);
    }

    public void loadChunk(Chunk chunk) {
        DataManager dataManager = this.roseStacker.getDataManager();

        Set<Chunk> singletonChunk = Collections.singleton(chunk);

        dataManager.getStackedBlocks(singletonChunk, true, this.stackedBlocks::addAll);
        dataManager.getStackedEntities(singletonChunk, true, this.stackedEntities::addAll);
        dataManager.getStackedItems(singletonChunk, true, this.stackedItems::addAll);
        dataManager.getStackedSpawners(singletonChunk, true, this.stackedSpawners::addAll);
    }

    public void unloadChunk(Chunk chunk) {
        DataManager dataManager = this.roseStacker.getDataManager();

        Set<Stack> stackedBlocks = this.stackedBlocks.stream().filter(x -> x.getLocation().getChunk() == chunk).collect(Collectors.toSet());
        Set<StackedEntity> stackedEntities = this.stackedEntities.stream().filter(x -> x.getLocation().getChunk() == chunk).collect(Collectors.toSet());
        Set<StackedItem> stackedItems = this.stackedItems.stream().filter(x -> x.getLocation().getChunk() == chunk).collect(Collectors.toSet());
        Set<Stack> stackedSpawners = this.stackedSpawners.stream().filter(x -> x.getLocation().getChunk() == chunk).collect(Collectors.toSet());

        dataManager.createOrUpdateStackedBlocksOrSpawners(stackedBlocks, true);
        dataManager.createOrUpdateStackedEntities(stackedEntities, true);
        dataManager.createOrUpdateStackedItems(stackedItems, true);
        dataManager.createOrUpdateStackedBlocksOrSpawners(stackedSpawners, true);

        this.stackedBlocks.removeAll(stackedBlocks);
        this.stackedEntities.removeAll(stackedEntities);
        this.stackedItems.removeAll(stackedItems);
        this.stackedSpawners.removeAll(stackedSpawners);
    }

    @Override
    public void run() {
        Set<Stack> removed = new HashSet<>();

        // Auto stack items
        for (StackedItem stackedItem : new HashSet<>(this.stackedItems)) {
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
        for (StackedEntity stackedEntity : new HashSet<>(this.stackedEntities)) {
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
        this.roseStacker.getDataManager().deleteStacks(new HashSet<>(this.deletedStacks));
        for (Stack stack : this.deletedStacks) {
            if (stack instanceof StackedBlock) {
                this.stackedBlocks.remove(stack);
            } else if (stack instanceof StackedEntity) {
                this.stackedEntities.remove(stack);
            } else if (stack instanceof StackedItem) {
                this.stackedItems.remove(stack);
            } else if (stack instanceof StackedSpawner) {
                this.stackedSpawners.remove(stack);
            }
        }
        this.deletedStacks.clear();

        // Auto unstack entities
        for (StackedEntity stackedEntity : new HashSet<>(this.stackedEntities))
            if (!stackedEntity.shouldStayStacked())
                this.splitEntity(stackedEntity);
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

            for (StackedItem other : this.stackedItems) {
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

            for (StackedEntity other : this.stackedEntities) {
                if (stackedEntity == other
                        || other.getEntity() == null
                        || !other.getEntity().isValid()
                        || stackedEntity.getLocation().getWorld() != other.getLocation().getWorld()
                        || stackedEntity.getEntity() == other.getEntity()
                        || stackedEntity.getEntity().getType() != other.getEntity().getType()
                        || stackedEntity.getLocation().distanceSquared(other.getLocation()) > maxEntityStackDistanceSqrd)
                    continue;

                // Check if we should merge the stacks
                EntityStackSettings stackSettings = this.stackSettingManager.getEntityStackSettings(stackedEntity.getEntity());
                if (stackSettings.canStackWith(stackedEntity, other, false)) {
                    if (Setting.ENTITY_REQUIRE_LINE_OF_SIGHT.getBoolean() && !StackerUtils.hasLineOfSight(stackedEntity.getEntity(), other.getEntity()))
                        return null;

                    StackedEntity increased = (StackedEntity) this.getPreferredEntityStack(stackedEntity, other);
                    StackedEntity removed = increased == stackedEntity ? other : stackedEntity;

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

}
