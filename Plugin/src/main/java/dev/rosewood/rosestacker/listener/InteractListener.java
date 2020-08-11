package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.utils.StackerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InteractListener implements Listener {

    private RoseStacker roseStacker;

    public InteractListener(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        ItemStack item = event.getItem();
        if (item == null || event.getAction() != Action.RIGHT_CLICK_BLOCK || clickedBlock == null)
            return;

        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        if (!stackManager.isSpawnerStackingEnabled())
            return;

        // Handle spawner conversion before we try to spawn entities
        if (clickedBlock.getType() == Material.SPAWNER
                && StackerUtils.isSpawnEgg(item.getType())
                && StackerUtils.getStackedItemStackAmount(item) == 1) {

            if (!event.getPlayer().hasPermission("rosestacker.spawnerconvert")) {
                event.setCancelled(true);
                return;
            }

            Bukkit.getScheduler().runTask(this.roseStacker, () -> {
                if (!stackManager.isSpawnerStacked(clickedBlock))
                    return;

                // Make sure spawners convert and update their display properly
                StackedSpawner stackedSpawner = stackManager.getStackedSpawner(clickedBlock);
                stackedSpawner.updateSpawnerProperties();
                stackedSpawner.updateDisplay();
            });

            return;
        }

        Location spawnLocation = clickedBlock.getRelative(event.getBlockFace()).getLocation();
        spawnLocation.add(0.5, 0, 0.5); // Center on block

        if (this.spawnEntities(null, spawnLocation, item)) {
            StackerUtils.takeOneItem(event.getPlayer(), event.getHand());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity))
            return;

        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        if (!stackManager.isEntityStackingEnabled())
            return;

        LivingEntity entity = (LivingEntity) event.getRightClicked();
        if (!stackManager.isEntityStacked(entity))
            return;

        Player player = event.getPlayer();
        ItemStack itemStack = event.getHand() == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
        if (itemStack.getType() == Material.NAME_TAG) {
            if (!stackManager.isEntityStacked(entity))
                return;

            StackedEntity stackedEntity = stackManager.getStackedEntity(entity);
            Bukkit.getScheduler().runTask(this.roseStacker, stackedEntity::updateOriginalCustomName);
            return;
        } else if (itemStack.getType() == Material.WATER_BUCKET) {
            switch (entity.getType()) {
                case COD:
                case SALMON:
                case PUFFERFISH:
                case TROPICAL_FISH:
                    break;
                default: return;
            }

            if (!stackManager.isEntityStacked(entity))
                return;

            StackedEntity stackedEntity = stackManager.getStackedEntity(entity);
            stackedEntity.restoreOriginalCustomName();
            Bukkit.getScheduler().runTask(this.roseStacker, stackedEntity::decreaseStackSize);
        }

        if (this.spawnEntities(entity, entity.getLocation(), itemStack)) {
            StackerUtils.takeOneItem(event.getPlayer(), event.getHand());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDispenserDispense(BlockDispenseEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.DISPENSER)
            return;

        ItemStack itemStack = event.getItem();
        Location spawnLocation = block.getRelative(((Directional) block.getBlockData()).getFacing()).getLocation();
        spawnLocation.add(0.5, 0, 0.5);

        if (this.spawnEntities(null, spawnLocation, itemStack)) {
            Inventory inventory = ((Container) block.getState()).getInventory();
            Bukkit.getScheduler().runTask(this.roseStacker, () -> {
                for (int slot = 0; slot < inventory.getSize(); slot++) {
                    ItemStack item = inventory.getItem(slot);
                    if (item == null || !item.isSimilar(itemStack))
                        continue;
                    item.setAmount(item.getAmount() - 1);
                    if (item.getAmount() < 0)
                        inventory.setItem(slot, null);
                    break;
                }
            });

            event.setCancelled(true);
        }
    }

    private boolean spawnEntities(Entity original, Location spawnLocation, ItemStack itemStack) {
        if (!StackerUtils.isSpawnEgg(itemStack.getType()))
            return false;

        int spawnAmount = StackerUtils.getStackedItemStackAmount(itemStack);

        EntityStackSettings stackSettings = this.roseStacker.getManager(StackSettingManager.class).getEntityStackSettings(itemStack.getType());
        EntityType entityType = stackSettings.getEntityType();
        if (original != null && original.getType() != entityType)
            return false;

        if (spawnLocation.getWorld() == null)
            return false;

        this.roseStacker.getManager(StackManager.class).preStackEntities(entityType, spawnAmount, spawnLocation);

        return true;
    }

}
