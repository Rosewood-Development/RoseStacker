package dev.esophose.rosestacker.listener;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.stack.StackedBlock;
import dev.esophose.rosestacker.stack.StackedSpawner;
import dev.esophose.rosestacker.stack.settings.BlockStackSettings;
import dev.esophose.rosestacker.stack.settings.SpawnerStackSettings;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
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
            // Always drop the correct spawner type even if it's not stacked
            if (!isStacked) {
                this.tryDropSpawners(player, dropLocation, ((CreatureSpawner) block.getState()).getSpawnedType(), 1);
                block.setType(Material.AIR);
                event.setCancelled(true);
                return;
            }

            StackedSpawner stackedSpawner = stackManager.getStackedSpawner(block);
            if (breakEverything) {
                this.tryDropSpawners(player, dropLocation, ((CreatureSpawner) block.getState()).getSpawnedType(), stackedSpawner.getStackSize());
                stackedSpawner.setStackSize(0);
                block.setType(Material.AIR);
            } else {
                this.tryDropSpawners(player, dropLocation, ((CreatureSpawner) block.getState()).getSpawnedType(), 1);
                stackedSpawner.increaseStackSize(-1);
            }

            if (stackedSpawner.getStackSize() <= 1)
                stackManager.removeSpawner(stackedSpawner);
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
                stackManager.removeBlock(stackedBlock);
        }

        event.setCancelled(true);
    }

    private void tryDropSpawners(Player player, Location dropLocation, EntityType spawnedType, int amount) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (player.getGameMode() == GameMode.CREATIVE
                || dropLocation.getWorld() == null
                || !itemInHand.getType().name().endsWith("PICKAXE"))
            return;

        if (Setting.SPAWNER_SILK_TOUCH_REQUIRED.getBoolean()) {
            if (Setting.SPAWNER_SILK_TOUCH_REQUIRE_PERMISSION.getBoolean() && !player.hasPermission("rosestacker.silktouch"))
                return;

            int silkTouchLevel = itemInHand.getEnchantmentLevel(Enchantment.SILK_TOUCH);
            int dropAmount = amount;
            if (!Setting.SPAWNER_SILK_TOUCH_GUARANTEE.getBoolean() || silkTouchLevel < 2) {
                for (int i = 0; i < amount; i++) {
                    boolean passesChance = StackerUtils.passesChance(Setting.SPAWNER_SILK_TOUCH_CHANCE.getInt() / 100D);
                    if (!passesChance || silkTouchLevel == 0)
                        dropAmount--;
                }
            }
            int destroyAmount = amount - dropAmount;

            if (dropAmount > 0) {
                ItemStack spawnerItem = StackerUtils.getSpawnerAsStackedItemStack(spawnedType, dropAmount);
                if (Setting.SPAWNER_DROP_TO_INVENTORY.getBoolean()) {
                    StackerUtils.dropToInventory(player, spawnerItem);
                } else {
                    dropLocation.getWorld().dropItemNaturally(dropLocation, spawnerItem);
                }
            }

            if (destroyAmount > 0)
                StackerUtils.dropExperience(dropLocation, 15 * destroyAmount, 43 * destroyAmount, 10);
        } else {
            ItemStack spawnerItem = StackerUtils.getSpawnerAsStackedItemStack(spawnedType, amount);
            if (Setting.SPAWNER_DROP_TO_INVENTORY.getBoolean()) {
                StackerUtils.dropToInventory(player, spawnerItem);
            } else {
                dropLocation.getWorld().dropItemNaturally(dropLocation, spawnerItem);
            }
        }
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
    public void onEntityExplode(EntityExplodeEvent event) {
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

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Block against = event.getBlockAgainst();

        if (against.equals(block))
            against = against.getRelative(BlockFace.DOWN);

        if (block.getType() == Material.SPAWNER) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
            SpawnerStackSettings spawnerStackSettings = this.roseStacker.getStackSettingManager().getSpawnerStackSettings(creatureSpawner);
            if (!spawnerStackSettings.isStackingEnabled())
                return;
        } else {
            BlockStackSettings blockStackSettings = this.roseStacker.getStackSettingManager().getBlockStackSettings(block);
            if (!blockStackSettings.isStackingEnabled())
                return;
        }

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
        if (isAdditiveStack && !player.isSneaking()) {
            if (!stackManager.isBlockTypeStackable(against))
                return;

            if (block.getType() == Material.SPAWNER) {
                // Handle spawner stacking
                StackedSpawner stackedSpawner = stackManager.getStackedSpawner(against);
                if (stackedSpawner == null)
                    stackedSpawner = (StackedSpawner) stackManager.createStackFromBlock(against, 1);

                if (stackedSpawner.getStackSize() + stackAmount > Setting.SPAWNER_MAX_STACK_SIZE.getInt()) {
                    event.setCancelled(true);
                    return;
                }

                stackedSpawner.increaseStackSize(stackAmount);
            } else {
                // Handle normal block stacking
                StackedBlock stackedBlock = stackManager.getStackedBlock(against);
                if (stackedBlock == null)
                    stackedBlock = (StackedBlock) stackManager.createStackFromBlock(against, 1);

                if (stackedBlock.getStackSize() + stackAmount > Setting.BLOCK_MAX_STACK_SIZE.getInt()) {
                    event.setCancelled(true);
                    return;
                }

                stackedBlock.increaseStackSize(stackAmount);
            }

            event.setCancelled(true);
        } else {
            // Set the spawner type
            if (placedItem.getType() == Material.SPAWNER) {
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                EntityType spawnedType = StackerUtils.getStackedItemEntityType(placedItem);
                if (spawnedType == null)
                    return;

                spawner.setSpawnedType(spawnedType);
                spawner.update();
            }

            // Only create stacks from matching types and if we're stacking more than 1
            if (stackAmount <= 1)
                return;

            if (stackAmount > Setting.BLOCK_MAX_STACK_SIZE.getInt()) {
                event.setCancelled(true);
                return;
            }

            stackManager.createStackFromBlock(block, stackAmount);
        }

        // Take an item from the player's hand
        StackerUtils.takeOneItem(player, isOffHand ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND);
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
        // We will handle dropping experience ourselves for when spawners actually break
        if (event.getBlock().getType() == Material.SPAWNER)
            event.setExpToDrop(0);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null
                || clickedBlock.getType() != Material.SPAWNER
                || event.getItem() == null
                || !event.getItem().getType().name().endsWith("_SPAWN_EGG"))
            return;

        Bukkit.getScheduler().runTask(this.roseStacker, () -> {
            StackManager stackManager = this.roseStacker.getStackManager();
            if (!stackManager.isBlockStacked(clickedBlock))
                return;

            // Make sure spawners convert and update their display properly
            StackedSpawner stackedSpawner = stackManager.getStackedSpawner(clickedBlock);
            stackedSpawner.updateSpawnCount();
            stackedSpawner.updateDisplay();
        });
    }

}
