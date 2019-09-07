package dev.esophose.rosestacker.listener;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.utils.EntitySerializer;
import net.minecraft.server.v1_14_R1.EntityZombie;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.PigZapEvent;

public class EntityListener implements Listener {

    private RoseStacker roseStacker;

    public EntityListener(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();
        if (stackManager.isEntityStackingDisabled())
            return;

        if (event.getEntity() instanceof Item)
            stackManager.createStackFromEntity(event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();
        if (stackManager.isEntityStackingDisabled())
            return;

        // TODO: Custom spawner mob properties
        if (event.getSpawnReason() == SpawnReason.SPAWNER) {
            AttributeInstance movementAttribute = event.getEntity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (movementAttribute != null)
                movementAttribute.setBaseValue(0);
        }

        this.roseStacker.getStackManager().createStackFromEntity(event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;

        LivingEntity entity = (LivingEntity) event.getEntity();

        AttributeInstance movementAttribute = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (movementAttribute != null && movementAttribute.getBaseValue() == 0)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();
        if (!stackManager.isEntityStacked(event.getEntity()))
            return;

        LivingEntity entity = event.getEntity();

        StackedEntity stackedEntity = stackManager.getStackedEntity(entity);
        if (stackedEntity.getStackSize() == 1) {
            stackManager.removeEntity(stackedEntity);
            return;
        }

        // Should we kill the entire stack at once?
        EntityDamageEvent lastDamageCause = entity.getLastDamageCause();
        if (stackedEntity.getStackSettings().shouldKillEntireStackOnDeath()
                || (lastDamageCause != null && Setting.ENTITY_KILL_ENTIRE_STACK_CONDITIONS.getStringList().stream().anyMatch(x -> x.equalsIgnoreCase(lastDamageCause.getCause().name())))) {

            if (Setting.ENTITY_DROP_ACCURATE_ITEMS.getBoolean())
                stackedEntity.dropStackLoot();
            if (Setting.ENTITY_DROP_ACCURATE_EXP.getBoolean())
                event.setDroppedExp(event.getDroppedExp() * stackedEntity.getStackSize());

            stackManager.removeEntity(stackedEntity);
            return;
        }

        // Decrease stack size by 1, hide the name so it doesn't display two stack tags at once
        entity.setCustomName(null);
        entity.setCustomNameVisible(false);
        stackManager.setEntityStackingDisabled(true);
        stackedEntity.decreaseStackSize();
        stackManager.setEntityStackingDisabled(false);
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        // TODO: Unload an entity if it teleports into an unloaded chunk

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTransform(EntityTransformEvent event) {
        this.handleEntityTransformation(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPigZap(PigZapEvent event) {
        this.handleEntityTransformation(event);
    }

    private void handleEntityTransformation(EntityTransformEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();
        if (!(event.getEntity() instanceof LivingEntity)
                || event.getEntity().getType() == event.getTransformedEntity().getType()
                || !stackManager.isEntityStacked(event.getEntity()))
            return;

        StackedEntity stackedEntity = stackManager.getStackedEntity(event.getEntity());
        if (stackedEntity.getStackSize() == 1)
            return;

        Entity transformedEntity = event.getTransformedEntity();
        if (Setting.ENTITY_TRANSFORM_ENTIRE_STACK.getBoolean()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.roseStacker, () -> {
                StackedEntity newStack = stackManager.getStackedEntity(transformedEntity);
                for (String serializedEntity : stackedEntity.getStackedEntityNBTStrings())
                    newStack.increaseStackSize(EntitySerializer.getNBTStringAsEntity(transformedEntity.getType(), transformedEntity.getLocation(), serializedEntity));
            });
        } else {
            // Wait for potential lightning to go away
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.roseStacker, stackedEntity::decreaseStackSize, 20);
        }
    }

}
