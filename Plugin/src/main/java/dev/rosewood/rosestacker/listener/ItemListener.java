package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedItem;
import dev.rosewood.rosestacker.stack.settings.ItemStackSettings;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Container;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ItemListener implements Listener {

    private final RosePlugin rosePlugin;
    private final StackManager stackManager;
    private final StackSettingManager stackSettingManager;

    public ItemListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
        this.stackManager = rosePlugin.getManager(StackManager.class);
        this.stackSettingManager = rosePlugin.getManager(StackSettingManager.class);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        if (this.stackManager.isAreaDisabled(event.getEntity().getLocation()) || !this.stackManager.isItemStackingEnabled())
            return;

        StackedItem stackedItem = this.stackManager.getStackedItem(event.getEntity());
        if (stackedItem != null)
            this.stackManager.removeItemStack(stackedItem);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemMerge(ItemMergeEvent event) {
        if (this.stackManager.isAreaDisabled(event.getEntity().getLocation()) || !this.stackManager.isItemStackingEnabled())
            return;

        ItemStackSettings itemStackSettings = this.stackSettingManager.getItemStackSettings(event.getEntity());
        if (itemStackSettings != null && !itemStackSettings.isStackingEnabled())
            return;

        // We will handle all item merging ourselves, thank you very much
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        LivingEntity entity = event.getEntity();
        if (this.stackManager.isAreaDisabled(entity.getLocation()))
            return;

        if (!this.stackManager.isItemStackingEnabled())
            return;

        StackedItem stackedItem = this.stackManager.getStackedItem(event.getItem());
        if (stackedItem == null)
            return;

        int maxStack = event.getItem().getItemStack().getType().getMaxStackSize();

        Inventory inventory;
        if (entity instanceof Player player) {
            if (SettingKey.ITEM_DISABLE_PICKUP_IF_VANISHED.get() && StackerUtils.isVanished(player)) {
                event.setCancelled(true);
                return;
            }

            inventory = player.getInventory();
        } else if (entity instanceof Villager villager) {
            inventory = villager.getInventory();
        } else if (event.getEntityType() == EntityType.DOLPHIN) {
            // Only stop the dolphin from picking up the item if it's larger than a normal item would be, otherwise
            // cancel the event, give the dolphin an item of max normal stack size, and reduce the stacked item size
            if (stackedItem.getStackSize() > maxStack) {
                ItemStack clone = event.getItem().getItemStack().clone();
                clone.setAmount(maxStack);
                stackedItem.setStackSize(stackedItem.getStackSize() - maxStack);
                EntityEquipment equipment = entity.getEquipment();
                if (equipment != null)
                    equipment.setItemInMainHand(clone);

                // Stun the item temporarily to avoid it getting instantly stacked back into
                stackedItem.getItem().setPickupDelay(20);

                event.setCancelled(true);
            }
            return;
        } else if (NMSUtil.getVersionNumber() >= 19 && entity instanceof Allay allay) {
            // Similar to dolphin logic
            int pickedUpAmount = Math.min(maxStack, stackedItem.getStackSize()) - event.getRemaining();
            if (stackedItem.getStackSize() > maxStack) {
                ItemStack clone = event.getItem().getItemStack().clone();
                clone.setAmount(pickedUpAmount);
                stackedItem.setStackSize(stackedItem.getStackSize() - pickedUpAmount);
                allay.getInventory().addItem(clone);
                stackedItem.getItem().setPickupDelay(20);
                event.setCancelled(true);
            }
            return;
        } else {
            // Only pick up one item
            if (stackedItem.getStackSize() > 1) {
                this.stackManager.splitItemStack(stackedItem, 1);
                event.setCancelled(true);
            }

            if (SettingKey.ENTITY_DONT_STACK_IF_HAS_EQUIPMENT.get()) {
                this.rosePlugin.getScheduler().runTask(() -> {
                    StackedEntity stackedEntity = this.stackManager.getStackedEntity(entity);
                    if (stackedEntity != null)
                        this.stackManager.tryUnstackEntity(stackedEntity);
                });
            }

            return;
        }

        // This only runs for unvanished players and villagers
        if (this.applyInventoryItemPickup(inventory, stackedItem, entity))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryPickup(InventoryPickupItemEvent event) {
        if (this.stackManager.isAreaDisabled(event.getItem().getLocation()))
            return;

        if (!this.stackManager.isItemStackingEnabled())
            return;

        StackedItem stackedItem = this.stackManager.getStackedItem(event.getItem());
        if (stackedItem == null)
            return;

        Inventory inventory = event.getInventory();
        if (this.applyInventoryItemPickup(inventory, stackedItem, null)) {
            InventoryHolder holder;
            if (NMSUtil.isPaper()) {
                holder = inventory.getHolder(false);
            } else {
                holder = inventory.getHolder();
            }

            if (holder instanceof Container container)
                container.update(); // Fix comparators not updating for single-slot changes

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAnvilRenameSpawner(PrepareAnvilEvent event) {
        ItemStack item = event.getInventory().getFirstItem();
        if (item != null && item.getType() == Material.SPAWNER && SettingKey.SPAWNER_DISABLE_ITEM_ANVIL_RENAMING.get())
            event.setResult(null);
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
        ItemStack target = stackedItem.getItem().getItemStack();
        int maxStackSize = target.getMaxStackSize();

        // Check how much space the inventory has for the new items
        int inventorySpace = this.getAmountAvailable(inventory, target);

        // Just let them pick it up and remove the item if it will all fit
        if (inventorySpace >= stackedItem.getStackSize() && stackedItem.getStackSize() <= maxStackSize) {
            this.stackManager.removeItemStack(stackedItem);
            return false;
        }

        boolean willPickupAll = inventorySpace >= stackedItem.getStackSize();
        int amount = willPickupAll ? stackedItem.getStackSize() - target.getAmount() : inventorySpace;

        this.addItemStackAmountToInventory(inventory, target, amount);

        if (willPickupAll) {
            this.stackManager.removeItemStack(stackedItem);
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

        // Check player offhand slot first
        if (inventory instanceof PlayerInventory playerInventory) {
            ItemStack offhandStack = playerInventory.getItemInOffHand();
            if (offhandStack.isSimilar(target))
                inventorySpace += Math.max(maxStackSize - offhandStack.getAmount(), 0);
        }

        for (ItemStack itemStack : inventory.getStorageContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                inventorySpace += maxStackSize;
                continue;
            }

            if (itemStack.isSimilar(target))
                inventorySpace += Math.max(maxStackSize - itemStack.getAmount(), 0);
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

        // Prioritize the offhand slot
        if (inventory instanceof PlayerInventory playerInventory) {
            ItemStack itemStack = playerInventory.getItemInOffHand();
            if (itemStack.isSimilar(target)) {
                int available = Math.max(target.getMaxStackSize() - itemStack.getAmount(), 0);
                int toTake = Math.min(available, amount);
                if (toTake > 0) {
                    itemStack.setAmount(itemStack.getAmount() + toTake);
                    amount -= toTake;
                }
            }
        }

        // Handle the rest
        while (amount > 0) {
            ItemStack newItemStack = target.clone();
            int toTake = Math.min(amount, target.getMaxStackSize());
            newItemStack.setAmount(toTake);
            amount -= toTake;
            toAdd.add(newItemStack);
        }

        inventory.addItem(toAdd.toArray(new ItemStack[0])).isEmpty();
    }

}
