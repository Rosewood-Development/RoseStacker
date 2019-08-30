package dev.esophose.rosestacker.listeners;

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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
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
        if (!stackManager.isBlockStacked(block))
            return;

        Player player = event.getPlayer();
        boolean breakEverything = player.isSneaking();
        Location dropLocation = block.getLocation().clone().add(0.5, 0.5, 0.5);

        // TODO: Make break-entire-stack-while-sneaking setting, default true

        if (block.getType() == Material.SPAWNER) {
            StackedSpawner stackedSpawner = stackManager.getStackedSpawner(block);
            if (breakEverything) {
                // TODO: Drop a pre-stacked itemstack of the spawner
            } else {
                // TODO: Drop a spawner with metadata to tell what type it is
            }

            if (stackedSpawner.getStackSize() == 1)
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
        // TODO: pre-stacked ItemStacks to place large amounts in one go

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Block against = event.getBlockAgainst();

        // Only stack similar types
        if (!stackManager.isBlockTypeStackable(against) || against.getType() != block.getType() || player.isSneaking())
            return;

        // Handle spawner stacking
        if (block.getType() == Material.SPAWNER) {
            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            CreatureSpawner spawnerAgainst = (CreatureSpawner) against.getState();

            // Only stack spawners of the same type
            if (spawner.getSpawnedType() != spawnerAgainst.getSpawnedType())
                return;

            // TODO: Handle stacked spawners
        } else {
            // Handle normal block stacking
            StackedBlock stackedBlock = stackManager.getStackedBlock(against);
            if (stackedBlock == null)
                stackedBlock = (StackedBlock) stackManager.createStackFromBlock(against, 1);

            stackedBlock.increaseStackSize(1);
        }

        event.setCancelled(true);

        // Take an item from the player's hand
        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        ItemStack target = player.getInventory().getItemInMainHand();
        boolean isOffHand = false;
        if (target.getType() == Material.AIR || !target.getType().isBlock()) {
            target = player.getInventory().getItemInOffHand();
            isOffHand = true;
        }

        int newAmount = target.getAmount() - 1;
        if (newAmount <= 0) {
            if (!isOffHand) {
                player.getInventory().setItemInMainHand(null);
            } else {
                player.getInventory().setItemInOffHand(null);
            }
        } else {
            target.setAmount(newAmount);
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

}
