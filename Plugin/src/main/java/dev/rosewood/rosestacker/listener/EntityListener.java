package dev.rosewood.rosestacker.listener;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.compatibility.CompatibilityAdapter;
import dev.rosewood.rosegarden.compatibility.handler.ShearedHandler;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.event.AsyncEntityDeathEvent;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.storage.EntityDataEntry;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorageType;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedItem;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.ItemStackSettings;
import dev.rosewood.rosestacker.stack.settings.MultikillBound;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.utils.EntityUtils;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import dev.rosewood.rosestacker.utils.VersionUtils;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.MushroomCow.Variant;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
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

    private static final Set<SpawnReason> DELAYED_SPAWN_REASONS = EnumSet.of(
            SpawnReason.BEEHIVE,
            SpawnReason.BUILD_IRONGOLEM,
            SpawnReason.BUILD_SNOWMAN,
            SpawnReason.BUILD_WITHER
    );

    private final RosePlugin rosePlugin;
    private final StackManager stackManager;
    private final StackSettingManager stackSettingManager;

    public EntityListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
        this.stackManager = rosePlugin.getManager(StackManager.class);
        this.stackSettingManager = rosePlugin.getManager(StackSettingManager.class);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (this.stackManager.isAreaDisabled(entity.getLocation()))
            return;

        if (!this.stackManager.isItemStackingEnabled() || this.stackManager.isEntityStackingTemporarilyDisabled())
            return;

        if (entity instanceof Item item) {
            ItemStackSettings itemStackSettings = this.stackSettingManager.getItemStackSettings(item);
            if (itemStackSettings != null && !itemStackSettings.isStackingEnabled())
                return;

            StackedItem stackedItem = this.stackManager.createItemStack(item, true);
            if (stackedItem == null || stackedItem.getStackSize() > 0)
                this.rosePlugin.getManager(EntityCacheManager.class).preCacheEntity(entity);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (this.stackManager.isAreaDisabled(entity.getLocation()))
            return;

        if (!this.stackManager.isEntityStackingEnabled() || this.stackManager.isEntityStackingTemporarilyDisabled())
            return;

        Runnable task = () -> {
            // Try to immediately stack everything except bees from hives and built entities due to them duplicating
            this.rosePlugin.getManager(EntityCacheManager.class).preCacheEntity(entity);
            this.stackManager.createEntityStack(entity, !DELAYED_SPAWN_REASONS.contains(event.getSpawnReason()));

            PersistentDataUtils.applyDisabledAi(entity);
        };

        // Delay stacking by 1 tick for spawn eggs due to an egg duplication issue
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            ThreadUtils.runSync(task);
        } else {
            task.run();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || event.getSpawner() == null)
            return;

        if (this.stackManager.isAreaDisabled(event.getEntity().getLocation()))
            return;

        this.rosePlugin.getManager(EntityCacheManager.class).preCacheEntity(entity);

        PersistentDataUtils.tagSpawnedFromSpawner(entity);
        if (this.stackManager.isEntityStackingEnabled() && !this.stackManager.isEntityStackingTemporarilyDisabled())
            this.stackManager.createEntityStack(entity, true);

        SpawnerStackSettings stackSettings = this.stackSettingManager.getSpawnerStackSettings(event.getSpawner());
        StackedSpawner stackedSpawner = this.stackManager.getStackedSpawner(event.getSpawner().getBlock());
        if (stackedSpawner == null)
            stackedSpawner = this.stackManager.createSpawnerStack(event.getSpawner().getBlock(), 1, false);

        boolean placedByPlayer = stackedSpawner != null && stackedSpawner.isPlacedByPlayer();
        if (stackSettings.isMobAIDisabled() && (!SettingKey.SPAWNER_DISABLE_MOB_AI_ONLY_PLAYER_PLACED.get() || placedByPlayer))
            PersistentDataUtils.removeEntityAi(entity);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSecondarySpawnerSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (this.stackManager.isAreaDisabled(entity.getLocation()))
            return;

        if (EntityUtils.hasTrialSpawnerSpawnReason(entity)) {
            PersistentDataUtils.tagSpawnedFromTrialSpawner(entity);
            return;
        }

        if (EntityUtils.hasSpawnerSpawnReason(entity)) {
            if (PersistentDataUtils.isSpawnedFromSpawner(entity))
                return;

            // If we haven't already handled this mob as being from a spawner, handle it now
            PersistentDataUtils.tagSpawnedFromSpawner(entity);

            SpawnerStackSettings stackSettings = this.stackSettingManager.getSpawnerStackSettings(entity.getType());
            if (stackSettings.isMobAIDisabled())
                PersistentDataUtils.removeEntityAi(entity);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        // Withers can still target enitites due to custom boss AI, so prevent them from targeting when AI is disabled
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity))
            return;

        boolean disableAttacking = (event.getEntityType() == EntityType.WITHER && PersistentDataUtils.isAiDisabled((Wither) event.getEntity()))
                || (SettingKey.SPAWNER_DISABLE_ATTACKING.get()) && PersistentDataUtils.isSpawnedFromSpawner(event.getEntity());
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

        if (this.stackManager.isAreaDisabled(event.getEntity().getLocation()))
            return;

        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            if (!this.stackManager.isEntityStackingEnabled())
                return;

            StackedEntity stackedEntity = this.stackManager.getStackedEntity(livingEntity);
            if (stackedEntity != null) {
                this.stackManager.changeStackingThread(livingEntity.getUniqueId(), stackedEntity, event.getFrom().getWorld(), event.getTo().getWorld());
                stackedEntity.updateDisplay();
            }
        } else if (entity instanceof Item item) {
            if (!this.stackManager.isItemStackingEnabled())
                return;

            StackedItem stackedItem = this.stackManager.getStackedItem(item);
            if (stackedItem != null) {
                this.stackManager.changeStackingThread(item.getUniqueId(), stackedItem, event.getFrom().getWorld(), event.getTo().getWorld());
                stackedItem.updateDisplay();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Prevent guardians with disabled AI from spiking their attacker
        if (event.getEntity().getType() == EntityType.PLAYER
                && (event.getDamager() instanceof Guardian || event.getDamager() instanceof Slime)
                && PersistentDataUtils.isAiDisabled((LivingEntity) event.getDamager())) {
            event.setCancelled(true);
        }

        if (!(event.getEntity() instanceof LivingEntity entity) || event.getEntity().getType() == EntityType.PLAYER)
            return;

        if (!SettingKey.ENTITY_INSTANT_KILL_DISABLED_AI.get() || this.stackManager.isAreaDisabled(entity.getLocation()) || !PersistentDataUtils.isAiDisabled(entity))
            return;

        Entity damager = event.getDamager();
        if ((damager instanceof Projectile projectile && !(projectile.getShooter() instanceof Player)) || !(damager instanceof Player))
            return;

        AttributeInstance attributeInstance = entity.getAttribute(VersionUtils.MAX_HEALTH);;
        if (attributeInstance != null) {
            event.setDamage(attributeInstance.getValue() * 2);
        } else {
            event.setDamage(entity.getHealth() * 2);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || event.getEntity().getType() == EntityType.ARMOR_STAND || event.getEntity().getType() == EntityType.PLAYER)
            return;

        if (this.stackManager.isAreaDisabled(entity.getLocation()))
            return;

        if (!this.stackManager.isEntityStackingEnabled())
            return;

        StackedEntity stackedEntity = this.stackManager.getStackedEntity(entity);
        if (stackedEntity == null || stackedEntity.getStackSize() == 1)
            return;

        if (!SettingKey.ENTITY_SHARE_DAMAGE_CONDITIONS.get().contains(event.getCause().name()))
            return;

        double damage = event.getFinalDamage();

        List<LivingEntity> killedEntities = stackedEntity.getDataStorage().removeIf(internal -> {
            if (internal.getHealth() - damage <= 0) {
                return true; // Don't set the health below 0, as that will trigger the death event which we want to avoid
            } else {
                internal.setHealth(internal.getHealth() - damage);
                return false;
            }
        });

        // Only try dropping loot if something actually died
        if (!killedEntities.isEmpty()) {
            stackedEntity.dropPartialStackLoot(killedEntities);

            Player killer = entity.getKiller();
            if (killer != null && killedEntities.size() - 1 > 0 && SettingKey.MISC_STACK_STATISTICS.get())
                killer.incrementStatistic(Statistic.KILL_ENTITY, entity.getType(), killedEntities.size() - 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        Entity entity = event.getEntity();
        if (event instanceof EntityCombustByBlockEvent || event instanceof EntityCombustByEntityEvent || !(entity instanceof LivingEntity))
            return;

        // Don't allow mobs to naturally burn in the daylight if their AI is disabled
        if (PersistentDataUtils.isAiDisabled((LivingEntity) entity) && !SettingKey.SPAWNER_DISABLE_MOB_AI_OPTIONS_UNDEAD_BURN_IN_DAYLIGHT.get())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper))
            return;

        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        StackedEntity stackedEntity = stackManager.getStackedEntity(creeper);
        if (stackedEntity == null)
            return;

        if (stackedEntity.getStackSettings().getSettingValue(EntityStackSettings.CREEPER_EXPLODE_KILL_ENTIRE_STACK).getBoolean()) {
            stackManager.removeEntityStack(stackedEntity);
        } else {
            this.handleEntityDeath(null, creeper);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event instanceof AsyncEntityDeathEvent))
            this.handleEntityDeath(event, event.getEntity());
    }

    private void handleEntityDeath(EntityDeathEvent event, LivingEntity entity) {
        if (this.stackManager.isAreaDisabled(entity.getLocation()))
            return;

        if (!this.stackManager.isEntityStackingEnabled())
            return;

        StackedEntity stackedEntity = this.stackManager.getStackedEntity(entity);
        if (stackedEntity == null)
            return;

        int stackSize = stackedEntity.getStackSize();
        if (stackSize == 1) {
            this.stackManager.removeEntityStack(stackedEntity);
            return;
        }

        // Should we kill the entire stack at once?
        if (event != null && stackedEntity.isEntireStackKilledOnDeath()) {
            stackedEntity.killEntireStack(event);
            return;
        }

        Vector previousVelocity = entity.getVelocity().clone();
        Runnable task = () -> {
            // Should we kill multiple entities?
            if (SettingKey.ENTITY_MULTIKILL_ENABLED.get()) {
                int enchantmentMultiplier = 1;
                if (!SettingKey.ENTITY_MULTIKILL_PLAYER_ONLY.get() || entity.getKiller() != null) {
                    if (SettingKey.ENTITY_MULTIKILL_ENCHANTMENT_ENABLED.get()) {
                        Enchantment requiredEnchantment = Enchantment.getByKey(NamespacedKey.fromString(SettingKey.ENTITY_MULTIKILL_ENCHANTMENT_TYPE.get()));
                        if (requiredEnchantment == null) {
                            // Only decrease stack size by 1 and print a warning to the console
                            RoseStacker.getInstance().getLogger().warning("Invalid multikill enchantment type: " + SettingKey.ENTITY_MULTIKILL_ENCHANTMENT_TYPE.get());
                        } else if (event != null && event.getEntity().getKiller() != null) {
                            Player killer = event.getEntity().getKiller();
                            enchantmentMultiplier = killer.getInventory().getItemInMainHand().getEnchantmentLevel(requiredEnchantment);
                        }
                    }
                }

                MultikillBound lowerBound = this.stackManager.getLowerMultikillBound();
                MultikillBound upperBound = this.stackManager.getUpperMultikillBound();

                int lowerValue = lowerBound.getValue(stackSize);
                int upperValue = upperBound.getValue(stackSize);
                if (upperValue < lowerValue)
                    upperValue = lowerValue;

                int targetAmount = StackerUtils.randomInRange(lowerValue, upperValue);
                int killAmount = Math.max(1, targetAmount * enchantmentMultiplier);

                if (killAmount >= stackSize) {
                    stackedEntity.killEntireStack(event);
                } else {
                    stackedEntity.killPartialStack(event, killAmount);
                }
            } else {
                // Decrease stack size by 1
                stackedEntity.decreaseStackSize();
            }

            stackedEntity.getEntity().setVelocity(new Vector());

            if (SettingKey.ENTITY_KILL_TRANSFER_VELOCITY.get())
                stackedEntity.getEntity().setVelocity(previousVelocity);
        };

        if (SettingKey.ENTITY_KILL_DELAY_NEXT_SPAWN.get()) {
            ThreadUtils.runSync(task);
        } else {
            task.run();
        }

        if (SettingKey.ENTITY_KILL_TRANSFER_VELOCITY.get())
            entity.setVelocity(new Vector());

        if (!SettingKey.ENTITY_DISPLAY_CORPSE.get())
            entity.remove();
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
        if (this.stackManager.isAreaDisabled(event.getEntity().getLocation()))
            return;

        if (!this.stackManager.isEntityStackingEnabled())
            return;

        EntityStackSettings newStackSettings = this.stackSettingManager.getEntityStackSettings(event.getTransformedEntity().getType());
        boolean aiDisabled = PersistentDataUtils.isAiDisabled((LivingEntity) event.getEntity());
        boolean fromSpawner = PersistentDataUtils.isSpawnedFromSpawner(event.getEntity());
        if (event.getEntity() instanceof Slime) {
            if (aiDisabled)
                event.getTransformedEntities().stream().map(x -> (Slime) x).forEach(PersistentDataUtils::removeEntityAi);
            if (fromSpawner)
                event.getTransformedEntities().stream().map(x -> (Slime) x).forEach(newStackSettings::applySpawnerSpawnedProperties);
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity)
                || !(event.getTransformedEntity() instanceof LivingEntity transformedEntity)
                || event.getEntity().getType() == event.getTransformedEntity().getType()
                || !this.stackManager.isEntityStacked((LivingEntity) event.getEntity()))
            return;

        StackedEntity stackedEntity = this.stackManager.getStackedEntity((LivingEntity) event.getEntity());
        if (stackedEntity == null || stackedEntity.getStackSize() == 1)
            return;

        if (SettingKey.ENTITY_TRANSFORM_ENTIRE_STACK.get()) {
            EntityDataEntry serialized = EntityDataEntry.createFromEntityNBT(transformedEntity);
            event.setCancelled(true);

            // Handle mooshroom shearing
            EntityType entityType = event.getEntityType();
            if (entityType == VersionUtils.MOOSHROOM) {
                int mushroomsDropped = 5;
                EntityStackSettings mooshroomStackSettings = stackSettingManager.getEntityStackSettings(entityType);
                if (mooshroomStackSettings.getSettingValue(EntityStackSettings.MOOSHROOM_DROP_ADDITIONAL_MUSHROOMS_FOR_EACH_COW_IN_STACK).getBoolean())
                    mushroomsDropped += (stackedEntity.getStackSize() - 1) * stackedEntity.getStackSettings().getSettingValue(EntityStackSettings.MOOSHROOM_EXTRA_MUSHROOMS_PER_COW_IN_STACK).getInt();

                Material dropType = ((MushroomCow) event.getEntity()).getVariant() == Variant.BROWN ? Material.BROWN_MUSHROOM : Material.RED_MUSHROOM;
                this.stackManager.preStackItems(GuiUtil.getMaterialAmountAsItemStacks(dropType, mushroomsDropped), event.getEntity().getLocation());
            }

            event.getEntity().remove();
            ThreadUtils.runSync(() -> {
                this.stackManager.setEntityStackingTemporarilyDisabled(true);
                LivingEntity newEntity = serialized.createEntity(transformedEntity.getLocation(), true, transformedEntity.getType());
                if (aiDisabled)
                    PersistentDataUtils.removeEntityAi(newEntity);
                if (fromSpawner)
                    PersistentDataUtils.tagSpawnedFromSpawner(newEntity);
                StackedEntity newStack = this.stackManager.createEntityStack(newEntity, false);
                this.stackManager.setEntityStackingTemporarilyDisabled(false);
                if (newStack == null)
                    return;

                if (fromSpawner)
                    newStack.getStackSettings().applySpawnerSpawnedProperties(newEntity);

                stackedEntity.getDataStorage().forEach(entity -> {
                    if (aiDisabled)
                        PersistentDataUtils.removeEntityAi(entity);
                    newStack.increaseStackSize(entity, false);
                });
                newStack.updateDisplay();
            });
        } else {
            // Make sure disabled AI and from spawner properties get transferred
            if (aiDisabled)
                event.getTransformedEntities().stream().map(x -> (LivingEntity) x).forEach(PersistentDataUtils::removeEntityAi);
            if (fromSpawner)
                event.getTransformedEntities().stream().map(x -> (LivingEntity) x).forEach(newStackSettings::applySpawnerSpawnedProperties);

            if (event.getTransformReason() == TransformReason.LIGHTNING) { // Wait for lightning to disappear
                ThreadUtils.runSyncDelayed(stackedEntity::decreaseStackSize, 20);
            } else {
                ThreadUtils.runSync(stackedEntity::decreaseStackSize);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChickenLayEgg(EntityDropItemEvent event) {
        if (event.getEntityType() != EntityType.CHICKEN)
            return;

        Material eggMaterial = event.getItemDrop().getItemStack().getType();
        switch (eggMaterial.getKey().getKey()) {
            case "egg", "blue_egg", "brown_egg" -> {}
            default -> {
                return;
            }
        }

        if (this.stackManager.isAreaDisabled(event.getEntity().getLocation()))
            return;

        if (!this.stackManager.isEntityStackingEnabled())
            return;

        Chicken chickenEntity = (Chicken) event.getEntity();
        StackedEntity stackedEntity = this.stackManager.getStackedEntity(chickenEntity);
        if (stackedEntity == null || stackedEntity.getStackSize() == 1)
            return;

        EntityStackSettings chickenStackSettings = stackedEntity.getStackSettings();
        if (!chickenStackSettings.getSettingValue(EntityStackSettings.CHICKEN_MULTIPLY_EGG_DROPS_BY_STACK_SIZE).getBoolean())
            return;

        event.getItemDrop().remove();

        int maxAmount = chickenStackSettings.getSettingValue(EntityStackSettings.CHICKEN_MAX_EGG_STACK_SIZE).getInt();
        if (maxAmount == 0) // Allow disabling eggs for stacks
            return;

        int amount = stackedEntity.getStackSize();
        if (maxAmount > 0)
            amount = Math.min(amount, maxAmount);

        List<ItemStack> items = GuiUtil.getMaterialAmountAsItemStacks(eggMaterial, amount);
        this.stackManager.preStackItems(items, event.getEntity().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerShearSheep(PlayerShearEntityEvent event) {
        this.handleSheepShear(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockShearSheep(BlockShearEntityEvent event) {
        this.handleSheepShear(event.getEntity());
    }

    private void handleSheepShear(Entity entity) {
        if (entity.getType() != EntityType.SHEEP)
            return;

        if (this.stackManager.isAreaDisabled(entity.getLocation()))
            return;

        if (!this.stackManager.isEntityStackingEnabled())
            return;

        Sheep sheepEntity = (Sheep) entity;
        StackedEntity stackedEntity = this.stackManager.getStackedEntity(sheepEntity);
        if (stackedEntity == null)
            return;

        if (stackedEntity.getStackSize() == 1) {
            ThreadUtils.runAsync(() -> {
                stackedEntity.resetHasMoved();
                this.stackManager.tryStackEntity(stackedEntity);
            });
            return;
        }

        if (!stackedEntity.getStackSettings().getSettingValue(EntityStackSettings.SHEEP_SHEAR_ALL_SHEEP_IN_STACK).getBoolean()) {
            ThreadUtils.runSync(() -> {
                if (!stackedEntity.shouldStayStacked())
                    this.stackManager.splitEntityStack(stackedEntity);
            });
            return;
        }

        ShearedHandler shearedHandler = CompatibilityAdapter.getShearedHandler();
        List<ItemStack> drops = new ArrayList<>();
        this.stackManager.setEntityUnstackingTemporarilyDisabled(true);
        ThreadUtils.runAsync(() -> {
            try {
                stackedEntity.getDataStorage().forEachTransforming(internal -> {
                    Sheep sheep = (Sheep) internal;
                    if (!shearedHandler.isSheared(sheep) || this.stackManager.getEntityDataStorageType(sheep.getType()) == StackedEntityDataStorageType.SIMPLE) {
                        shearedHandler.setSheared(sheep, true);
                        drops.add(new ItemStack(ItemUtils.getWoolMaterial(sheep.getColor()), getWoolDropAmount()));
                        return true;
                    }
                    return false;
                });

                Location location = sheepEntity.getLocation();
                location.add(0, sheepEntity.getEyeHeight(), 0);
                ThreadUtils.runSync(() -> this.stackManager.preStackItems(drops, location, false));
            } finally {
                this.stackManager.setEntityUnstackingTemporarilyDisabled(false);
            }
        });
    }

    /**
     * @return a number between 1 and 3 inclusively
     */
    private static int getWoolDropAmount() {
        return (int) (Math.random() * 3) + 1;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSheepRegrowWool(SheepRegrowWoolEvent event) {
        if (this.stackManager.isAreaDisabled(event.getEntity().getLocation()))
            return;

        if (!this.stackManager.isEntityStackingEnabled())
            return;

        Sheep sheepEntity = event.getEntity();
        StackedEntity stackedEntity = this.stackManager.getStackedEntity(sheepEntity);
        if (stackedEntity == null || stackedEntity.getStackSize() == 1)
            return;

        double regrowPercentage = stackedEntity.getStackSettings().getSettingValue(EntityStackSettings.SHEEP_PERCENTAGE_OF_WOOL_TO_REGROW_PER_GRASS_EATEN).getDouble() / 100D;
        int regrowAmount = Math.max(1, (int) Math.round(stackedEntity.getStackSize() * regrowPercentage));

        ShearedHandler shearedHandler = CompatibilityAdapter.getShearedHandler();
        if (shearedHandler.isSheared(sheepEntity)) {
            shearedHandler.setSheared(sheepEntity, false);
            regrowAmount--;
        }

        if (regrowAmount <= 1)
            return;

        AtomicInteger regrowRemaining = new AtomicInteger(regrowAmount);
        ThreadUtils.runAsync(() -> stackedEntity.getDataStorage().forEachTransforming(internal -> {
            Sheep sheep = (Sheep) internal;
            if (shearedHandler.isSheared(sheep) && regrowRemaining.getAndDecrement() > 0) {
                shearedHandler.setSheared(sheep, false);
                return true;
            }
            return false;
        }));
    }

}
