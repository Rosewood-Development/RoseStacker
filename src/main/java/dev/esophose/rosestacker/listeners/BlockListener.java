package dev.esophose.rosestacker.listeners;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.stack.StackedBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
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

import java.util.ArrayList;

public class BlockListener implements Listener {

    private RoseStacker roseStacker;

    public BlockListener(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();

        // TODO: Handle breaking a single block and the entire stack at once if holding shift, configurable
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

        Block block = event.getBlock();
        Block against = event.getBlockAgainst();

        // Only stack similar types
        if (against.getType() != block.getType())
            return;

        // Handle spawner stacking
        if (block.getType() == Material.SPAWNER) {
            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            CreatureSpawner spawnerAgainst = (CreatureSpawner) against.getState();

            // Only stack spawners of the same type
            if (spawner.getSpawnedType() != spawnerAgainst.getSpawnedType())
                return;

            // TODO: Handle stacked spawners

            event.setCancelled(true);
            return;
        }

        // Handle normal block stacking
        StackedBlock stackedBlock = stackManager.getStackedBlock(against);
        if (stackedBlock == null) {
            stackedBlock = (StackedBlock) stackManager.createStackFromBlock(against, 1);
        }

        stackedBlock.increaseStackSize(1);

        event.setCancelled(true);
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
