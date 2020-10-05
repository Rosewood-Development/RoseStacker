package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClick(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!(event.getRightClicked() instanceof LivingEntity)
                || event.getHand() != EquipmentSlot.HAND
                || !StackerUtils.isStackingTool(item))
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

        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeftClickEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;

        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!StackerUtils.isStackingTool(item))
            return;

        event.setCancelled(true);

        if (!player.isSneaking()) {

        } else {

        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!StackerUtils.isStackingTool(item))
            return;

        event.setCancelled(true);
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
