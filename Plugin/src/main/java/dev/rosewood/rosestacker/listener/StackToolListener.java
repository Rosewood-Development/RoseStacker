package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.SpawnerSpawnManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedItem;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StackToolListener implements Listener {

    private final StackManager stackManager;
    private final LocaleManager localeManager;
    private final SpawnerSpawnManager spawnerSpawnManager;

    private final Map<UUID, SelectedEntities> selectedEntities;

    public StackToolListener(RosePlugin rosePlugin) {
        this.stackManager = rosePlugin.getManager(StackManager.class);
        this.localeManager = rosePlugin.getManager(LocaleManager.class);
        this.spawnerSpawnManager = rosePlugin.getManager(SpawnerSpawnManager.class);

        this.selectedEntities = new HashMap<>();
    }

    /**
     * Handles Right Clicking for StackedEntities.
     * Handles Shift Right Clicking for StackedEntities.
     *
     * @param event The PlayerInteractAtEntityEvent
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClick(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!(event.getRightClicked() instanceof LivingEntity)
                || event.getHand() != EquipmentSlot.HAND
                || !StackerUtils.isStackingTool(tool))
            return;

        event.setCancelled(true);

        LivingEntity entity = (LivingEntity) event.getRightClicked();
        StackedEntity stackedEntity = this.stackManager.getStackedEntity(entity);
        if (stackedEntity == null)
            return;

        if (!player.hasPermission("rosestacker.stacktool")) {
            this.localeManager.sendMessage(player, "command-stacktool-no-permission");
            return;
        }

        if (!player.isSneaking()) {
            boolean stackable = !StackerUtils.isUnstackable(entity);
            StackerUtils.setUnstackable(entity, stackable);
            String stackableStr = !stackable ? "stackable" : "unstackable";
            this.localeManager.sendMessage(player, "command-stacktool-marked-" + stackableStr, StringPlaceholders.single("type", stackedEntity.getStackSettings().getDisplayName()));
        } else {
            StackerUtils.setUnstackable(entity, true);
            List<LivingEntity> stackEntities = StackerUtils.deconstructStackedEntities(stackedEntity);
            for (LivingEntity stackEntity : stackEntities)
                StackerUtils.setUnstackable(stackEntity, true);
            StackerUtils.reconstructStackedEntities(stackedEntity, stackEntities);
            this.localeManager.sendMessage(player, "command-stacktool-marked-all-unstackable", StringPlaceholders.single("type", stackedEntity.getStackSettings().getDisplayName()));
        }
    }

    /**
     * Handles Shift Left Clicks for StackedEntities
     *
     * @param event The EntityDamageByEntityEvent
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeftClickEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;

        Player player = (Player) event.getDamager();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!StackerUtils.isStackingTool(tool))
            return;

        event.setCancelled(true);

        if (!(event.getEntity() instanceof LivingEntity))
            return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        StackedEntity stackedEntity = this.stackManager.getStackedEntity(entity);
        if (stackedEntity == null)
            return;

        if (!player.hasPermission("rosestacker.stacktool")) {
            this.localeManager.sendMessage(player, "command-stacktool-no-permission");
            return;
        }

        if (!player.isSneaking()) {
            SelectedEntities selected = this.selectedEntities.get(player.getUniqueId());
            if (selected == null) {
                selected = new SelectedEntities();
                this.selectedEntities.put(player.getUniqueId(), selected);
            }

            if (selected.getEntity1() == stackedEntity) {
                selected.unselect();
                this.localeManager.sendMessage(player, "command-stacktool-unselect-1", StringPlaceholders.single("type", stackedEntity.getStackSettings().getDisplayName()));
                return;
            }

            selected.select(stackedEntity);

            if (!selected.hasSelected()) {
                this.localeManager.sendMessage(player, "command-stacktool-select-1", StringPlaceholders.single("type", stackedEntity.getStackSettings().getDisplayName()));
            } else {
                this.localeManager.sendMessage(player, "command-stacktool-select-2", StringPlaceholders.single("type", stackedEntity.getStackSettings().getDisplayName()));

                StackedEntity entity1 = selected.getEntity1();
                StackedEntity entity2 = selected.getEntity2();
                selected.unselect();

                EntityStackComparisonResult result = stackedEntity.getStackSettings().canStackWith(entity1, entity2, false);
                if (result == EntityStackComparisonResult.CAN_STACK) {
                    this.localeManager.sendMessage(player, "command-stacktool-can-stack");
                } else {
                    this.localeManager.sendMessage(player, "command-stacktool-can-not-stack", StringPlaceholders.single("reason", result.name()));
                }
            }
        } else {
            AttributeInstance movementAttribute = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            boolean hasAi = movementAttribute == null || movementAttribute.getBaseValue() > 0;

            String trueStr = this.localeManager.getLocaleMessage("command-stacktool-info-true");
            String falseStr = this.localeManager.getLocaleMessage("command-stacktool-info-false");

            this.localeManager.sendMessage(player, "command-stacktool-info");
            this.localeManager.sendSimpleMessage(player, "command-stacktool-info-id", StringPlaceholders.single("id", stackedEntity.getId()));
            this.localeManager.sendSimpleMessage(player, "command-stacktool-info-uuid", StringPlaceholders.single("uuid", entity.getUniqueId().toString()));
            this.localeManager.sendSimpleMessage(player, "command-stacktool-info-entity-id", StringPlaceholders.single("id", entity.getEntityId()));
            this.localeManager.sendSimpleMessage(player, "command-stacktool-info-entity-type", StringPlaceholders.single("type", entity.getType().name()));
            this.localeManager.sendSimpleMessage(player, "command-stacktool-info-stack-size", StringPlaceholders.single("amount", stackedEntity.getStackSize()));
            if (entity.getCustomName() != null)
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-custom-name", StringPlaceholders.single("name", entity.getCustomName()));
            this.localeManager.sendSimpleMessage(player, "command-stacktool-info-entity-stackable", StringPlaceholders.single("value", StackerUtils.isUnstackable(entity) ? falseStr : trueStr));
            this.localeManager.sendSimpleMessage(player, "command-stacktool-info-entity-from-spawner", StringPlaceholders.single("value", this.spawnerSpawnManager.isSpawnedFromSpawner(entity) ? trueStr : falseStr));
            this.localeManager.sendSimpleMessage(player, "command-stacktool-info-entity-has-ai", StringPlaceholders.single("value", hasAi ? trueStr : falseStr));
            this.localeManager.sendSimpleMessage(player, "command-stacktool-info-location", StringPlaceholders.builder("x", entity.getLocation().getBlockX())
                    .addPlaceholder("y", entity.getLocation().getBlockY()).addPlaceholder("z", entity.getLocation().getBlockZ()).addPlaceholder("world", entity.getWorld().getName()).build());
            this.localeManager.sendSimpleMessage(player, "command-stacktool-info-chunk", StringPlaceholders.builder("x", entity.getLocation().getChunk().getX())
                    .addPlaceholder("z", entity.getLocation().getChunk().getZ()).build());
        }
    }

    /**
     * Handles Shift Left Click for StackedItems
     *
     * @param event The PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!StackerUtils.isStackingTool(tool))
            return;

        event.setCancelled(true);

        if (!player.isSneaking() || event.getHand() != EquipmentSlot.HAND)
            return;

        if (!player.hasPermission("rosestacker.stacktool")) {
            this.localeManager.sendMessage(player, "command-stacktool-no-permission");
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
                if (entity.getType() != EntityType.DROPPED_ITEM)
                    continue;

                Item item = (Item) entity;
                if (!StackerUtils.isLookingAtItem(player, item))
                    continue;

                StackedItem stackedItem = this.stackManager.getStackedItem(item);
                if (stackedItem == null)
                    continue;

                ItemMeta itemMeta = item.getItemStack().getItemMeta();

                this.localeManager.sendMessage(player, "command-stacktool-info");
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-id", StringPlaceholders.single("id", stackedItem.getId()));
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-uuid", StringPlaceholders.single("uuid", item.getUniqueId().toString()));
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-entity-id", StringPlaceholders.single("id", item.getEntityId()));
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-item-type", StringPlaceholders.single("type", item.getItemStack().getType().name()));
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-stack-size", StringPlaceholders.single("amount", stackedItem.getStackSize()));
                if (itemMeta != null && itemMeta.hasDisplayName())
                    this.localeManager.sendSimpleMessage(player, "command-stacktool-info-custom-name", StringPlaceholders.single("name", itemMeta.getDisplayName()));
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-location", StringPlaceholders.builder("x", item.getLocation().getBlockX())
                        .addPlaceholder("y", item.getLocation().getBlockY()).addPlaceholder("z", item.getLocation().getBlockZ()).addPlaceholder("world", item.getWorld().getName()).build());
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-chunk", StringPlaceholders.builder("x", item.getLocation().getChunk().getX())
                        .addPlaceholder("z", item.getLocation().getChunk().getZ()).build());

                return;
            }
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock == null)
                return;

            if (clickedBlock.getType() != Material.SPAWNER) {
                StackedBlock stackedBlock = this.stackManager.getStackedBlock(clickedBlock);
                if (stackedBlock == null)
                    return;

                this.localeManager.sendMessage(player, "command-stacktool-info");
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-id", StringPlaceholders.single("id", stackedBlock.getId()));
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-block-type", StringPlaceholders.single("type", clickedBlock.getType().name()));
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-stack-size", StringPlaceholders.single("amount", stackedBlock.getStackSize()));
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-location", StringPlaceholders.builder("x", clickedBlock.getX())
                        .addPlaceholder("y", clickedBlock.getY()).addPlaceholder("z", clickedBlock.getZ()).addPlaceholder("world", clickedBlock.getWorld().getName()).build());
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-chunk", StringPlaceholders.builder("x", clickedBlock.getChunk().getX())
                        .addPlaceholder("z", clickedBlock.getChunk().getZ()).build());
            } else {
                StackedSpawner stackedSpawner = this.stackManager.getStackedSpawner(clickedBlock);
                if (stackedSpawner == null)
                    return;

                this.localeManager.sendMessage(player, "command-stacktool-info");
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-id", StringPlaceholders.single("id", stackedSpawner.getId()));
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-spawner-type", StringPlaceholders.single("type", stackedSpawner.getSpawner().getSpawnedType().name()));
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-stack-size", StringPlaceholders.single("amount", stackedSpawner.getStackSize()));
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-location", StringPlaceholders.builder("x", clickedBlock.getX())
                        .addPlaceholder("y", clickedBlock.getY()).addPlaceholder("z", clickedBlock.getZ()).addPlaceholder("world", clickedBlock.getWorld().getName()).build());
                this.localeManager.sendSimpleMessage(player, "command-stacktool-info-chunk", StringPlaceholders.builder("x", clickedBlock.getChunk().getX())
                        .addPlaceholder("z", clickedBlock.getChunk().getZ()).build());
            }
        }

        // Make the item bob a little bit
        player.updateInventory();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.selectedEntities.remove(event.getPlayer().getUniqueId());
    }

    private static class SelectedEntities {

        private StackedEntity entity1, entity2;

        public void select(StackedEntity entity) {
            if (this.entity1 == null) {
                this.entity1 = entity;
            } else if (this.entity2 == null) {
                this.entity2 = entity;
            }
        }

        public void unselect() {
            this.entity1 = null;
            this.entity2 = null;
        }

        public boolean hasSelected() {
            return this.entity1 != null && this.entity2 != null;
        }

        public StackedEntity getEntity1() {
            return this.entity1;
        }

        public StackedEntity getEntity2() {
            return this.entity2;
        }

    }

}
