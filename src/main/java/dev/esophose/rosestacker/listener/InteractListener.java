package dev.esophose.rosestacker.listener;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import dev.esophose.rosestacker.utils.EntitySerializer;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class InteractListener implements Listener {

    private RoseStacker roseStacker;

    public InteractListener(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (event.getItem() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK || clickedBlock == null)
            return;

        ItemStack itemStack = event.getItem();
        if (!StackerUtils.isSpawnEgg(itemStack.getType()))
            return;

        int spawnAmount = StackerUtils.getStackedItemStackAmount(itemStack);
        if (spawnAmount == 1)
            return;

        EntityStackSettings stackSettings = this.roseStacker.getStackSettingManager().getEntityStackSettings(itemStack.getType());
        if (spawnAmount > stackSettings.getMaxStackSize()) {
            event.setCancelled(true);
            return;
        }

        EntityType entityType = stackSettings.getEntityType();
        Location spawnLocation = clickedBlock.getRelative(event.getBlockFace()).getLocation();
        if (spawnLocation.getWorld() == null)
            return;

        StackManager stackManager = this.roseStacker.getStackManager();
        stackManager.setEntityStackingTemporarilyDisabled(true);
        LivingEntity initialEntity = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, entityType);
        stackManager.setEntityStackingTemporarilyDisabled(false);

        initialEntity.setAI(false);
        initialEntity.setInvulnerable(true);

        StackedEntity stackedEntity = stackManager.createEntityStack(initialEntity, false);
        Bukkit.getScheduler().runTaskAsynchronously(this.roseStacker, () -> {
            for (int i = 0; i < spawnAmount - 1; i++) {
                LivingEntity newEntity = EntitySerializer.createEntityUnspawned(entityType, spawnLocation);
                stackedEntity.increaseStackSize(newEntity, false);
            }

            Bukkit.getScheduler().runTask(this.roseStacker, () -> {
                stackedEntity.updateDisplay();
                initialEntity.setAI(true);
                initialEntity.setInvulnerable(false);
            });
        });

        StackerUtils.takeOneItem(event.getPlayer(), event.getHand());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity))
            return;

        StackManager stackManager = this.roseStacker.getStackManager();

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
        }

        if (!StackerUtils.isSpawnEgg(itemStack.getType()))
            return;

        int spawnAmount = StackerUtils.getStackedItemStackAmount(itemStack);
        if (spawnAmount == 1)
            return;

        EntityStackSettings stackSettings = this.roseStacker.getStackSettingManager().getEntityStackSettings(itemStack.getType());
        if (spawnAmount > stackSettings.getMaxStackSize()) {
            event.setCancelled(true);
            return;
        }

        EntityType entityType = stackSettings.getEntityType();
        if (event.getRightClicked().getType() != entityType)
            return;

        Location spawnLocation = event.getRightClicked().getLocation();
        if (spawnLocation.getWorld() == null)
            return;

        StackedEntity stackedEntity = stackManager.getStackedEntity(entity);
        if (stackedEntity.getStackSize() + spawnAmount > stackSettings.getMaxStackSize()) {
            event.setCancelled(true);
            return;
        }

        entity.setAI(false);
        entity.setInvulnerable(true);

        String entityNbt = EntitySerializer.toNBTString(entity);
        Bukkit.getScheduler().runTaskAsynchronously(this.roseStacker, () -> {
            for (int i = 0; i < spawnAmount - 1; i++) {
                LivingEntity newEntity = EntitySerializer.getNBTStringAsEntity(entity.getType(), spawnLocation, entityNbt);
                stackedEntity.increaseStackSize(newEntity, false);
            }

            Bukkit.getScheduler().runTask(this.roseStacker, () -> {
                stackedEntity.updateDisplay();
                entity.setAI(true);
                entity.setInvulnerable(false);
            });
        });

        StackerUtils.takeOneItem(event.getPlayer(), event.getHand());
        event.setCancelled(true);
    }

}
