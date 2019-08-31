package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.stack.Stack;
import dev.esophose.rosestacker.stack.StackedBlock;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.StackedItem;
import dev.esophose.rosestacker.stack.StackedSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class StackManager extends Manager implements Runnable {

    private BukkitTask task;

    private Set<StackedItem> stackedItems;
    private Set<StackedBlock> stackedBlocks;
    private Set<StackedSpawner> stackedSpawners;
    private Set<StackedEntity> stackedEntities;

    private Set<Material> stackableBlockMaterials;

    private boolean isEntityStackingDisabled;

    public StackManager(RoseStacker roseStacker) {
        super(roseStacker);

        this.stackedItems = new HashSet<>();
        this.stackedBlocks = new HashSet<>();
        this.stackedSpawners = new HashSet<>();
        this.stackedEntities = new HashSet<>();

        this.stackableBlockMaterials = new HashSet<>();

        this.isEntityStackingDisabled = false;
    }

    @Override
    public void reload() {
        if (this.task != null)
            this.task.cancel();

        this.task = Bukkit.getScheduler().runTaskTimer(this.roseStacker, this, 0, 5);

        this.stackedItems.clear();
        this.stackedBlocks.clear();
        this.stackedSpawners.clear();
        this.stackedEntities.clear();

        this.stackableBlockMaterials.clear();

        for (String materialName : Setting.STACKABLE_BLOCKS.getStringList())
            this.stackableBlockMaterials.add(Material.matchMaterial(materialName));
        this.stackableBlockMaterials.add(Material.SPAWNER);

        // Load all existing entities in the world
        List<String> disabledWorlds = Setting.DISABLED_WORLDS.getStringList();
        for (World world : Bukkit.getWorlds()) {
            if (disabledWorlds.contains(world.getName()))
                continue;

            world.getEntities().forEach(this::createStackFromEntity);
        }
    }

    @Override
    public void disable() {
        this.task.cancel();
    }

    public StackedItem getStackedItem(Item item) {
        for (StackedItem stackedItem : this.stackedItems)
            if (stackedItem.getItem().equals(item))
                return stackedItem;
        return null;
    }

    public StackedBlock getStackedBlock(Block block) {
        for (StackedBlock stackedBlock : this.stackedBlocks)
            if (stackedBlock.getBlock().equals(block))
                return stackedBlock;
        return null;
    }

    public StackedSpawner getStackedSpawner(Block block) {
        for (StackedSpawner stackedSpawner : this.stackedSpawners)
            if (stackedSpawner.getSpawner().equals(block.getState()))
                return stackedSpawner;
        return null;
    }

    public StackedEntity getStackedEntity(Entity entity) {
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
        return this.getStackedEntity(entity) != null || (entity instanceof Item && this.getStackedItem((Item) entity) != null);
    }

    /**
     * Checks if a given block type is able to be stacked
     *
     * @param block The block to check
     * @return true if the block is stackable, otherwise false
     */
    public boolean isBlockTypeStackable(Block block) {
        return this.stackableBlockMaterials.contains(block.getType());
    }

    public void removeItem(StackedItem stackedItem) {
        this.stackedItems.remove(stackedItem);
    }

    public void removeEntity(StackedEntity stackedEntity) {
        this.stackedEntities.remove(stackedEntity);
    }

    public void removeBlock(Block block) {
        if (!this.isBlockStacked(block))
            return;

        if (block.getType() == Material.SPAWNER) {
            this.stackedSpawners.remove(this.getStackedSpawner(block));
        } else {
            this.stackedBlocks.remove(this.getStackedBlock(block));
        }
    }

    /**
     * Splits a StackedItem into two stacked
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

    public Stack createStackFromEntity(Entity entity) {
        Stack newStack = null;

        // Stack items
        if (entity instanceof Item) {
            Item item = (Item) entity;
            StackedItem newStackedItem = new StackedItem(item.getItemStack().getAmount(), item);
            this.stackedItems.add(newStackedItem);
            newStack = newStackedItem;
        } else if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            StackedEntity newStackedEntity = new StackedEntity(livingEntity, new LinkedList<>());
            this.stackedEntities.add(newStackedEntity);
            newStack = newStackedEntity;
        }

        if (newStack != null)
            this.tryStackEntity(newStack);

        return newStack;
    }

    public Stack createStackFromBlock(Block block, int amount) {
        Stack newStack;

        // Stack spawners
        if (block.getType() == Material.SPAWNER) {
            StackedSpawner stackedSpawner = new StackedSpawner(amount, (CreatureSpawner) block.getState());
            this.stackedSpawners.add(stackedSpawner);
            newStack = stackedSpawner;
        } else {
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
        for (Entity entity : chunk.getEntities())
            this.createStackFromEntity(entity);
    }

    public void unloadChunk(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            // Unload items
            if (entity instanceof Item)
                this.stackedItems.remove(entity);
        }
    }

    @Override
    public void run() {
        Set<Stack> removed = new HashSet<>();

        for (StackedItem stackedItem : new ArrayList<>(this.stackedItems)) {
            if (removed.contains(stackedItem))
                continue;

            if (!stackedItem.getItem().isValid()) {
                this.stackedItems.remove(stackedItem);
                continue;
            }

            Stack removedStack = this.tryStackEntity(stackedItem);
            if (removedStack != null)
                removed.add(removedStack);
        }

        for (StackedEntity stackedEntity : new ArrayList<>(this.stackedEntities)) {
            if (removed.contains(stackedEntity))
                continue;

            if (!stackedEntity.getEntity().isValid()) {
                this.stackedEntities.remove(stackedEntity);
                continue;
            }

            Stack removedStack = this.tryStackEntity(stackedEntity);
            if (removedStack != null)
                removed.add(removedStack);
        }
    }

    /**
     * Tries to stack a stack with all other stacks
     *
     * @param stack The stack to try stacking
     * @return if a stack was deleted, the stack that was deleted, otherwise null
     */
    private Stack tryStackEntity(Stack stack) {
        double maxItemStackDistanceSqrd = 1.5 * 1.5; // How far away should we stack items? // TODO: Configurable
        double maxEntityStackDistanceSqrd = 3 * 3; // How far away should we stack entities? // TODO: Configurable

        // TODO: Group by world

        if (stack instanceof StackedItem) {
            StackedItem stackedItem = (StackedItem) stack;

            if (stackedItem.getItem().getPickupDelay() > stackedItem.getItem().getPickupDelay())
                return null;

            for (StackedItem other : this.stackedItems) {
                if (stackedItem == other
                        || !other.getItem().isValid()
                        || stackedItem.getLocation().getWorld() != other.getLocation().getWorld()
                        || !stackedItem.getItem().getItemStack().isSimilar(other.getItem().getItemStack())
                        || other.getItem().getPickupDelay() > other.getItem().getTicksLived())
                    continue;

                // Check if we should merge the stacks
                if (stackedItem.getLocation().distanceSquared(other.getLocation()) <= maxItemStackDistanceSqrd) {
                    StackedItem increased = (StackedItem) this.getPreferredEntityStack(stackedItem, other);
                    StackedItem removed = increased == stackedItem ? other : stackedItem;

                    increased.increaseStackSize(removed.getStackSize());
                    increased.getItem().setTicksLived(1);
                    removed.getItem().remove();
                    this.stackedItems.remove(removed);

                    return removed;
                }
            }
        } else if (stack instanceof StackedEntity) {
            StackedEntity stackedEntity = (StackedEntity) stack;

            for (StackedEntity other : this.stackedEntities) {
                if (stackedEntity == other
                        || other.getEntity() == null
                        || !other.getEntity().isValid()
                        || stackedEntity.getLocation().getWorld() != other.getLocation().getWorld()
                        || stackedEntity.getEntity().getType() != other.getEntity().getType())
                    continue;

                // Check if we should merge the stacks
                if (stackedEntity.getLocation().distanceSquared(other.getLocation()) <= maxEntityStackDistanceSqrd) {
                    StackedEntity increased = (StackedEntity) this.getPreferredEntityStack(stackedEntity, other);
                    StackedEntity removed = increased == stackedEntity ? other : stackedEntity;

                    increased.increaseStackSize(removed.getEntity());
                    increased.increaseStackSize(removed.getStackedEntityNBTStrings());
                    removed.getEntity().remove();
                    this.stackedEntities.remove(removed);

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
