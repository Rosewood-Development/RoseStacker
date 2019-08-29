package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.stack.Stack;
import dev.esophose.rosestacker.stack.StackedItem;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class StackManager extends Manager implements Runnable {

    private BukkitTask task;

    private Set<StackedItem> stackedItems;

    public StackManager(RoseStacker roseStacker) {
        super(roseStacker);

        this.stackedItems = new HashSet<>();
    }

    @Override
    public void reload() {
        if (this.task != null)
            this.task.cancel();

        this.task = Bukkit.getScheduler().runTaskTimer(this.roseStacker, this, 0, 5);

        this.stackedItems.clear();

        // Load all existing entities in the world
        List<String> disabledWorlds = Setting.DISABLED_WORLDS.getStringList();
        for (World world : Bukkit.getWorlds()) {
            if (disabledWorlds.contains(world.getName()))
                continue;

            // Handle item entity stacking
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

    public void removeItem(StackedItem stackedItem) {
        this.stackedItems.remove(stackedItem);
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

        Item newItem = (Item) stackedItem.getLocation().getWorld().spawnEntity(stackedItem.getLocation(), EntityType.DROPPED_ITEM);
        newItem.setItemStack(newItemStack);
        newItem.setPickupDelay(0);

        stackedItem.getItem().setPickupDelay(20);

        StackedItem newStackedItem = new StackedItem(newSize, newItem);
        this.stackedItems.add(newStackedItem);
        return newStackedItem;
    }

    public void createStackFromEntity(Entity entity) {
        Stack newStack = null;

        // Load items
        if (entity instanceof Item) {
            Item item = (Item) entity;
            StackedItem newStackedItem = new StackedItem(item.getItemStack().getAmount(), item);
            this.stackedItems.add(newStackedItem);
            newStack = newStackedItem;
        }

        if (newStack != null)
            this.tryStack(newStack);
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

            Stack removedStack = this.tryStack(stackedItem);
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
    private Stack tryStack(Stack stack) {
        // Handle item entity stacking
        double maxStackDistanceSqrd = 1.5 * 1.5; // How far away should we stack items? // TODO: Configurable

        if (stack instanceof StackedItem) {
            StackedItem stackedItem = (StackedItem) stack;

            for (StackedItem other : this.stackedItems) {
                if (stackedItem == other
                        || stackedItem.getLocation().getWorld() != other.getLocation().getWorld()
                        || !stackedItem.getItem().getItemStack().isSimilar(other.getItem().getItemStack())
                        || other.getItem().getPickupDelay() > other.getItem().getTicksLived())
                    continue;

                // Check if we should merge the stacks
                if (stackedItem.getLocation().distanceSquared(other.getLocation()) <= maxStackDistanceSqrd) {
                    StackedItem increased;
                    StackedItem removed;

                    // Merge whichever stack is bigger
                    if (stackedItem.getStackSize() == other.getStackSize()) {
                        // Pick whichever has lived the shortest amount
                        if (stackedItem.getItem().getTicksLived() > other.getItem().getTicksLived()) {
                            increased = stackedItem;
                            removed = other;
                        } else {
                            increased = other;
                            removed = stackedItem;
                        }
                    } else if (stackedItem.getStackSize() > other.getStackSize()) {
                        increased = stackedItem;
                        removed = other;
                    } else {
                        increased = other;
                        removed = stackedItem;
                    }

                    increased.increaseStackSize(removed.getStackSize());
                    removed.getItem().remove();
                    this.stackedItems.remove(removed);

                    return removed;
                }
            }
        }

        return null;
    }

}
