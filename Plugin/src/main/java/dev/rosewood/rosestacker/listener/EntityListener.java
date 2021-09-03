package dev.rosewood.rosestacker.listener;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.event.AsyncEntityDeathEvent;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.object.WrappedNBT;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedItem;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.entity.ChickenStackSettings;
import dev.rosewood.rosestacker.stack.settings.entity.SheepStackSettings;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.MushroomCow.Variant;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.EntityTransformEvent.TransformReason;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class EntityListener implements Listener {

    private final RosePlugin rosePlugin;
    private final StackManager stackManager;
    private final StackSettingManager stackSettingManager;
    private final EntityCacheManager entityCacheManager;

    public EntityListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;

        this.stackManager = this.rosePlugin.getManager(StackManager.class);
        this.stackSettingManager = this.rosePlugin.getManager(StackSettingManager.class);
        this.entityCacheManager = this.rosePlugin.getManager(EntityCacheManager.class);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (this.stackManager.isWorldDisabled(entity.getWorld()))
            return;

        if (!this.stackManager.isItemStackingEnabled() || this.stackManager.isEntityStackingTemporarilyDisabled())
            return;

        if (entity instanceof Item) {
            this.entityCacheManager.preCacheEntity(entity);
            this.stackManager.createItemStack((Item) entity, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (this.stackManager.isWorldDisabled(entity.getWorld()))
            return;

        if (!this.stackManager.isEntityStackingEnabled() || this.stackManager.isEntityStackingTemporarilyDisabled())
            return;

        PersistentDataUtils.setEntitySpawnReason(entity, event.getSpawnReason());
        this.entityCacheManager.preCacheEntity(entity);
        this.stackManager.createEntityStack(entity, true);

        PersistentDataUtils.applyDisabledAi(entity);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        PersistentDataUtils.tagSpawnedFromSpawner(entity);
        StackedSpawner stackedSpawner = this.stackManager.getStackedSpawner(event.getSpawner().getBlock());
        boolean placedByPlayer = stackedSpawner != null && stackedSpawner.isPlacedByPlayer();
        if (this.stackSettingManager.getSpawnerStackSettings(event.getSpawner()).isMobAIDisabled() && (!Setting.SPAWNER_DISABLE_MOB_AI_ONLY_PLAYER_PLACED.getBoolean() || placedByPlayer))
            PersistentDataUtils.removeEntityAi(entity);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        // Withers can still target enitites due to custom boss AI, so prevent them from targeting when AI is disabled
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity))
            return;

        boolean disableAttacking = (event.getEntityType() == EntityType.WITHER && PersistentDataUtils.isAiDisabled((Wither) event.getEntity()))
                || (Setting.SPAWNER_DISABLE_ATTACKING.getBoolean()) && PersistentDataUtils.isSpawnedFromSpawner((LivingEntity) event.getEntity());
        if (disableAttacking)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTeleport(EntityTeleportEvent event) {
        // Endermen can still target enitites due to custom dodging AI, so prevent them from teleporting when AI is disabled
        if (event.getEntityType() == EntityType.ENDERMAN && PersistentDataUtils.isAiDisabled((Enderman) event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTeleport(EntityPortalEvent event) {
        if (event.getTo() == null || event.getFrom().getWorld() == event.getTo().getWorld())
            return;

        if (this.stackManager.isWorldDisabled(event.getEntity().getWorld()))
            return;

        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            if (!this.stackManager.isEntityStackingEnabled())
                return;

            LivingEntity livingEntity = (LivingEntity) entity;
            StackedEntity stackedEntity = this.stackManager.getStackedEntity(livingEntity);
            if (stackedEntity != null) {
                Bukkit.getScheduler().runTask(this.rosePlugin, () -> {
                    this.stackManager.changeStackingThread(livingEntity.getUniqueId(), stackedEntity, event.getFrom().getWorld(), event.getTo().getWorld());
                    stackedEntity.updateDisplay();
                });
            }
        } else if (entity instanceof Item) {
            if (!this.stackManager.isItemStackingEnabled())
                return;

            Item item = (Item) entity;
            StackedItem stackedItem = this.stackManager.getStackedItem(item);
            if (stackedItem != null)
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Prevent guardians with disabled AI from spiking their attacker
        if (event.getEntity().getType() == EntityType.PLAYER
                && event.getDamager() instanceof Guardian
                && PersistentDataUtils.isAiDisabled((Guardian) event.getDamager())) {
            event.setCancelled(true);
        }

        if (!(event.getEntity() instanceof LivingEntity) || event.getEntity().getType() == EntityType.PLAYER)
            return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        if (!Setting.ENTITY_INSTANT_KILL_DISABLED_AI.getBoolean() || this.stackManager.isWorldDisabled(entity.getWorld()) || !PersistentDataUtils.isAiDisabled(entity))
            return;

        Entity damager = event.getDamager();
        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (!(projectile.getShooter() instanceof Player))
                return;
        } else if (!(damager instanceof Player)) {
            return;
        }

        AttributeInstance attributeInstance = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attributeInstance != null) {
            event.setDamage(attributeInstance.getValue() * 2);
        } else {
            event.setDamage(entity.getHealth() * 2);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity) || event.getEntity().getType() == EntityType.ARMOR_STAND || event.getEntity().getType() == EntityType.PLAYER)
            return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        if (this.stackManager.isWorldDisabled(entity.getWorld()))
            return;

        if (!this.stackManager.isEntityStackingEnabled())
            return;

        StackedEntity stackedEntity = this.stackManager.getStackedEntity(entity);
        if (stackedEntity == null || stackedEntity.getStackSize() == 1)
            return;

        if (!Setting.ENTITY_SHARE_DAMAGE_CONDITIONS.getStringList().contains(event.getCause().name()))
            return;

        double damage = event.getFinalDamage();

        List<LivingEntity> killedEntities = new ArrayList<>();
        List<LivingEntity> internalEntities = StackerUtils.deconstructStackedEntities(stackedEntity);
        for (LivingEntity internal : internalEntities) {
            double health = internal.getHealth();
            if (health - damage <= 0) {
                killedEntities.add(internal);
            } else {
                internal.setHealth(health - damage);
            }
        }

        // Only try dropping loot if something actually died
        if (!killedEntities.isEmpty()) {
            internalEntities.removeIf(killedEntities::contains);

            // Pick a random amount of exp to drop based on the entity type
            // This is only an incredibly rough estimate and isn't 1:1 with vanilla
            int experience;
            Class<? extends Entity> type = stackedEntity.getStackSettings().getEntityType().getEntityClass();
            if (type == null || NPC.class.isAssignableFrom(type) || Golem.class.isAssignableFrom(type) || type == Bat.class) {
                experience = 0;
            } else if (Animals.class.isAssignableFrom(type)) {
                experience = StackerUtils.randomInRange(1, 3);
            } else if (type == Wither.class) {
                experience = 50;
            } else if (type == Blaze.class || Guardian.class.isAssignableFrom(type)) {
                experience = 10;
            } else {
                experience = 5;
            }

            stackedEntity.dropPartialStackLoot(killedEntities, new ArrayList<>(), experience);
        }

        StackerUtils.reconstructStackedEntities(stackedEntity, internalEntities);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        Entity entity = event.getEntity();
        if (event instanceof EntityCombustByBlockEvent || event instanceof EntityCombustByEntityEvent || !(entity instanceof LivingEntity))
            return;

        // Don't allow mobs to naturally burn in the daylight if their AI is disabled
        if (PersistentDataUtils.isAiDisabled((LivingEntity) entity) && !Setting.SPAWNER_DISABLE_MOB_AI_OPTIONS_UNDEAD_BURN_IN_DAYLIGHT.getBoolean())
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
        if (!(event instanceof AsyncEntityDeathEvent))
            this.handleEntityDeath(event, event.getEntity(), true);
    }

    private void handleEntityDeath(EntityEvent event, LivingEntity entity, boolean useLastDamageCause) {
        if (this.stackManager.isWorldDisabled(entity.getWorld()))
            return;

        if (!this.stackManager.isEntityStackingEnabled())
            return;

        StackedEntity stackedEntity = this.stackManager.getStackedEntity(entity);
        if (stackedEntity == null)
            return;

        if (stackedEntity.getStackSize() == 1) {
            this.stackManager.removeEntityStack(stackedEntity);
            return;
        }

        // Should we kill the entire stack at once?
        EntityDamageEvent lastDamageCause = entity.getLastDamageCause();
        if (useLastDamageCause && (stackedEntity.getStackSettings().shouldKillEntireStackOnDeath()
                || (Setting.SPAWNER_DISABLE_MOB_AI_OPTIONS_KILL_ENTIRE_STACK_ON_DEATH.getBoolean() && PersistentDataUtils.isAiDisabled(entity))
                || (lastDamageCause != null && Setting.ENTITY_KILL_ENTIRE_STACK_CONDITIONS.getStringList().stream().anyMatch(x -> x.equalsIgnoreCase(lastDamageCause.getCause().name()))))) {

            if (Setting.ENTITY_DROP_ACCURATE_ITEMS.getBoolean()) {
                if (event instanceof EntityDeathEvent) {
                    EntityDeathEvent deathEvent = (EntityDeathEvent) event;
                    stackedEntity.dropStackLoot(new ArrayList<>(deathEvent.getDrops()), deathEvent.getDroppedExp());
                    deathEvent.getDrops().clear();
                } else {
                    stackedEntity.dropStackLoot(null, 0);
                }
            } else if (Setting.ENTITY_DROP_ACCURATE_EXP.getBoolean() && event instanceof EntityDeathEvent) {
                EntityDeathEvent deathEvent = (EntityDeathEvent) event;
                deathEvent.setDroppedExp(deathEvent.getDroppedExp() * stackedEntity.getStackSize());
            }

            this.stackManager.removeEntityStack(stackedEntity);
            return;
        }

        // Decrease stack size by 1
        stackedEntity.updateDisplay();
        stackedEntity.decreaseStackSize();
        stackedEntity.getEntity().setVelocity(new Vector());

        if (Setting.ENTITY_KILL_TRANSFER_VELOCITY.getBoolean()) {
            stackedEntity.getEntity().setVelocity(entity.getVelocity());
            entity.setVelocity(new Vector());
        }

        if (!Setting.ENTITY_DISPLAY_CORPSE.getBoolean())
            event.getEntity().remove();
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
        if (this.stackManager.isWorldDisabled(event.getEntity().getWorld()))
            return;

        if (!this.stackManager.isEntityStackingEnabled())
            return;

        if (!(event.getEntity() instanceof LivingEntity)
                || !(event.getTransformedEntity() instanceof LivingEntity)
                || event.getEntity().getType() == event.getTransformedEntity().getType()
                || !this.stackManager.isEntityStacked((LivingEntity) event.getEntity()))
            return;

        StackedEntity stackedEntity = this.stackManager.getStackedEntity((LivingEntity) event.getEntity());
        if (stackedEntity.getStackSize() == 1)
            return;

        LivingEntity transformedEntity = (LivingEntity) event.getTransformedEntity();
        if (Setting.ENTITY_TRANSFORM_ENTIRE_STACK.getBoolean()) {
            NMSHandler nmsHandler = NMSAdapter.getHandler();
            WrappedNBT<?> serialized = nmsHandler.getEntityAsNBT(transformedEntity);
            event.setCancelled(true);

            // Handle mooshroom shearing
            if (event.getEntityType() == EntityType.MUSHROOM_COW) {
                int mushroomsDropped = stackedEntity.getStackSize() * 5; // 5 mushrooms per mooshroom sheared

                MushroomCow mooshroom = (MushroomCow) event.getEntity();
                Material dropType;
                if (NMSUtil.getVersionNumber() > 13) {
                    if (mooshroom.getVariant() == Variant.BROWN) {
                        dropType = Material.BROWN_MUSHROOM;
                    } else {
                        dropType = Material.RED_MUSHROOM;
                    }
                } else {
                    dropType = Material.RED_MUSHROOM;
                }

                this.stackManager.preStackItems(GuiUtil.getMaterialAmountAsItemStacks(dropType, mushroomsDropped), event.getEntity().getLocation());
            }

            boolean aiDisabled = PersistentDataUtils.isAiDisabled((LivingEntity) event.getEntity());
            event.getEntity().remove();
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.rosePlugin, () -> {
                this.stackManager.setEntityStackingTemporarilyDisabled(true);
                LivingEntity newEntity = nmsHandler.createEntityFromNBT(serialized, transformedEntity.getLocation(), true, transformedEntity.getType());
                if (aiDisabled)
                    PersistentDataUtils.removeEntityAi(newEntity);
                StackedEntity newStack = this.stackManager.createEntityStack(newEntity, false);
                this.stackManager.setEntityStackingTemporarilyDisabled(false);
                if (newStack == null)
                    return;

                for (WrappedNBT<?> serializedEntity : stackedEntity.getStackedEntityNBT().getAll()) {
                    LivingEntity entity = nmsHandler.createEntityFromNBT(serializedEntity, transformedEntity.getLocation(), false, transformedEntity.getType());
                    if (aiDisabled)
                        PersistentDataUtils.removeEntityAi(entity);
                    newStack.increaseStackSize(entity);
                }
            });
        } else {
            // Make sure disabled AI gets transferred
            if (PersistentDataUtils.isAiDisabled((LivingEntity) event.getEntity()))
                PersistentDataUtils.removeEntityAi((LivingEntity) event.getTransformedEntity());

            if (event.getTransformReason() == TransformReason.LIGHTNING) { // Wait for lightning to disappear
                Bukkit.getScheduler().scheduleSyncDelayedTask(this.rosePlugin, stackedEntity::decreaseStackSize, 20);
            } else {
                Bukkit.getScheduler().runTask(this.rosePlugin, stackedEntity::decreaseStackSize);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChickenLayEgg(EntityDropItemEvent event) {
        if (event.getEntityType() != EntityType.CHICKEN || event.getItemDrop().getItemStack().getType() != Material.EGG)
            return;

        if (this.stackManager.isWorldDisabled(event.getEntity().getWorld()))
            return;

        if (!this.stackManager.isEntityStackingEnabled())
            return;

        Chicken chickenEntity = (Chicken) event.getEntity();
        StackedEntity stackedEntity = this.stackManager.getStackedEntity(chickenEntity);
        if (stackedEntity == null || stackedEntity.getStackSize() == 1)
            return;

        ChickenStackSettings chickenStackSettings = (ChickenStackSettings) stackedEntity.getStackSettings();
        if (!chickenStackSettings.shouldMultiplyEggDropsByStackSize())
            return;

        event.getItemDrop().remove();
        List<ItemStack> items = GuiUtil.getMaterialAmountAsItemStacks(Material.EGG, stackedEntity.getStackSize());
        this.stackManager.preStackItems(items, event.getEntity().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerShearSheep(PlayerShearEntityEvent event) {
        ItemStack tool = event.getPlayer().getInventory().getItem(event.getHand()).clone();
        if (tool.getType() != Material.SHEARS || !handleSheepShear(this.rosePlugin, tool, event.getEntity()))
            return;

        event.setCancelled(true);

        if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
            event.getPlayer().getInventory().setItem(event.getHand(), tool);
    }

    public static boolean handleSheepShear(RosePlugin rosePlugin, ItemStack shears, Entity entity) {
        if (entity.getType() != EntityType.SHEEP)
            return false;

        StackManager stackManager = rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(entity.getWorld()))
            return false;

        if (stackManager.isWorldDisabled(entity.getWorld()))
            return false;

        if (!stackManager.isEntityStackingEnabled())
            return false;

        Sheep sheepEntity = (Sheep) entity;
        StackedEntity stackedEntity = stackManager.getStackedEntity(sheepEntity);
        if (stackedEntity == null || stackedEntity.getStackSize() == 1)
            return false;

        SheepStackSettings sheepStackSettings = (SheepStackSettings) stackedEntity.getStackSettings();
        if (!sheepStackSettings.shouldShearAllSheepInStack())
            return false;

        ItemUtils.damageTool(shears);

        ItemStack baseSheepWool = new ItemStack(ItemUtils.getWoolMaterial(sheepEntity.getColor()), getWoolDropAmount());
        sheepEntity.setSheared(true);
        List<ItemStack> drops = new ArrayList<>(Collections.singletonList(baseSheepWool));

        stackManager.setEntityUnstackingTemporarilyDisabled(true);
        Bukkit.getScheduler().runTaskAsynchronously(RoseStacker.getInstance(), () -> {
            try {
                List<Sheep> sheepList = StackerUtils.deconstructStackedEntities(stackedEntity).stream()
                        .map(x -> (Sheep) x)
                        .collect(Collectors.toList());

                for (Sheep sheep : sheepList) {
                    if (!sheep.isSheared()) {
                        ItemStack sheepWool = new ItemStack(ItemUtils.getWoolMaterial(sheep.getColor()), getWoolDropAmount());
                        sheep.setSheared(true);
                        drops.add(sheepWool);
                    }
                }
                StackerUtils.reconstructStackedEntities(stackedEntity, sheepList);

                Bukkit.getScheduler().runTask(RoseStacker.getInstance(), () -> stackManager.preStackItems(drops, sheepEntity.getLocation()));
            } finally {
                stackManager.setEntityUnstackingTemporarilyDisabled(false);
            }
        });

        return true;
    }

    /**
     * @return a number between 1 and 3 inclusively
     */
    private static int getWoolDropAmount() {
        return (int) (Math.random() * 3) + 1;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSheepRegrowWool(SheepRegrowWoolEvent event) {
        if (this.stackManager.isWorldDisabled(event.getEntity().getWorld()))
            return;

        if (!this.stackManager.isEntityStackingEnabled())
            return;

        Sheep sheepEntity = event.getEntity();
        StackedEntity stackedEntity = this.stackManager.getStackedEntity(sheepEntity);
        if (stackedEntity == null || stackedEntity.getStackSize() == 1)
            return;

        SheepStackSettings sheepStackSettings = (SheepStackSettings) stackedEntity.getStackSettings();
        double regrowPercentage = sheepStackSettings.getPercentageOfWoolToRegrowPerGrassEaten() / 100D;
        int regrowAmount = Math.max(1, (int) Math.round(stackedEntity.getStackSize() * regrowPercentage));

        if (sheepEntity.isSheared()) {
            sheepEntity.setSheared(false);
            regrowAmount--;
        }

        if (regrowAmount < 1)
            return;

        int fRegrowAmount = regrowAmount;
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            int remaining = fRegrowAmount;

            List<Sheep> sheepList = StackerUtils.deconstructStackedEntities(stackedEntity).stream()
                    .map(x -> (Sheep) x)
                    .collect(Collectors.toList());

            for (Sheep sheep : sheepList) {
                if (sheep.isSheared()) {
                    sheep.setSheared(false);
                    remaining--;
                    if (remaining <= 0)
                        break;
                }
            }

            StackerUtils.reconstructStackedEntities(stackedEntity, sheepList);
        });
    }

}
