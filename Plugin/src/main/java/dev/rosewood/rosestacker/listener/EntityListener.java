package dev.rosewood.rosestacker.listener;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedItem;
import dev.rosewood.rosestacker.stack.settings.entity.ChickenStackSettings;
import dev.rosewood.rosestacker.stack.settings.entity.SheepStackSettings;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.MushroomCow.Variant;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.EntityTransformEvent.TransformReason;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class EntityListener implements Listener {

    private RosePlugin rosePlugin;

    public EntityListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getEntity().getWorld()))
            return;

        if (!stackManager.isItemStackingEnabled() || stackManager.isEntityStackingTemporarilyDisabled())
            return;

        if (event.getEntity() instanceof Item)
            stackManager.createItemStack((Item) event.getEntity(), true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getEntity().getWorld()))
            return;

        if (!stackManager.isEntityStackingEnabled() || stackManager.isEntityStackingTemporarilyDisabled())
            return;

        stackManager.createEntityStack(event.getEntity(), true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTeleport(EntityPortalEvent event) {
        if (event.getTo() == null || event.getFrom().getWorld() == event.getTo().getWorld())
            return;

        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getEntity().getWorld()))
            return;

        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            if (!stackManager.isEntityStackingEnabled())
                return;

            LivingEntity livingEntity = (LivingEntity) entity;
            StackedEntity stackedEntity = stackManager.getStackedEntity(livingEntity);
            if (stackedEntity != null) {
                Bukkit.getScheduler().runTask(this.rosePlugin, () -> {
                    stackManager.changeStackingThread(livingEntity.getUniqueId(), stackedEntity, event.getFrom().getWorld(), event.getTo().getWorld());
                    stackedEntity.updateDisplay();
                });
            }
        } else if (entity instanceof Item) {
            if (!stackManager.isItemStackingEnabled())
                return;

            Item item = (Item) entity;
            StackedItem stackedItem = stackManager.getStackedItem(item);
            if (stackedItem != null)
                Bukkit.getScheduler().runTask(this.rosePlugin, () -> stackManager.changeStackingThread(item.getUniqueId(), stackedItem, event.getFrom().getWorld(), event.getTo().getWorld()));
        }
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
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(entity.getWorld()))
            return;

        if (!stackManager.isEntityStackingEnabled())
            return;

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

        if (Setting.ENTITY_KILL_TRANSFER_VELOCITY.getBoolean()) {
            stackedEntity.getEntity().setVelocity(entity.getVelocity());
            entity.setVelocity(new Vector());
        }
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
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getEntity().getWorld()))
            return;

        if (!stackManager.isEntityStackingEnabled())
            return;

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
            NMSHandler nmsHandler = NMSAdapter.getHandler();
            byte[] serialized = nmsHandler.getEntityAsNBT(transformedEntity, Setting.ENTITY_SAVE_ATTRIBUTES.getBoolean());
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

                stackManager.preStackItems(GuiUtil.getMaterialAmountAsItemStacks(dropType, mushroomsDropped), event.getEntity().getLocation());
            }

            event.getEntity().remove();
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.rosePlugin, () -> {
                stackManager.setEntityStackingTemporarilyDisabled(true);
                StackedEntity newStack = stackManager.createEntityStack(nmsHandler.spawnEntityFromNBT(serialized, transformedEntity.getLocation()), false);
                stackManager.setEntityStackingTemporarilyDisabled(false);
                if (newStack == null)
                    return;

                for (byte[] serializedEntity : stackedEntity.getStackedEntityNBT())
                    newStack.increaseStackSize(nmsHandler.getNBTAsEntity(transformedEntity.getType(), transformedEntity.getLocation(), serializedEntity));
            });
        } else {
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

        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getEntity().getWorld()))
            return;

        if (!stackManager.isEntityStackingEnabled())
            return;

        Chicken chickenEntity = (Chicken) event.getEntity();
        StackedEntity stackedEntity = stackManager.getStackedEntity(chickenEntity);
        if (stackedEntity == null || stackedEntity.getStackSize() == 1)
            return;

        ChickenStackSettings chickenStackSettings = (ChickenStackSettings) stackedEntity.getStackSettings();
        if (!chickenStackSettings.shouldMultiplyEggDropsByStackSize())
            return;

        event.getItemDrop().remove();
        List<ItemStack> items = GuiUtil.getMaterialAmountAsItemStacks(Material.EGG, stackedEntity.getStackSize());
        stackManager.preStackItems(items, event.getEntity().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerShearSheep(PlayerShearEntityEvent event) {
        ItemStack tool = event.getItem();
        if (!handleSheepShear(this.rosePlugin, tool, event.getEntity()))
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

        StackerUtils.damageTool(shears);

        ItemStack baseSheepWool = new ItemStack(StackerUtils.getWoolMaterial(sheepEntity.getColor()), getWoolDropAmount());
        sheepEntity.setSheared(true);
        List<ItemStack> drops = new ArrayList<>(Collections.singletonList(baseSheepWool));

        Bukkit.getScheduler().runTaskAsynchronously(RoseStacker.getInstance(), () -> {
            List<Sheep> sheepList = deconstructStackedSheep(stackedEntity);
            for (Sheep sheep : sheepList) {
                if (!sheep.isSheared()) {
                    ItemStack sheepWool = new ItemStack(StackerUtils.getWoolMaterial(sheep.getColor()), getWoolDropAmount());
                    sheep.setSheared(true);
                    drops.add(sheepWool);
                }
            }
            reconstructStackedSheep(stackedEntity, sheepList);

            Bukkit.getScheduler().runTask(RoseStacker.getInstance(), () -> stackManager.preStackItems(drops, sheepEntity.getLocation()));
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
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getEntity().getWorld()))
            return;

        if (!stackManager.isEntityStackingEnabled())
            return;

        Sheep sheepEntity = event.getEntity();
        StackedEntity stackedEntity = stackManager.getStackedEntity(sheepEntity);
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

            List<Sheep> sheepList = deconstructStackedSheep(stackedEntity);
            for (Sheep sheep : sheepList) {
                if (sheep.isSheared()) {
                    sheep.setSheared(false);
                    remaining--;
                    if (remaining <= 0)
                        break;
                }
            }
            reconstructStackedSheep(stackedEntity, sheepList);
        });
    }

    private static List<Sheep> deconstructStackedSheep(StackedEntity stackedEntity) {
        List<byte[]> nbtList = stackedEntity.getStackedEntityNBT();
        List<Sheep> sheepList = new ArrayList<>(nbtList.size());

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        for (byte[] nbt : nbtList)
            sheepList.add((Sheep) nmsHandler.getNBTAsEntity(EntityType.SHEEP, stackedEntity.getLocation(), nbt));

        return sheepList;
    }

    private static void reconstructStackedSheep(StackedEntity stackedEntity, List<Sheep> sheepList) {
        List<byte[]> nbtList = Collections.synchronizedList(new LinkedList<>());

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        for (Sheep sheep : sheepList)
            nbtList.add(nmsHandler.getEntityAsNBT(sheep, Setting.ENTITY_SAVE_ATTRIBUTES.getBoolean()));

        stackedEntity.setStackedEntityNBT(nbtList);
    }

}
