package dev.esophose.rosestacker.listener;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.stack.StackedEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntityListener implements Listener {

    private RoseStacker roseStacker;

    private boolean ignoreNextCreatureSpawn;

    public EntityListener(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
        this.ignoreNextCreatureSpawn = false;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Item)
            this.roseStacker.getStackManager().createStackFromEntity(event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (this.ignoreNextCreatureSpawn) {
            this.ignoreNextCreatureSpawn = false;
            return;
        }

        if (event.getEntityType() != EntityType.ARMOR_STAND)
            this.roseStacker.getStackManager().createStackFromEntity(event.getEntity());

        // TODO: Custom spawner mob properties
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();

        if (!(event.getEntity() instanceof LivingEntity) || !stackManager.isEntityStacked(event.getEntity()))
            return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        if (entity.getHealth() - event.getFinalDamage() > 0)
            return;

        StackedEntity stackedEntity = stackManager.getStackedEntity(entity);
        if (stackedEntity.getStackSize() == 1) {
            stackManager.removeEntity(stackedEntity);
            return;
        }

        // TODO: Kill-all-stack for certain death types
        if (event instanceof EntityDamageByBlockEvent) {

        } else if (event instanceof EntityDamageByEntityEvent) {

        } else {

        }

        // Decrease stack size by 1, hide the name so it doesn't display two stack tags at once
        entity.setCustomName(null);
        entity.setCustomNameVisible(false);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.roseStacker, () -> {
            this.ignoreNextCreatureSpawn = true;
            stackedEntity.decreaseStackSize();
        });
    }

}
