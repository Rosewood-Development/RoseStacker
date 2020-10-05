package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
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

public class StackToolListener implements Listener {

    private final RosePlugin rosePlugin;
    private final StackManager stackManager;
    private final LocaleManager localeManager;

    private Map<UUID, SelectedEntities> selectedEntities;

    public StackToolListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
        this.stackManager = this.rosePlugin.getManager(StackManager.class);
        this.localeManager = this.rosePlugin.getManager(LocaleManager.class);

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

        if (!player.isSneaking()) {
            SelectedEntities selected = this.selectedEntities.get(player.getUniqueId());
            if (selected == null) {
                selected = new SelectedEntities();
                this.selectedEntities.put(player.getUniqueId(), selected);
            }

            selected.select(entity);

            if (!selected.hasSelected()) {
                this.localeManager.sendMessage(player, "command-stacktool-select-1", StringPlaceholders.single("type", stackedEntity.getStackSettings().getDisplayName()));
            } else {

            }
        } else {
            player.sendMessage("Entity info: " + event.getEntityType().name());
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

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
                if (entity.getType() != EntityType.DROPPED_ITEM)
                    continue;

                Item item = (Item) entity;
                if (!StackerUtils.isLookingAtItem(player, item))
                    continue;

                player.sendMessage("Item info: " + item.getItemStack().getType().name());
            }
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock == null)
                return;

            if (clickedBlock.getType() != Material.SPAWNER) {
                player.sendMessage("Block info: " + clickedBlock.getType().name());
            } else {
                player.sendMessage("Spawner info: " + ((CreatureSpawner) clickedBlock.getState()).getSpawnedType().name());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.selectedEntities.remove(event.getPlayer().getUniqueId());
    }

    private static class SelectedEntities {

        private LivingEntity entity1, entity2;

        public void select(LivingEntity entity) {
            if (this.entity1 == null) {
                this.entity1 = entity;
            } else if (this.entity2 == null) {
                this.entity2 = entity;
            }
        }

        public boolean hasSelected() {
            return this.entity1 != null && this.entity2 != null;
        }

        public LivingEntity getEntity1() {
            return this.entity1;
        }

        public LivingEntity getEntity2() {
            return this.entity2;
        }

    }

}
