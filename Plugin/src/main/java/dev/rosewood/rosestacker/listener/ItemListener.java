package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackedItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ItemListener implements Listener {

    private RosePlugin rosePlugin;

    public ItemListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getEntity().getWorld()) || !stackManager.isItemStackingEnabled())
            return;

        StackedItem stackedItem = stackManager.getStackedItem(event.getEntity());
        if (stackedItem != null)
            stackManager.removeItemStack(stackedItem);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemMerge(ItemMergeEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getEntity().getWorld()) || !stackManager.isItemStackingEnabled())
            return;

        // We will handle all item merging ourselves, thank you very much
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getEntity().getWorld()))
            return;

        if (!stackManager.isItemStackingEnabled())
            return;

        StackedItem stackedItem = stackManager.getStackedItem(event.getItem());
        if (stackedItem == null)
            return;

        Inventory inventory;
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            inventory = player.getInventory();
        } else if (event.getEntity() instanceof Villager) {
            Villager villager = (Villager) event.getEntity();
            inventory = villager.getInventory();
        } else {
            // Only pick up one item
            if (stackedItem.getStackSize() > 1) {
                stackManager.splitItemStack(stackedItem, 1);
                event.setCancelled(true);
            }
            return;
        }

        if (this.applyInventoryItemPickup(inventory, stackedItem, event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryPickup(InventoryPickupItemEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getItem().getWorld()))
            return;

        if (!stackManager.isItemStackingEnabled())
            return;

        StackedItem stackedItem = stackManager.getStackedItem(event.getItem());
        if (stackedItem == null)
            return;

        Inventory inventory = event.getInventory();

        if (this.applyInventoryItemPickup(inventory, stackedItem, null))
            event.setCancelled(true);
    }

    /**
     * Applies a stacked item pickup to an inventory
     *
     * @param inventory The inventory to apply to
     * @param stackedItem The stacked item to pick up
     * @param eventEntity The entity picking up the item, or null
     * @return true if the pickup event should be cancelled, otherwise false
     */
    private boolean applyInventoryItemPickup(Inventory inventory, StackedItem stackedItem, Entity eventEntity) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);

        ItemStack target = stackedItem.getItem().getItemStack();
        int maxStackSize = target.getMaxStackSize();

        // Check how much space the inventory has for the new items
        int inventorySpace = this.getAmountAvailable(inventory, target);

        // Just let them pick it up if it will all fit
        if (inventorySpace >= stackedItem.getStackSize() && stackedItem.getStackSize() <= maxStackSize) {
            stackManager.removeItemStack(stackedItem);
            return false;
        }

        boolean willPickupAll = inventorySpace >= stackedItem.getStackSize();
        int amount = willPickupAll ? stackedItem.getStackSize() : inventorySpace;

        this.addItemStackAmountToInventory(inventory, target, amount);

        if (willPickupAll) {
            stackManager.removeItemStack(stackedItem);
        } else {
            stackedItem.setStackSize(stackedItem.getStackSize() - amount);

            // Play a pickup sound since one won't naturally play
            if (eventEntity instanceof Player)
                eventEntity.getWorld().playSound(eventEntity.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2F, (float) (1 + Math.random()));

            return true;
        }

        return false;
    }

    /**
     * Gets the amount of item spaces that are available in an inventory for a given ItemStack
     *
     * @param inventory The inventory to check
     * @param target The target item type
     * @return the number of available item spaces available
     */
    private int getAmountAvailable(Inventory inventory, ItemStack target) {
        int maxStackSize = target.getMaxStackSize();

        int inventorySpace = 0;
        for (ItemStack itemStack : inventory.getStorageContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                inventorySpace += maxStackSize;
                continue;
            }

            if (itemStack.isSimilar(target))
                inventorySpace += Math.max(maxStackSize - itemStack.getAmount(), 0);
        }

        if (inventory instanceof PlayerInventory) {
            PlayerInventory playerInventory = (PlayerInventory) inventory;
            for (ItemStack itemStack : playerInventory.getExtraContents()) {
                // Can't pick up items directly to the offhand slot unless it already has that item in it
                if (itemStack == null || itemStack.getType() == Material.AIR)
                    continue;

                if (itemStack.isSimilar(target))
                    inventorySpace += Math.max(maxStackSize - itemStack.getAmount(), 0);
            }
        }

        return inventorySpace;
    }

    /**
     * Adds a certain number of an item stack to an inventory
     *
     * @param inventory The Inventory to add items to
     * @param target The target ItemStack type to add
     * @param amount The amount of the ItemStack to add
     */
    private void addItemStackAmountToInventory(Inventory inventory, ItemStack target, int amount) {
        List<ItemStack> toAdd = new ArrayList<>();

        while (amount > 0) {
            ItemStack newItemStack = target.clone();
            int toTake = Math.min(amount, target.getMaxStackSize());
            newItemStack.setAmount(toTake);
            amount -= toTake;
            toAdd.add(newItemStack);
        }

        if (!inventory.addItem(toAdd.toArray(new ItemStack[0])).isEmpty())
            throw new IllegalStateException("Added more items to inventory than was possible!");
    }

}
