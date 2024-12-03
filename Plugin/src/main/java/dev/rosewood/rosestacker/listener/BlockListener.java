package dev.rosewood.rosestacker.listener;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.compatibility.CompatibilityAdapter;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.event.BlockStackEvent;
import dev.rosewood.rosestacker.event.BlockUnstackEvent;
import dev.rosewood.rosestacker.event.SpawnerStackEvent;
import dev.rosewood.rosestacker.event.SpawnerUnstackEvent;
import dev.rosewood.rosestacker.hook.BlockLoggingHook;
import dev.rosewood.rosestacker.hook.InsightsHook;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.spawner.SpawnerType;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ExplosionResult;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BlockListener implements Listener {

    private final RosePlugin rosePlugin;

    public BlockListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockClicked(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null)
            return;

        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getPlayer().getWorld()))
            return;

        // Check for interacting with certain stacked blocks
        ItemStack item = event.getItem();
        if (stackManager.isBlockStackingEnabled() && stackManager.isBlockStacked(block) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item != null && block.getType() == Material.TNT && (item.getType() == Material.FLINT_AND_STEEL || item.getType() == Material.FIRE_CHARGE)) {
                event.setUseInteractedBlock(Event.Result.DENY);
                return;
            } else if (StackerUtils.isInteractable(block.getType()) && (!event.getPlayer().isSneaking() || item == null)) {
                event.setUseInteractedBlock(Event.Result.DENY);
                return;
            }
        }

        if (event.getPlayer().isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (stackManager.isBlockStackingEnabled() && SettingKey.BLOCK_GUI_ENABLED.get()) {
                StackedBlock stackedBlock = stackManager.getStackedBlock(block);
                if (stackedBlock != null) {
                    stackedBlock.openGui(event.getPlayer());
                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setUseItemInHand(Event.Result.DENY);
                    return;
                }
            }

            boolean isNotSneakOverriding = !SettingKey.SPAWNER_STACK_ENTIRE_HAND_WHEN_SNEAKING.get() || item == null || item.getType() != Material.SPAWNER;
            if (stackManager.isSpawnerStackingEnabled() && block.getType() == Material.SPAWNER && SettingKey.SPAWNER_GUI_ENABLED.get() && isNotSneakOverriding) {
                StackedSpawner stackedSpawner = stackManager.getStackedSpawner(block);
                if (stackedSpawner == null)
                    stackedSpawner = stackManager.createSpawnerStack(block, 1, false); // Doesn't exist, need it to in order to open the GUI

                if (stackedSpawner != null) {
                    stackedSpawner.openGui(event.getPlayer());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getPlayer().getWorld()))
            return;

        Block block = event.getBlock();
        boolean isStacked = this.isBlockOrSpawnerStack(stackManager, block);
        boolean isSpawner = block.getType() == Material.SPAWNER;
        if (!isStacked && !isSpawner)
            return;

        Player player = event.getPlayer();
        Location dropLocation = block.getLocation().clone();

        if (isSpawner) {
            if (!stackManager.isSpawnerStackingEnabled())
                return;

            StackedSpawner stackedSpawner = stackManager.getStackedSpawner(block);
            if (stackedSpawner == null) {
                // Check if the spawner type is disabled, if it is just ignore the rest of the event, otherwise create a new stacked spawner
                CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
                SpawnerStackSettings spawnerStackSettings = this.rosePlugin.getManager(StackSettingManager.class).getSpawnerStackSettings(creatureSpawner);
                if (spawnerStackSettings == null || !spawnerStackSettings.isStackingEnabled())
                    return;

                stackedSpawner = stackManager.createSpawnerStack(block, 1, false);
            }

            SpawnerType spawnerType = stackedSpawner.getSpawnerTile().getSpawnerType();
            boolean breakEverything = SettingKey.SPAWNER_BREAK_ENTIRE_STACK_WHILE_SNEAKING.get() && player.isSneaking();
            int breakAmount = breakEverything ? stackedSpawner.getStackSize() : 1;

            SpawnerUnstackEvent spawnerUnstackEvent = new SpawnerUnstackEvent(player, stackedSpawner, breakAmount);
            Bukkit.getPluginManager().callEvent(spawnerUnstackEvent);
            if (spawnerUnstackEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }
            breakAmount = spawnerUnstackEvent.getDecreaseAmount();

            if (this.tryDropSpawners(player, dropLocation, spawnerType, breakAmount, stackedSpawner.isPlacedByPlayer())) {
                BlockLoggingHook.recordBlockBreak(player, block);
                if (breakAmount == stackedSpawner.getStackSize()) {
                    // Fix an issue where Insights can't detect the last spawner broken when the hook is disabled
                    if (!SettingKey.MISC_INSIGHTS_LOGGING.get() && Bukkit.getPluginManager().isPluginEnabled("Insights"))
                        InsightsHook.modifyBlockAmount(block, -1);
                    stackedSpawner.setStackSize(0);
                    block.setType(Material.AIR);
                } else {
                    stackedSpawner.increaseStackSize(-breakAmount);
                }

                if (stackedSpawner.getStackSize() <= 0) {
                    stackManager.removeSpawnerStack(stackedSpawner);
                }
            } else {
                event.setCancelled(true);
                return;
            }
        } else {
            if (!stackManager.isBlockStackingEnabled())
                return;

            StackedBlock stackedBlock = stackManager.getStackedBlock(block);
            if (stackedBlock == null)
                return;

            if (stackedBlock.isLocked()) {
                event.setCancelled(true);
                return;
            }

            boolean breakEverything = SettingKey.BLOCK_BREAK_ENTIRE_STACK_WHILE_SNEAKING.get() && player.isSneaking();
            int breakAmount = breakEverything ? stackedBlock.getStackSize() : 1;

            BlockUnstackEvent blockUnstackEvent = new BlockUnstackEvent(player, stackedBlock, breakAmount);
            Bukkit.getPluginManager().callEvent(blockUnstackEvent);
            if (blockUnstackEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }
            breakAmount = blockUnstackEvent.getDecreaseAmount();

            if (player.getGameMode() != GameMode.CREATIVE) {
                List<ItemStack> items;
                if (SettingKey.BLOCK_BREAK_ENTIRE_STACK_INTO_SEPARATE.get()) {
                    items = GuiUtil.getMaterialAmountAsItemStacks(block.getType(), breakAmount);
                } else {
                    items = List.of(ItemUtils.getBlockAsStackedItemStack(block.getType(), breakAmount));
                }

                if (SettingKey.BLOCK_DROP_TO_INVENTORY.get()) {
                    ItemUtils.dropItemsToPlayer(player, items);
                } else {
                    stackManager.preStackItems(items, dropLocation);
                }
            }

            BlockLoggingHook.recordBlockBreak(player, block);
            if (breakAmount == stackedBlock.getStackSize()) {
                // Fix an issue where Insights can't detect the last block broken when the hook is disabled
                if (!SettingKey.MISC_INSIGHTS_LOGGING.get() && Bukkit.getPluginManager().isPluginEnabled("Insights"))
                    InsightsHook.modifyBlockAmount(block, -1);
                stackedBlock.setStackSize(0);
                block.setType(Material.AIR);
            } else {
                stackedBlock.increaseStackSize(-1);
            }

            if (stackedBlock.getStackSize() <= 1)
                stackManager.removeBlockStack(stackedBlock);
        }

        this.damageTool(player);
        event.setCancelled(true);
    }

    private void damageTool(Player player) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (player.getGameMode() == GameMode.CREATIVE || !itemStack.getType().name().endsWith("PICKAXE"))
            return;

        ItemUtils.damageTool(itemStack);
    }

    /**
     * Tries to drop spawners that a player broke
     *
     * @param player The Player
     * @param dropLocation The location to drop the items
     * @param spawnerType The type of entity the spawner spawns
     * @param amount The amount to try to drop
     * @param placedByPlayer whether or not the spawner was placed by a player
     * @return true if spawners weren't protected, false otherwise
     */
    private boolean tryDropSpawners(Player player, Location dropLocation, SpawnerType spawnerType, int amount, boolean placedByPlayer) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (dropLocation.getWorld() == null)
            return true;

        if (player.getGameMode() == GameMode.CREATIVE) {
            if (!SettingKey.SPAWNER_DROP_IN_CREATIVE.get())
                return true;
        } else {
            if (!itemInHand.getType().name().endsWith("PICKAXE"))
                return true;
        }

        if ((SettingKey.SPAWNER_SILK_TOUCH_REQUIRED.get() || SettingKey.SPAWNER_ADVANCED_PERMISSIONS.get()) && player.getGameMode() != GameMode.CREATIVE) {
            int destroyAmount = 0;

            int silkTouchLevel = itemInHand.getEnchantmentLevel(Enchantment.SILK_TOUCH);
            boolean destroyFromMissingPermission;
            boolean hasAdvNoSilkPermission = player.hasPermission("rosestacker.nosilk." + spawnerType.getEnumName().toLowerCase());
            boolean hasAdvSilkTouchPermission = player.hasPermission("rosestacker.silktouch." + spawnerType.getEnumName().toLowerCase());
            if (SettingKey.SPAWNER_ADVANCED_PERMISSIONS.get()) {
                boolean hasPermission = hasAdvNoSilkPermission;
                if (silkTouchLevel > 0)
                    hasPermission |= hasAdvSilkTouchPermission;
                destroyFromMissingPermission = !hasPermission;
            } else {
                destroyFromMissingPermission = SettingKey.SPAWNER_SILK_TOUCH_REQUIRE_PERMISSION.get() && !player.hasPermission("rosestacker.silktouch");
            }

            if (destroyFromMissingPermission)
                destroyAmount = amount;

            if (!(SettingKey.SPAWNER_SILK_TOUCH_ONLY_NATURAL.get() && placedByPlayer)
                    && (!SettingKey.SPAWNER_ADVANCED_PERMISSIONS.get() || !hasAdvNoSilkPermission)
                    && (!SettingKey.SPAWNER_SILK_TOUCH_GUARANTEE.get() || silkTouchLevel < 2)) {
                if (silkTouchLevel > 0) {
                    double chance = StackerUtils.getSilkTouchChanceRaw(player) / 100;
                    int attempts = amount - destroyAmount;
                    destroyAmount += attempts - StackerUtils.countPassedChances(chance, attempts);
                } else {
                    destroyAmount += amount; // Destroy all spawners with no silk touch while it's required
                }
            }

            if (destroyAmount > amount)
                destroyAmount = amount;

            amount -= destroyAmount;

            if (destroyAmount > 0) {
                if (SettingKey.SPAWNER_SILK_TOUCH_PROTECT.get() && (silkTouchLevel <= 0 || destroyFromMissingPermission)) {
                    LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
                    if (SettingKey.SPAWNER_ADVANCED_PERMISSIONS.get()) {
                        if (hasAdvSilkTouchPermission) {
                            localeManager.sendMessage(player, "spawner-advanced-break-silktouch-no-permission");
                        } else {
                            localeManager.sendMessage(player, "spawner-advanced-break-no-permission");
                        }
                    } else {
                        localeManager.sendMessage(player, "spawner-silk-touch-protect");
                    }
                    return false;
                }

                if (SettingKey.SPAWNER_DROP_EXPERIENCE_WHEN_DESTROYED.get())
                    StackerUtils.dropExperience(dropLocation, 15L * destroyAmount, 43L * destroyAmount, (int) Math.min(Integer.MAX_VALUE, 15L * destroyAmount));
            }
        }

        if (amount <= 0)
            return true;

        boolean dropToInventory = SettingKey.SPAWNER_DROP_TO_INVENTORY.get();
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        List<ItemStack> itemsToDrop;
        if (SettingKey.SPAWNER_BREAK_ENTIRE_STACK_INTO_SEPARATE.get()) {
            ItemStack item = ItemUtils.getSpawnerAsStackedItemStack(spawnerType, 1);
            item.setAmount(amount);
            itemsToDrop = List.of(item);
        } else {
            itemsToDrop = List.of(ItemUtils.getSpawnerAsStackedItemStack(spawnerType, amount));
        }

        if (dropToInventory) {
            Collection<ItemStack> remainingItems = player.getInventory().addItem(itemsToDrop.toArray(ItemStack[]::new)).values();
            if (!remainingItems.isEmpty())
                stackManager.preStackItems(remainingItems, player.getLocation().add(0, 1, 0));
        } else {
            stackManager.preStackItems(itemsToDrop, dropLocation);
        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getBlock().getWorld()))
            return;

        if (!stackManager.isBlockStackingEnabled())
            return;

        if (this.isBlockOrSpawnerStack(stackManager, event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (NMSUtil.getVersionNumber() < 21 || (event.getExplosionResult() == ExplosionResult.DESTROY || event.getExplosionResult() == ExplosionResult.DESTROY_WITH_DECAY))
            this.handleExplosion(event.getBlock().getLocation(), event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (NMSUtil.getVersionNumber() < 21 || (event.getExplosionResult() == ExplosionResult.DESTROY || event.getExplosionResult() == ExplosionResult.DESTROY_WITH_DECAY))
            this.handleExplosion(event.getLocation(), event.blockList());
    }

    private void handleExplosion(Location location, List<Block> blockList) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(location.getWorld()))
            return;

        boolean stackedBlockProtection = SettingKey.BLOCK_EXPLOSION_PROTECTION.get() && stackManager.isBlockStackingEnabled();
        boolean stackedSpawnerProtection = SettingKey.SPAWNER_EXPLOSION_PROTECTION.get() && stackManager.isSpawnerStackingEnabled();

        if (stackedSpawnerProtection)
            blockList.removeIf(stackManager::isSpawnerStacked);

        if (stackedBlockProtection)
            blockList.removeIf(stackManager::isBlockStacked);

        for (Block block : new ArrayList<>(blockList)) {
            if (stackManager.isBlockStacked(block)) {
                blockList.remove(block);

                if (!StackerUtils.passesChance(SettingKey.BLOCK_EXPLOSION_DESTROY_CHANCE.get() / 100))
                    continue;

                StackedBlock stackedBlock = stackManager.getStackedBlock(block);
                stackedBlock.kickOutGuiViewers();

                int destroyAmountFixed = SettingKey.BLOCK_EXPLOSION_DESTROY_AMOUNT_FIXED.get();
                int destroyAmount;
                if (destroyAmountFixed != -1) {
                    destroyAmount = destroyAmountFixed;
                } else {
                    destroyAmount = stackedBlock.getStackSize() - (int) Math.ceil(stackedBlock.getStackSize() * (SettingKey.BLOCK_EXPLOSION_DESTROY_AMOUNT_PERCENTAGE.get() / 100));
                }

                BlockUnstackEvent blockUnstackEvent = new BlockUnstackEvent(null, stackedBlock, destroyAmount);
                Bukkit.getPluginManager().callEvent(blockUnstackEvent);
                if (blockUnstackEvent.isCancelled())
                    continue;
                destroyAmount = blockUnstackEvent.getDecreaseAmount();

                int newStackSize = stackedBlock.getStackSize() - destroyAmount;
                if (newStackSize <= 0) {
                    block.setType(Material.AIR);
                    stackedBlock.setStackSize(0);
                    stackManager.removeBlockStack(stackedBlock);
                    continue;
                }

                if (SettingKey.BLOCK_EXPLOSION_DECREASE_STACK_SIZE_ONLY.get()) {
                    stackedBlock.setStackSize(newStackSize);
                    if (newStackSize <= 1)
                        stackManager.removeBlockStack(stackedBlock);
                } else {
                    stackedBlock.setStackSize(0);
                    stackManager.removeBlockStack(stackedBlock);
                    Material type = block.getType();
                    block.setType(Material.AIR);
                    ThreadUtils.runSync(() -> {
                        List<ItemStack> items;
                        if (SettingKey.BLOCK_BREAK_ENTIRE_STACK_INTO_SEPARATE.get()) {
                            items = GuiUtil.getMaterialAmountAsItemStacks(type, newStackSize);
                        } else {
                            items = List.of(ItemUtils.getBlockAsStackedItemStack(type, newStackSize));
                        }
                        stackManager.preStackItems(items, block.getLocation().clone().add(0.5, 0.5, 0.5));
                    });
                }
            } else if (stackManager.isSpawnerStacked(block)) {
                blockList.remove(block);

                if (!StackerUtils.passesChance(SettingKey.SPAWNER_EXPLOSION_DESTROY_CHANCE.get() / 100))
                    continue;

                StackedSpawner stackedSpawner = stackManager.getStackedSpawner(block);

                int destroyAmountFixed = SettingKey.SPAWNER_EXPLOSION_DESTROY_AMOUNT_FIXED.get();
                int destroyAmount;
                if (destroyAmountFixed != -1) {
                    destroyAmount = destroyAmountFixed;
                } else {
                    destroyAmount = stackedSpawner.getStackSize() - (int) Math.ceil(stackedSpawner.getStackSize() * (SettingKey.SPAWNER_EXPLOSION_DESTROY_AMOUNT_PERCENTAGE.get() / 100));
                }

                SpawnerUnstackEvent spawnerUnstackEvent = new SpawnerUnstackEvent(null, stackedSpawner, destroyAmount);
                Bukkit.getPluginManager().callEvent(spawnerUnstackEvent);
                if (spawnerUnstackEvent.isCancelled())
                    continue;
                destroyAmount = spawnerUnstackEvent.getDecreaseAmount();

                int newStackSize = stackedSpawner.getStackSize() - destroyAmount;
                if (newStackSize <= 0) {
                    block.setType(Material.AIR);
                    stackedSpawner.setStackSize(0);
                    stackManager.removeSpawnerStack(stackedSpawner);
                    continue;
                }

                if (SettingKey.SPAWNER_EXPLOSION_DECREASE_STACK_SIZE_ONLY.get()) {
                    stackedSpawner.setStackSize(newStackSize);
                } else {
                    stackedSpawner.setStackSize(0);
                    stackManager.removeSpawnerStack(stackedSpawner);
                    SpawnerType spawnerType = stackedSpawner.getSpawnerTile().getSpawnerType();
                    block.setType(Material.AIR);
                    ThreadUtils.runSync(() -> {
                        if (SettingKey.SPAWNER_BREAK_ENTIRE_STACK_INTO_SEPARATE.get()) {
                            ItemStack spawnerItem = ItemUtils.getSpawnerAsStackedItemStack(spawnerType, 1);
                            stackManager.dropItemStack(spawnerItem, newStackSize, block.getLocation(), true);
                        } else {
                            stackManager.preStackItems(List.of(ItemUtils.getSpawnerAsStackedItemStack(spawnerType, newStackSize)), block.getLocation());
                        }
                    });
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getBlock().getWorld()))
            return;

        if (!stackManager.isBlockStackingEnabled())
            return;

        if (stackManager.isBlockStacked(event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getBlock().getWorld()))
            return;

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
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getBlock().getWorld()))
            return;

        if (!stackManager.isBlockStackingEnabled())
            return;

        for (Block block : event.getBlocks()) {
            if (this.isBlockOrSpawnerStack(stackManager, block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getPlayer().getWorld()))
            return;

        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Block is transforming from one type to another, ignore this to prevent potential duplication
        Material replacedType = event.getBlockReplacedState().getType();
        if (replacedType.isSolid() && !replacedType.name().equals("SCULK_VEIN")) // For whatever reason SCULK_VEIN blocks are marked as solid even though they can be replaced??
            return;

        Block against = event.getBlockAgainst();
        if (against.equals(block))
            against = against.getRelative(BlockFace.DOWN);

        // Get the block in the player's hand that's being placed
        ItemStack placedItem = player.getInventory().getItemInMainHand();
        boolean isOffHand = false;
        if (placedItem.getType() == Material.AIR || !placedItem.getType().isBlock()) {
            placedItem = player.getInventory().getItemInOffHand();
            isOffHand = true;
        }

        // Will be true if we are adding to an existing stack (including a stack of 1), or false if we are creating a new one from an itemstack with a stack value
        boolean isDistanceStack = false;
        boolean isAdditiveStack = against.getType() == block.getType();
        SpawnerType spawnerType = placedItem.getType() == Material.SPAWNER ? ItemUtils.getStackedItemSpawnerType(placedItem) : null;
        int stackAmount = ItemUtils.getStackedItemStackAmount(placedItem);

        if (spawnerType != null) {
            if (!stackManager.isSpawnerStackingEnabled() || !stackManager.isSpawnerTypeStackable(spawnerType))
                return;

            if (block.getState() instanceof CreatureSpawner creatureSpawner) {
                spawnerType.get().ifPresent(x -> {
                    CompatibilityAdapter.getCreatureSpawnerHandler().setSpawnedType(creatureSpawner, x);
                    creatureSpawner.update();
                });
            }
        } else {
            if (!stackManager.isBlockStackingEnabled() || !stackManager.isBlockTypeStackable(block))
                return;
        }

        // See if we can stack the spawner (if applicable) into one nearby
        int autoStackRange = SettingKey.SPAWNER_AUTO_STACK_RANGE.get();
        boolean autoStackChunk = SettingKey.SPAWNER_AUTO_STACK_CHUNK.get();
        boolean useAutoStack = autoStackRange > 0 || autoStackChunk;
        if (useAutoStack && spawnerType != null) {
            StackedSpawner nearest = null;
            boolean anyNearby = false;
            List<StackedSpawner> spawners = new ArrayList<>(stackManager.getStackingThread(block.getWorld()).getStackedSpawners().values());
            if (!autoStackChunk) {
                double closestDistance = autoStackRange * autoStackRange;
                for (StackedSpawner spawner : spawners) {
                    double distance = spawner.getLocation().distanceSquared(block.getLocation());
                    if (distance < closestDistance) {
                        boolean sameType = spawner.getSpawnerTile().getSpawnerType().equals(spawnerType);
                        anyNearby |= SettingKey.SPAWNER_AUTO_STACK_PREVENT_MULTIPLE_IN_RANGE.get() || sameType;
                        if (sameType && spawner.getStackSize() + stackAmount <= spawner.getStackSettings().getMaxStackSize()) {
                            closestDistance = distance;
                            nearest = spawner;
                        }
                    }
                }
            } else {
                int blockChunkX = block.getLocation().getBlockX() >> 4;
                int blockChunkZ = block.getLocation().getBlockZ() >> 4;
                for (StackedSpawner spawner : spawners) {
                    Block spawnerBlock = spawner.getBlock();
                    if (spawnerBlock.getX() >> 4 == blockChunkX && spawnerBlock.getZ() >> 4 == blockChunkZ) {
                        boolean sameType = spawner.getSpawnerTile().getSpawnerType().equals(spawnerType);
                        anyNearby |= SettingKey.SPAWNER_AUTO_STACK_PREVENT_MULTIPLE_IN_RANGE.get() || sameType;
                        if (sameType && spawner.getStackSize() + stackAmount <= spawner.getStackSettings().getMaxStackSize()) {
                            nearest = spawner;
                            break;
                        }
                    }
                }
            }

            if (nearest != null) {
                against = nearest.getBlock();
                isAdditiveStack = true;
                isDistanceStack = true;
            } else if (anyNearby && SettingKey.SPAWNER_AUTO_STACK_PREVENT_MULTIPLE_IN_RANGE.get()) {
                event.setCancelled(true);
                return;
            }
        }

        // Don't allow placing if they don't have permission
        if (spawnerType != null && SettingKey.SPAWNER_ADVANCED_PERMISSIONS.get()
                && !player.hasPermission("rosestacker.spawnerplace." + spawnerType.getEnumName().toLowerCase())) {
            this.rosePlugin.getManager(LocaleManager.class).sendMessage(player, "spawner-advanced-place-no-permission");
            event.setCancelled(true);
            return;
        }

        StackedSpawner againstSpawner = null;
        if (isAdditiveStack && against.getType() == Material.SPAWNER) {
            againstSpawner = stackManager.getStackedSpawner(against);
            if (againstSpawner == null) {
                event.setCancelled(true);
                return;
            }

            isAdditiveStack = againstSpawner.getSpawnerTile().getSpawnerType().equals(spawnerType);
        }

        if (isAdditiveStack && ((!player.isSneaking() || (SettingKey.SPAWNER_STACK_ENTIRE_HAND_WHEN_SNEAKING.get() && placedItem.getType() == Material.SPAWNER)) || against.getType().isInteractable() || isDistanceStack)) {
            if (block.getType() == Material.SPAWNER) {
                // Handle spawner stacking
                if (againstSpawner == null)
                    againstSpawner = stackManager.getStackedSpawner(against);

                int itemsToTake;
                if (againstSpawner != null && SettingKey.SPAWNER_STACK_ENTIRE_HAND_WHEN_SNEAKING.get() && player.isSneaking()) {
                    itemsToTake = 0;
                    int availableItems = placedItem.getAmount();
                    for (int i = 0; i < availableItems; i++)
                        if (againstSpawner.getStackSize() + stackAmount * (itemsToTake + 1) <= againstSpawner.getStackSettings().getMaxStackSize())
                            itemsToTake++;
                    stackAmount *= itemsToTake;
                } else {
                    if (againstSpawner != null && againstSpawner.getStackSize() + stackAmount > againstSpawner.getStackSettings().getMaxStackSize()) {
                        event.setCancelled(true);
                        return;
                    }
                    itemsToTake = 1;
                }

                if (itemsToTake <= 0) {
                    event.setCancelled(true);
                    return;
                }

                if (againstSpawner != null) {
                    SpawnerStackEvent spawnerStackEvent = new SpawnerStackEvent(player, againstSpawner, stackAmount, false);
                    Bukkit.getPluginManager().callEvent(spawnerStackEvent);
                    if (spawnerStackEvent.isCancelled()) {
                        event.setCancelled(true);
                        return;
                    }
                    stackAmount = spawnerStackEvent.getIncreaseAmount();
                } else {
                    againstSpawner = stackManager.createSpawnerStack(against, 1, false);

                    SpawnerStackEvent spawnerStackEvent = new SpawnerStackEvent(player, againstSpawner, stackAmount, true);
                    Bukkit.getPluginManager().callEvent(spawnerStackEvent);
                    if (spawnerStackEvent.isCancelled()) {
                        event.setCancelled(true);
                        return;
                    }

                    if (againstSpawner.getStackSize() + stackAmount > againstSpawner.getStackSettings().getMaxStackSize()) {
                        event.setCancelled(true);
                        return;
                    }
                }

                againstSpawner.increaseStackSize(stackAmount);

                // Fling particles from the attempted place location to the actual place location
                if (isDistanceStack && SettingKey.SPAWNER_AUTO_STACK_PARTICLES.get()) {
                    for (int i = 0; i < 50; i++) {
                        Vector offset = Vector.getRandom();
                        Location startLoc = block.getLocation().clone().add(offset);
                        Vector start = startLoc.toVector();
                        Vector end = against.getLocation().toVector().add(offset).add(new Vector(0.0, 0.1, 0.0));
                        Vector angle = end.clone().subtract(start);
                        double length = angle.length() * 0.09;
                        angle.normalize();
                        player.spawnParticle(Particle.END_ROD, startLoc, 0, angle.getX(), angle.getY(), angle.getZ(), length);
                    }
                }

                // Take an item from the player's hand
                ItemUtils.takeItems(itemsToTake, player, isOffHand ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND);
            } else {
                if (!stackManager.isBlockTypeStackable(against))
                    return;

                // Handle normal block stacking
                StackedBlock stackedBlock = stackManager.getStackedBlock(against);

                if (stackedBlock != null) {
                    if (stackedBlock.isLocked()) {
                        event.setCancelled(true);
                        return;
                    }

                    if (stackedBlock.getStackSize() + stackAmount > stackedBlock.getStackSettings().getMaxStackSize()) {
                        event.setCancelled(true);
                        return;
                    }

                    BlockStackEvent blockStackEvent = new BlockStackEvent(player, stackedBlock, stackAmount, false);
                    Bukkit.getPluginManager().callEvent(blockStackEvent);
                    if (blockStackEvent.isCancelled()) {
                        event.setCancelled(true);
                        return;
                    }
                    stackAmount = blockStackEvent.getIncreaseAmount();
                } else {
                    stackedBlock = stackManager.createBlockStack(against, 1);

                    BlockStackEvent blockStackEvent = new BlockStackEvent(player, stackedBlock, stackAmount, false);
                    Bukkit.getPluginManager().callEvent(blockStackEvent);
                    if (blockStackEvent.isCancelled()) {
                        event.setCancelled(true);
                        return;
                    }

                    if (stackedBlock.getStackSize() + stackAmount > stackedBlock.getStackSettings().getMaxStackSize()) {
                        event.setCancelled(true);
                        return;
                    }

                    // If stacking beacons, remove the effects
                    if (against.getType() == Material.BEACON) {
                        Beacon beacon = (Beacon) against.getState();
                        beacon.setPrimaryEffect(null);
                        beacon.setSecondaryEffect(null);
                        beacon.update();
                    }
                }

                stackedBlock.increaseStackSize(stackAmount);

                // Take an item from the player's hand
                ItemUtils.takeItems(1, player, isOffHand ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND);
            }

            event.setCancelled(true);
            BlockLoggingHook.recordBlockPlace(player, against);
        } else { // Handle singular items that have a stack multiplier
            // Set the spawner type
            StackSettingManager stackSettingManager = this.rosePlugin.getManager(StackSettingManager.class);
            if (placedItem.getType() == Material.SPAWNER) {
                if (spawnerType == null)
                    return;

                SpawnerStackSettings spawnerStackSettings = stackSettingManager.getSpawnerStackSettings(spawnerType);
                if (spawnerStackSettings == null)
                    return;

                if (stackAmount <= 0)
                    return;

                if (stackAmount > spawnerStackSettings.getMaxStackSize()) {
                    event.setCancelled(true);
                    return;
                }

                StackedSpawner tempStackedSpawner = new StackedSpawner(0, block, true);
                SpawnerStackEvent spawnerStackEvent = new SpawnerStackEvent(player, tempStackedSpawner, stackAmount, true);
                Bukkit.getPluginManager().callEvent(spawnerStackEvent);
                if (spawnerStackEvent.isCancelled()) {
                    tempStackedSpawner.setStackSize(0);
                    event.setCancelled(true);
                    return;
                }
                stackAmount = spawnerStackEvent.getIncreaseAmount();

                StackedSpawner stackedSpawner = stackManager.createSpawnerStack(block, stackAmount, true);
                if (stackedSpawner != null) {
                    stackedSpawner.getSpawnerTile().setSpawnerType(spawnerType);
                    stackedSpawner.updateSpawnerProperties(true);
                }
            } else {
                if (stackAmount <= 1)
                    return;

                BlockStackSettings blockStackSettings = stackSettingManager.getBlockStackSettings(block);
                if (blockStackSettings == null)
                    return;

                if (stackAmount > blockStackSettings.getMaxStackSize()) {
                    event.setCancelled(true);
                    return;
                }

                StackedBlock tempStackedBlock = new StackedBlock(0, block);
                BlockStackEvent blockStackEvent = new BlockStackEvent(player, tempStackedBlock, stackAmount, true);
                Bukkit.getPluginManager().callEvent(blockStackEvent);
                if (blockStackEvent.isCancelled()) {
                    tempStackedBlock.setStackSize(0);
                    event.setCancelled(true);
                    return;
                }

                stackManager.createBlockStack(block, stackAmount);
            }

            // Take an item from the player's hand
            ItemUtils.takeItems(1, player, isOffHand ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getBlock().getWorld()))
            return;

        if (!stackManager.isBlockStackingEnabled())
            return;

        if (this.isBlockOrSpawnerStack(stackManager, event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getBlock().getWorld()))
            return;

        if (!stackManager.isBlockStackingEnabled())
            return;

        if (this.isBlockOrSpawnerStack(stackManager, event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExp(BlockExpEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getBlock().getWorld()))
            return;

        if (!stackManager.isSpawnerStackingEnabled())
            return;

        // Don't want to be able to get exp from stacked spawners, so just remove it all together
        // We will handle dropping experience ourselves for when spawners actually break
        if (event.getBlock().getType() == Material.SPAWNER)
            event.setExpToDrop(0);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityMultiblockCreation(CreatureSpawnEvent event) {
        switch (event.getSpawnReason()) {
            case BUILD_IRONGOLEM:
            case BUILD_SNOWMAN:
            case BUILD_WITHER:
                break;
            default:
                return;
        }

        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getEntity().getWorld()) || !stackManager.isBlockStackingEnabled())
            return;

        // Do not allow the entity to be spawned if there is a stacked block too close to the entity location
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = event.getEntity().getLocation().add(x, y, z).getBlock();
                    if (stackManager.isBlockStacked(block)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    private boolean isBlockOrSpawnerStack(StackManager stackManager, Block block) {
        return stackManager.isBlockStacked(block) || stackManager.isSpawnerStacked(block);
    }

}
