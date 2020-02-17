package dev.esophose.sparkstacker.listener;

import com.bgsoftware.wildstacker.listeners.events.EggLayEvent;
import dev.esophose.sparkstacker.SparkStacker;
import dev.esophose.sparkstacker.manager.ConfigurationManager.Setting;
import dev.esophose.sparkstacker.manager.StackManager;
import dev.esophose.sparkstacker.manager.StackSettingManager;
import dev.esophose.sparkstacker.stack.StackedEntity;
import dev.esophose.sparkstacker.stack.settings.SpawnerStackSettings;
import dev.esophose.sparkstacker.stack.settings.entity.ChickenStackSettings;
import dev.esophose.sparkstacker.utils.EntitySerializer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class EntityListener implements Listener {

    private SparkStacker sparkStacker;

    public EntityListener(SparkStacker sparkStacker) {
        this.sparkStacker = sparkStacker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        StackManager stackManager = this.sparkStacker.getStackManager();
        if (stackManager.isEntityStackingTemporarilyDisabled())
            return;

        if (event.getEntity() instanceof Item)
            stackManager.createItemStack((Item) event.getEntity(), true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        StackManager stackManager = this.sparkStacker.getStackManager();
        if (stackManager.isEntityStackingTemporarilyDisabled())
            return;

        stackManager.createEntityStack(event.getEntity(), true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getTo() == null)
            return;

        StackManager stackManager = this.sparkStacker.getStackManager();
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            if (stackManager.isEntityStacked(livingEntity))
                stackManager.changeStackingThread(livingEntity, event.getFrom().getWorld(), event.getTo().getWorld());
        } else if (entity instanceof Item) {
            Item item = (Item) entity;
            if (stackManager.isItemStacked(item))
                stackManager.changeStackingThread(item, event.getFrom().getWorld(), event.getTo().getWorld());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        SpawnerStackSettings spawnerStackSettings = this.sparkStacker.getStackSettingManager().getSpawnerStackSettings(event.getSpawner());

        // TODO: Custom spawner mob properties
        if (spawnerStackSettings.isMobAIDisabled()) {
            AttributeInstance movementAttribute = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (movementAttribute != null)
                movementAttribute.setBaseValue(0);
        }

        // Tag the entity as spawned from a spawner
        entity.setMetadata("spawner_spawned", new FixedMetadataValue(this.sparkStacker, true));
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
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;

        this.handleEntityDeath(event, (LivingEntity) event.getEntity(), false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        this.handleEntityDeath(event, event.getEntity(), true);
    }

    private void handleEntityDeath(EntityEvent event, LivingEntity entity, boolean useLastDamageCause) {
        StackManager stackManager = this.sparkStacker.getStackManager();
        StackedEntity stackedEntity = stackManager.getStackedEntity(entity);
        if (stackedEntity == null)
            return;

        if (stackedEntity.getStackSize() == 1) {
            stackManager.removeEntityStack(stackedEntity);
            return;
        }

        // Should we kill the entire stack at once?
        EntityDamageEvent lastDamageCause = entity.getLastDamageCause();
        if (useLastDamageCause && (stackedEntity.getStackSettings().shouldKillEntireStackOnDeath()
                || (lastDamageCause != null && Setting.ENTITY_KILL_ENTIRE_STACK_CONDITIONS.getStringList().stream().anyMatch(x -> x.equalsIgnoreCase(lastDamageCause.getCause().name()))))) {

            if (Setting.ENTITY_DROP_ACCURATE_ITEMS.getBoolean()) {
                if (event instanceof EntityDeathEvent) {
                    EntityDeathEvent deathEvent = (EntityDeathEvent) event;
                    stackedEntity.dropStackLoot(deathEvent.getDrops(), deathEvent.getDroppedExp());
                    deathEvent.getDrops().clear();
                } else {
                    stackedEntity.dropStackLoot(null, 0);
                }
            } else if (Setting.ENTITY_DROP_ACCURATE_EXP.getBoolean() && event instanceof EntityDeathEvent) {
                EntityDeathEvent deathEvent = (EntityDeathEvent) event;
                deathEvent.setDroppedExp(deathEvent.getDroppedExp() * stackedEntity.getStackSize());
            }

            stackManager.removeEntityStack(stackedEntity);
            return;
        }

        // Decrease stack size by 1, hide the name so it doesn't display two stack tags at once
        entity.setCustomName(null);
        entity.setCustomNameVisible(false);
        stackManager.setEntityStackingTemporarilyDisabled(true);
        stackedEntity.decreaseStackSize();
        stackManager.setEntityStackingTemporarilyDisabled(false);
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
        StackManager stackManager = this.sparkStacker.getStackManager();
        if (!(event.getEntity() instanceof LivingEntity)
                || !(event.getTransformedEntity() instanceof LivingEntity)
                || event.getEntity().getType() == event.getTransformedEntity().getType()
                || !stackManager.isEntityStacked((LivingEntity) event.getEntity()))
            return;

        StackedEntity stackedEntity = stackManager.getStackedEntity((LivingEntity) event.getEntity());
        if (stackedEntity.getStackSize() == 1)
            return;

        LivingEntity transformedEntity = (LivingEntity) event.getTransformedEntity();
        if (Setting.ENTITY_TRANSFORM_ENTIRE_STACK.getBoolean()) {
            String serialized = EntitySerializer.toNBTString(transformedEntity);
            event.setCancelled(true);
            event.getEntity().remove();
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.sparkStacker, () -> {
                stackManager.setEntityStackingTemporarilyDisabled(true);
                StackedEntity newStack = stackManager.createEntityStack(EntitySerializer.fromNBTString(serialized, transformedEntity.getLocation()), false);
                stackManager.setEntityStackingTemporarilyDisabled(false);
                if (newStack == null)
                    return;

                for (String serializedEntity : stackedEntity.getStackedEntityNBTStrings())
                    newStack.increaseStackSize(EntitySerializer.getNBTStringAsEntity(transformedEntity.getType(), transformedEntity.getLocation(), serializedEntity));
            });
        } else {
            // Wait for potential lightning to go away
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.sparkStacker, stackedEntity::decreaseStackSize, 20);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEggLay(EntityDropItemEvent event) {
        if (event.getEntityType() != EntityType.CHICKEN || event.getItemDrop().getItemStack().getType() != Material.EGG)
            return;

        StackSettingManager stackSettingManager = this.sparkStacker.getStackSettingManager();
        ChickenStackSettings chickenStackSettings = (ChickenStackSettings) stackSettingManager.getEntityStackSettings(EntityType.CHICKEN);
        if (!chickenStackSettings.shouldMultiplyEggDropsByStackSize())
            return;

        StackManager stackManager = this.sparkStacker.getStackManager();
        Chicken chickenEntity = (Chicken) event.getEntity();
        StackedEntity stackedEntity = stackManager.getStackedEntity(chickenEntity);
        if (stackedEntity == null || stackedEntity.getStackSize() == 1)
            return;

        event.getItemDrop().remove();

        int maxStackSize = Material.EGG.getMaxStackSize();
        int eggs = stackedEntity.getStackSize();
        int fullItemStacks = (int) Math.floor((double) eggs / maxStackSize);
        int remainingEggs = eggs % maxStackSize;

        List<ItemStack> eggStacks = new ArrayList<>();
        for (int i = 0; i < fullItemStacks; i++)
            eggStacks.add(new ItemStack(Material.EGG, maxStackSize));
        if (remainingEggs > 0)
            eggStacks.add(new ItemStack(Material.EGG, remainingEggs));

        stackManager.preStackItems(eggStacks, event.getEntity().getLocation());
    }

}
