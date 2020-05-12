package dev.rosewood.rosestacker.listener;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.hook.CoreProtectHook;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.ArrayList;
import java.util.List;
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
import org.bukkit.event.block.Action;
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class BlockListener implements Listener {

    private RoseStacker roseStacker;

    public BlockListener(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockClicked(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || !event.getPlayer().isSneaking() || event.getAction() != Action.RIGHT_CLICK_BLOCK || !Setting.BLOCK_GUI_ENABLED.getBoolean())
            return;

        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        if (!stackManager.isBlockStackingEnabled())
            return;

        StackedBlock stackedBlock = stackManager.getStackedBlock(block);
        if (stackedBlock != null) {
            stackedBlock.openGui(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);

        Block block = event.getBlock();
        boolean isStacked = this.isBlockOrSpawnerStack(stackManager, block);
        boolean isSpawner = block.getType() == Material.SPAWNER;
        if (!isStacked && !isSpawner)
            return;

        Player player = event.getPlayer();
        Location dropLocation = block.getLocation().clone().add(0.5, 0.5, 0.5);

        if (isSpawner) {
            if (!stackManager.isSpawnerStackingEnabled())
                return;

            // Always drop the correct spawner type even if it's not stacked
            if (!isStacked) {
                this.tryDropSpawners(player, dropLocation, ((CreatureSpawner) block.getState()).getSpawnedType(), 1);
                block.setType(Material.AIR);
                event.setCancelled(true);
                CoreProtectHook.recordBlockBreak(player, block);
                this.damageTool(player);
                return;
            }

            StackedSpawner stackedSpawner = stackManager.getStackedSpawner(block);
            boolean breakEverything = Setting.SPAWNER_BREAK_ENTIRE_STACK_WHILE_SNEAKING.getBoolean() && player.isSneaking();
            if (breakEverything) {
                this.tryDropSpawners(player, dropLocation, ((CreatureSpawner) block.getState()).getSpawnedType(), stackedSpawner.getStackSize());
                stackedSpawner.setStackSize(0);
                block.setType(Material.AIR);
            } else {
                this.tryDropSpawners(player, dropLocation, ((CreatureSpawner) block.getState()).getSpawnedType(), 1);
                stackedSpawner.increaseStackSize(-1);
            }

            if (stackedSpawner.getStackSize() <= 0) {
                stackManager.removeSpawnerStack(stackedSpawner);
                return;
            }
        } else {
            if (!stackManager.isBlockStackingEnabled())
                return;

            StackedBlock stackedBlock = stackManager.getStackedBlock(block);
            if (stackedBlock.isLocked()) {
                event.setCancelled(true);
                return;
            }

            boolean breakEverything = Setting.BLOCK_BREAK_ENTIRE_STACK_WHILE_SNEAKING.getBoolean() && player.isSneaking();
            if (breakEverything) {
                if (player.getGameMode() != GameMode.CREATIVE)
                    stackManager.preStackItems(GuiUtil.getMaterialAmountAsItemStacks(block.getType(), stackedBlock.getStackSize()), dropLocation);
                stackedBlock.setStackSize(0);
                CoreProtectHook.recordBlockBreak(player, block);
                block.setType(Material.AIR);
            } else {
                if (player.getGameMode() != GameMode.CREATIVE)
                    player.getWorld().dropItemNaturally(dropLocation, new ItemStack(block.getType()));
                stackedBlock.increaseStackSize(-1);
                CoreProtectHook.recordBlockBreak(player, block);
            }

            if (stackedBlock.getStackSize() <= 1)
                stackManager.removeBlockStack(stackedBlock);
        }

        this.damageTool(player);
        event.setCancelled(true);
    }

    private void damageTool(Player player) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (!itemStack.getType().name().endsWith("PICKAXE"))
            return;

        Damageable damageable = (Damageable) itemStack.getItemMeta();
        if (damageable == null)
            return;

        damageable.setDamage(damageable.getDamage() + 1);
        itemStack.setItemMeta((ItemMeta) damageable);
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
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        if (!stackManager.isBlockStackingEnabled())
            return;

        if (this.isBlockOrSpawnerStack(stackManager, event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        this.handleExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        this.handleExplosion(event.blockList());
    }

    private void handleExplosion(List<Block> blockList) {
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);

        boolean stackedBlockProtection = Setting.BLOCK_EXPLOSION_PROTECTION.getBoolean() && stackManager.isBlockStackingEnabled();
        boolean stackedSpawnerProtection = Setting.SPAWNER_EXPLOSION_PROTECTION.getBoolean() && stackManager.isSpawnerStackingEnabled();

        if (stackedBlockProtection && stackedSpawnerProtection) {
            blockList.removeIf(x -> this.isBlockOrSpawnerStack(stackManager, x));
            return;
        }

        for (Block block : new ArrayList<>(blockList)) {
            if (!stackedBlockProtection && stackManager.isBlockStacked(block)) {
                blockList.remove(block);

                if (!StackerUtils.passesChance(Setting.BLOCK_EXPLOSION_DESTROY_CHANCE.getDouble() / 100))
                    continue;

                StackedBlock stackedBlock = stackManager.getStackedBlock(block);
                stackedBlock.kickOutGuiViewers();

                int newStackSize;

                int destroyAmountFixed = Setting.BLOCK_EXPLOSION_DESTROY_AMOUNT_FIXED.getInt();
                if (destroyAmountFixed != -1) {
                    newStackSize = stackedBlock.getStackSize() - destroyAmountFixed;
                } else {
                    newStackSize = (int) Math.floor(stackedBlock.getStackSize() * (Setting.BLOCK_EXPLOSION_DESTROY_AMOUNT_PERCENTAGE.getDouble() / 100));
                }

                if (newStackSize <= 0) {
                    block.setType(Material.AIR);
                    stackedBlock.setStackSize(0);
                    stackManager.removeBlockStack(stackedBlock);
                    continue;
                }

                if (Setting.BLOCK_EXPLOSION_DECREASE_STACK_SIZE_ONLY.getBoolean()) {
                    stackedBlock.setStackSize(newStackSize);
                    if (newStackSize <= 1)
                        stackManager.removeBlockStack(stackedBlock);
                } else {
                    stackedBlock.setStackSize(0);
                    stackManager.removeBlockStack(stackedBlock);
                    Material type = block.getType();
                    block.setType(Material.AIR);
                    Bukkit.getScheduler().runTask(this.roseStacker, () ->
                            stackManager.preStackItems(GuiUtil.getMaterialAmountAsItemStacks(type, newStackSize), block.getLocation().clone().add(0.5, 0.5, 0.5)));
                }
            } else if (!stackedSpawnerProtection && stackManager.isSpawnerStacked(block)) {
                blockList.remove(block);

                if (!StackerUtils.passesChance(Setting.SPAWNER_EXPLOSION_DESTROY_CHANCE.getDouble() / 100))
                    continue;

                StackedSpawner stackedSpawner = stackManager.getStackedSpawner(block);
                int newStackSize;

                int destroyAmountFixed = Setting.SPAWNER_EXPLOSION_DESTROY_AMOUNT_FIXED.getInt();
                if (destroyAmountFixed != -1) {
                    newStackSize = stackedSpawner.getStackSize() - destroyAmountFixed;
                } else {
                    newStackSize = (int) Math.floor(stackedSpawner.getStackSize() * (Setting.SPAWNER_EXPLOSION_DESTROY_AMOUNT_PERCENTAGE.getDouble() / 100));
                }

                if (newStackSize <= 0) {
                    block.setType(Material.AIR);
                    stackedSpawner.setStackSize(0);
                    stackManager.removeSpawnerStack(stackedSpawner);
                    continue;
                }

                if (Setting.SPAWNER_EXPLOSION_DECREASE_STACK_SIZE_ONLY.getBoolean()) {
                    stackedSpawner.setStackSize(newStackSize);
                } else {
                    stackedSpawner.setStackSize(0);
                    stackManager.removeSpawnerStack(stackedSpawner);
                    EntityType spawnedType = ((CreatureSpawner) block.getState()).getSpawnedType();
                    block.setType(Material.AIR);
                    Bukkit.getScheduler().runTask(this.roseStacker, () ->
                            block.getWorld().dropItemNaturally(block.getLocation().clone().add(0.5, 0.5, 0.5), StackerUtils.getSpawnerAsStackedItemStack(spawnedType, newStackSize)));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        if (!stackManager.isBlockStackingEnabled())
            return;

        if (this.isBlockOrSpawnerStack(stackManager, event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        if (!stackManager.isBlockStackingEnabled())
            return;

        for (Block block : event.getBlocks()) {
            if (this.isBlockOrSpawnerStack(stackManager, block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        if (!stackManager.isBlockStackingEnabled())
            return;

        for (Block block : event.getBlocks()) {
            if (this.isBlockOrSpawnerStack(stackManager, block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        // TODO: auto stack range

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Block against = event.getBlockAgainst();

        if (against.equals(block))
            against = against.getRelative(BlockFace.DOWN);

        if (block.getType() == Material.SPAWNER) {
            if (!stackManager.isSpawnerStackingEnabled())
                return;

            CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
            SpawnerStackSettings spawnerStackSettings = this.roseStacker.getManager(StackSettingManager.class).getSpawnerStackSettings(creatureSpawner);
            if (!spawnerStackSettings.isStackingEnabled())
                return;
        } else {
            if (!stackManager.isBlockStackingEnabled())
                return;

            BlockStackSettings blockStackSettings = this.roseStacker.getManager(StackSettingManager.class).getBlockStackSettings(block);
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
        EntityType entityType = null;
        if (isAdditiveStack && against.getType() == Material.SPAWNER) {
            entityType = StackerUtils.getStackedItemEntityType(placedItem);
            isAdditiveStack = ((CreatureSpawner) against.getState()).getSpawnedType() == entityType;
        }

        int stackAmount = StackerUtils.getStackedItemStackAmount(placedItem);
        if (isAdditiveStack && !player.isSneaking()) {
            if (block.getType() == Material.SPAWNER) {
                if (!stackManager.isSpawnerTypeStackable(entityType))
                    return;

                // Handle spawner stacking
                StackedSpawner stackedSpawner = stackManager.getStackedSpawner(against);
                if (stackedSpawner == null)
                    stackedSpawner = stackManager.createSpawnerStack(against, 1);

                if (stackedSpawner.getStackSize() + stackAmount > Setting.SPAWNER_MAX_STACK_SIZE.getInt()) {
                    event.setCancelled(true);
                    return;
                }

                stackedSpawner.increaseStackSize(stackAmount);
            } else {
                if (!stackManager.isBlockTypeStackable(against))
                    return;

                // Handle normal block stacking
                StackedBlock stackedBlock = stackManager.getStackedBlock(against);
                if (stackedBlock == null) {
                    stackedBlock = stackManager.createBlockStack(against, 1);
                } else if (stackedBlock.isLocked()) {
                    event.setCancelled(true);
                    return;
                }

                if (stackedBlock.getStackSize() + stackAmount > Setting.BLOCK_MAX_STACK_SIZE.getInt()) {
                    event.setCancelled(true);
                    return;
                }

                stackedBlock.increaseStackSize(stackAmount);
            }

            event.setCancelled(true);
            CoreProtectHook.recordBlockPlace(player, against);
        } else { // Handle singular items that have a stack multiplier
            // Set the spawner type
            if (placedItem.getType() == Material.SPAWNER) {
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                EntityType spawnedType = StackerUtils.getStackedItemEntityType(placedItem);
                if (spawnedType == null)
                    return;

                spawner.setSpawnedType(spawnedType);
                spawner.update();

                if (stackAmount <= 0)
                    return;

                if (stackAmount > Setting.SPAWNER_MAX_STACK_SIZE.getInt()) {
                    event.setCancelled(true);
                    return;
                }

                stackManager.createSpawnerStack(block, stackAmount);
            } else {
                if (stackAmount <= 1)
                    return;

                if (stackAmount > Setting.BLOCK_MAX_STACK_SIZE.getInt()) {
                    event.setCancelled(true);
                    return;
                }

                stackManager.createBlockStack(block, stackAmount);
            }
        }

        // Take an item from the player's hand
        StackerUtils.takeOneItem(player, isOffHand ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        if (!stackManager.isBlockStackingEnabled())
            return;

        if (this.isBlockOrSpawnerStack(stackManager, event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        if (!stackManager.isBlockStackingEnabled())
            return;

        if (this.isBlockOrSpawnerStack(stackManager, event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExp(BlockExpEvent event) {
        if (!this.roseStacker.getManager(StackManager.class).isSpawnerStackingEnabled())
            return;

        // Don't want to be able to get exp from stacked spawners, so just remove it all together
        // We will handle dropping experience ourselves for when spawners actually break
        if (event.getBlock().getType() == Material.SPAWNER)
            event.setExpToDrop(0);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawnerChangeWithSpawnEgg(PlayerInteractEvent event) {
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        if (!stackManager.isSpawnerStackingEnabled())
            return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null
                || clickedBlock.getType() != Material.SPAWNER
                || event.getItem() == null
                || !event.getItem().getType().name().endsWith("_SPAWN_EGG"))
            return;

        if (!event.getPlayer().hasPermission("rosestacker.spawnerconvert")) {
            event.setCancelled(true);
            return;
        }

        Bukkit.getScheduler().runTask(this.roseStacker, () -> {
            if (!this.isBlockOrSpawnerStack(stackManager, clickedBlock))
                return;

            // Make sure spawners convert and update their display properly
            StackedSpawner stackedSpawner = stackManager.getStackedSpawner(clickedBlock);
            stackedSpawner.updateSpawnerProperties();
            stackedSpawner.updateDisplay();
        });
    }

    private boolean isBlockOrSpawnerStack(StackManager stackManager, Block block) {
        return stackManager.isBlockStacked(block) || stackManager.isSpawnerStacked(block);
    }

}
