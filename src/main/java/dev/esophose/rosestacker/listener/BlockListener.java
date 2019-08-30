package dev.esophose.rosestacker.listener;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.stack.StackedBlock;
import dev.esophose.rosestacker.stack.StackedSpawner;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class BlockListener implements Listener {

    private RoseStacker roseStacker;

    public BlockListener(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();

        Block block = event.getBlock();
        boolean isStacked = stackManager.isBlockStacked(block);
        boolean isSpawner = block.getType() == Material.SPAWNER;
        if (!isStacked && !isSpawner)
            return;

        Player player = event.getPlayer();
        boolean breakEverything = player.isSneaking();
        Location dropLocation = block.getLocation().clone().add(0.5, 0.5, 0.5);

        // TODO: Make break-entire-stack-while-sneaking setting, default true

        if (isSpawner) {
            // Always drop the correct spawner type even if it's not stackes
            if (!isStacked) {
                if (player.getGameMode() != GameMode.CREATIVE)
                    dropLocation.getWorld().dropItemNaturally(dropLocation, StackerUtils.getSpawnerAsStackedItemStack(((CreatureSpawner) block.getState()).getSpawnedType(), 1));
                block.setType(Material.AIR);
                event.setCancelled(true);
                return;
            }

            StackedSpawner stackedSpawner = stackManager.getStackedSpawner(block);
            if (breakEverything) {
                if (player.getGameMode() != GameMode.CREATIVE)
                    dropLocation.getWorld().dropItemNaturally(dropLocation, StackerUtils.getSpawnerAsStackedItemStack(((CreatureSpawner) block.getState()).getSpawnedType(), stackedSpawner.getStackSize()));
                stackedSpawner.setStackSize(0);
                block.setType(Material.AIR);
            } else {
                if (player.getGameMode() != GameMode.CREATIVE)
                    dropLocation.getWorld().dropItemNaturally(dropLocation, StackerUtils.getSpawnerAsStackedItemStack(((CreatureSpawner) block.getState()).getSpawnedType(), 1));
                stackedSpawner.increaseStackSize(-1);
            }

            if (stackedSpawner.getStackSize() <= 1)
                stackManager.removeBlock(block);
        } else {
            StackedBlock stackedBlock = stackManager.getStackedBlock(block);
            if (breakEverything) {
                if (player.getGameMode() != GameMode.CREATIVE)
                    StackerUtils.dropItems(dropLocation, new ItemStack(block.getType()), stackedBlock.getStackSize());
                stackedBlock.setStackSize(0);
                block.setType(Material.AIR);
            } else {
                if (player.getGameMode() != GameMode.CREATIVE)
                    dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(block.getType()));
                stackedBlock.increaseStackSize(-1);
            }

            if (stackedBlock.getStackSize() <= 1)
                stackManager.removeBlock(block);
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();
        if (stackManager.isBlockStacked(event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();

        for (Block block : new ArrayList<>(event.blockList()))
            if (stackManager.isBlockStacked(block))
                event.blockList().remove(block); // TODO: Configurable setting to destroy entire stack instead of protecting it
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();
        if (stackManager.isBlockStacked(event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();

        for (Block block : event.getBlocks()) {
            if (stackManager.isBlockStacked(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();

        for (Block block : event.getBlocks()) {
            if (stackManager.isBlockStacked(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();
        // TODO: auto stack range
        // TODO: max stack size

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Block against = event.getBlockAgainst();

        // Get the block in the player's hand that's being placed
        ItemStack placedItem = player.getInventory().getItemInMainHand();
        boolean isOffHand = false;
        if (placedItem.getType() == Material.AIR || !placedItem.getType().isBlock()) {
            placedItem = player.getInventory().getItemInOffHand();
            isOffHand = true;
        }

        // Will be true if we are adding to an existing stack (including a stack of 1), or false if we are creating a new one from an itemstack with a stack value
        boolean isAdditiveStack = against.getType() == block.getType();
        if (isAdditiveStack && against.getType() == Material.SPAWNER)
            isAdditiveStack = ((CreatureSpawner) against.getState()).getSpawnedType() == StackerUtils.getStackedItemEntityType(placedItem);

        int stackAmount = StackerUtils.getStackedItemStackAmount(placedItem);
        if (isAdditiveStack) {
            if (!stackManager.isBlockTypeStackable(against) || player.isSneaking())
                return;

            if (block.getType() == Material.SPAWNER) {
                // Handle spawner stacking
                StackedSpawner stackedSpawner = stackManager.getStackedSpawner(against);
                if (stackedSpawner == null)
                    stackedSpawner = (StackedSpawner) stackManager.createStackFromBlock(against, 1);

                stackedSpawner.increaseStackSize(stackAmount);
            } else {
                // Handle normal block stacking
                StackedBlock stackedBlock = stackManager.getStackedBlock(against);
                if (stackedBlock == null)
                    stackedBlock = (StackedBlock) stackManager.createStackFromBlock(against, 1);

                stackedBlock.increaseStackSize(stackAmount);
            }

            event.setCancelled(true);
        } else {
            // Handle placing spawners
            if (placedItem.getType() == Material.SPAWNER) {
                // Create a stacked spawner
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                EntityType spawnedType = StackerUtils.getStackedItemEntityType(placedItem);
                if (spawnedType == null)
                    return;

                // Set the spawner type
                spawner.setSpawnedType(spawnedType);
                spawner.update();
            } else {
                event.setCancelled(true);
            }

            // Don't bother creating a stack if we're only placing 1 of them
            if (stackAmount == 1)
                return;

            stackManager.createStackFromBlock(block, stackAmount);
        }

        // Take an item from the player's hand
        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        int newAmount = placedItem.getAmount() - 1;
        if (newAmount <= 0) {
            if (!isOffHand) {
                player.getInventory().setItemInMainHand(null);
            } else {
                player.getInventory().setItemInOffHand(null);
            }
        } else {
            placedItem.setAmount(newAmount);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();
        if (stackManager.isBlockStacked(event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();
        if (stackManager.isBlockStacked(event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExp(BlockExpEvent event) {
        // Don't want to be able to get exp from stacked spawners, so just remove it all together
        if (event.getBlock().getType() == Material.SPAWNER)
            event.setExpToDrop(0);
    }

}
