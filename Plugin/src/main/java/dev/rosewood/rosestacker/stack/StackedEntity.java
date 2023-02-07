package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.event.AsyncEntityDeathEvent;
import dev.rosewood.rosestacker.event.EntityStackMultipleDeathEvent;
import dev.rosewood.rosestacker.hook.NPCsHook;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataEntry;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorage;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.utils.DataUtils;
import dev.rosewood.rosestacker.utils.EntityUtils;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class StackedEntity extends Stack<EntityStackSettings> implements Comparable<StackedEntity> {

    private LivingEntity entity;
    private StackedEntityDataStorage stackedEntityDataStorage;
    private int npcCheckCounter;

    private String displayName;
    private boolean displayNameVisible;

    private EntityStackSettings stackSettings;

    public StackedEntity(LivingEntity entity, StackedEntityDataStorage stackedEntityDataStorage) {
        this.entity = entity;
        this.stackedEntityDataStorage = stackedEntityDataStorage;
        this.npcCheckCounter = NPCsHook.anyEnabled() ? 5 : 0;

        this.displayName = null;
        this.displayNameVisible = false;

        if (this.entity != null) {
            this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getEntityStackSettings(this.entity);
            this.updateDisplay();
        }
    }

    public StackedEntity(LivingEntity entity) {
        this(entity, NMSAdapter.getHandler().createEntityDataStorage(entity, RoseStacker.getInstance().getManager(StackManager.class).getEntityDataStorageType()));
    }

    // We are going to check if this entity is an NPC multiple times, since MythicMobs annoyingly doesn't
    // actually register it as an NPC until a few ticks after it spawns
    public boolean checkNPC() {
        boolean npc = false;
        if (this.npcCheckCounter > 0) {
            if (NPCsHook.isNPC(this.entity))
                npc = true;
            this.npcCheckCounter--;
        }
        return npc;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public void updateEntity() {
        LivingEntity entity = (LivingEntity) Bukkit.getEntity(this.entity.getUniqueId());
        if (entity == null || entity == this.entity)
            return;

        this.entity = entity;
        this.stackedEntityDataStorage.updateEntity(entity);
        this.updateDisplay();
    }

    public void increaseStackSize(LivingEntity entity) {
        this.increaseStackSize(entity, true);
    }

    public void increaseStackSize(LivingEntity entity, boolean updateDisplay) {
        Runnable task = () -> {
            this.stackedEntityDataStorage.add(entity);
            if (updateDisplay)
                this.updateDisplay();
        };

        // EnderDragonChangePhaseEvents is called when reading the entity NBT data.
        // Since we usually do this async and the event isn't allowed to be async, Spigot throws a fit.
        // We switch over to a non-async thread specifically for ender dragons because of this.
        if (!Bukkit.isPrimaryThread() && entity instanceof EnderDragon) {
            ThreadUtils.runSync(task);
        } else {
            task.run();
        }
    }

    /**
     * Increases the stack size by a certain amount, clones the main entity
     *
     * @param updateDisplay Whether to update the entity's nametag or not
     */
    public void increaseStackSize(int amount, boolean updateDisplay) {
        this.stackedEntityDataStorage.addClones(amount);

        if (updateDisplay)
            this.updateDisplay();
    }

    public void increaseStackSize(StackedEntityDataStorage serializedStackedEntities) {
        this.stackedEntityDataStorage.addAll(serializedStackedEntities.getAll());
        this.updateDisplay();
    }

    /**
     * Unstacks the visible entity from the stack and moves the next in line to the front
     *
     * @return The new StackedEntity of size 1 that was just created
     */
    public StackedEntity decreaseStackSize() {
        if (this.stackedEntityDataStorage.isEmpty())
            return null;

        StackManager stackManager = RoseStacker.getInstance().getManager(StackManager.class);
        LivingEntity oldEntity = this.entity;

        stackManager.setEntityStackingTemporarilyDisabled(true);
        this.entity = NMSAdapter.getHandler().createEntityFromNBT(this.stackedEntityDataStorage.pop(), oldEntity.getLocation(), true, oldEntity.getType());
        stackManager.setEntityStackingTemporarilyDisabled(false);
        this.stackSettings.applyUnstackProperties(this.entity, oldEntity);
        stackManager.updateStackedEntityKey(oldEntity, this.entity);
        this.entity.setVelocity(this.entity.getVelocity().add(Vector.getRandom().multiply(0.01))); // Nudge the entity to unstack it from the old entity

        // Attempt to prevent adult entities from going into walls when a baby entity gets unstacked
        if (oldEntity instanceof Ageable ageable1 && this.entity instanceof Ageable ageable2 && !ageable1.isAdult() && ageable2.isAdult()) {
            Location centered = ageable1.getLocation();
            centered.setX(centered.getBlockX() + 0.5);
            centered.setZ(centered.getBlockZ() + 0.5);
            ageable2.teleport(centered);
        }

        this.stackedEntityDataStorage.updateEntity(this.entity);
        this.updateDisplay();
        PersistentDataUtils.applyDisabledAi(this.entity);

        DataUtils.clearStackedEntityData(oldEntity);
        return new StackedEntity(oldEntity, NMSAdapter.getHandler().createEntityDataStorage(oldEntity, RoseStacker.getInstance().getManager(StackManager.class).getEntityDataStorageType()));
    }

    /**
     * @deprecated Use {@link #getDataStorage()} instead
     */
    @Deprecated(forRemoval = true)
    public StackedEntityDataStorage getStackedEntityNBT() {
        return this.getDataStorage();
    }

    public StackedEntityDataStorage getDataStorage() {
        return this.stackedEntityDataStorage;
    }

    /**
     * Warning! This method should not be used outside this plugin.
     * This method overwrites the data storage and NOTHING ELSE.
     * If the stack size were to change, there would be no way of detecting it, you have been warned!
     *
     * @param stackedEntityDataStorage The data storage to overwrite with
     * @deprecated Use {@link #setDataStorage(StackedEntityDataStorage)} instead
     */
    @Deprecated(forRemoval = true)
    public void setStackedEntityNBT(StackedEntityDataStorage stackedEntityDataStorage) {
        this.setDataStorage(stackedEntityDataStorage);
    }

    /**
     * Warning! This method should not be used outside this plugin.
     * This method overwrites the data storage and NOTHING ELSE.
     * If the stack size were to change, there would be no way of detecting it, you have been warned!
     *
     * @param stackedEntityDataStorage The data storage to overwrite with
     */
    public void setDataStorage(StackedEntityDataStorage stackedEntityDataStorage) {
        stackedEntityDataStorage.updateEntity(this.entity);
        this.stackedEntityDataStorage = stackedEntityDataStorage;
        this.updateDisplay();
    }

    /**
     * Drops all loot and experience for all internally-stacked entities.
     * Does not include loot for the current entity.
     *
     * @param existingLoot The loot from this.entity, nullable
     * @param droppedExp The exp dropped from this.entity
     */
    public void dropStackLoot(Collection<ItemStack> existingLoot, int droppedExp) {
        ThreadUtils.runAsync(() -> {
            List<LivingEntity> internalEntities = new ArrayList<>();

            int threshold = Setting.ENTITY_LOOT_APPROXIMATION_THRESHOLD.getInt();
            int approximationAmount = Setting.ENTITY_LOOT_APPROXIMATION_AMOUNT.getInt();
            if (Setting.ENTITY_LOOT_APPROXIMATION_ENABLED.getBoolean() && this.getStackSize() > threshold) {
                this.stackedEntityDataStorage.forEachCapped(approximationAmount - 1, internalEntities::add);
                this.dropPartialStackLoot(internalEntities, this.getStackSize() / (double) approximationAmount, existingLoot, droppedExp);
            } else {
                this.stackedEntityDataStorage.forEach(internalEntities::add);
                this.dropPartialStackLoot(internalEntities, 1, existingLoot, droppedExp);
            }
        });
    }

    /**
     * Drops loot for entities that are part of the stack.
     * Does not include loot for the current entity (except for nether stars for withers).
     *
     * @param internalEntities The entities which should be part of this stack
     * @param multiplier The multiplier for the loot drops
     * @param existingLoot The loot from this.entity, nullable
     * @param droppedExp The exp dropped from this.entity
     */
    public void dropPartialStackLoot(List<LivingEntity> internalEntities, double multiplier, Collection<ItemStack> existingLoot, int droppedExp) {
        // Cache the current entity just in case it somehow changes while we are processing the loot
        LivingEntity thisEntity = this.entity;
        Collection<ItemStack> loot = new ArrayList<>();

        // The stack loot can either be processed synchronously or asynchronously depending on a setting
        // It should always be processed async unless errors are caused by other plugins
        boolean async = Setting.ENTITY_DEATH_EVENT_RUN_ASYNC.getBoolean();
        Runnable mainTask = () -> {
            boolean callEvents = !RoseStackerAPI.getInstance().isEntityStackMultipleDeathEventCalled();
            int totalExp = 0;
            NMSHandler nmsHandler = NMSAdapter.getHandler();
            boolean isAnimal = thisEntity instanceof Animals;
            boolean isWither = thisEntity.getType() == EntityType.WITHER;
            boolean killedByWither = thisEntity.getLastDamageCause() instanceof EntityDamageByEntityEvent
                    && (((EntityDamageByEntityEvent) thisEntity.getLastDamageCause()).getDamager().getType() == EntityType.WITHER
                    || ((EntityDamageByEntityEvent) thisEntity.getLastDamageCause()).getDamager().getType() == EntityType.WITHER_SKULL);
            boolean isSlime = thisEntity instanceof Slime;
            boolean isAccurateSlime = isSlime && this.stackSettings.getSettingValue(EntityStackSettings.SLIME_ACCURATE_DROPS_WITH_KILL_ENTIRE_STACK_ON_DEATH).getBoolean();

            Map<LivingEntity, EntityStackMultipleDeathEvent.EntityDrops> entityDrops = new LinkedHashMap<>(internalEntities.size());
            if (callEvents) {
                if (existingLoot != null)
                    loot.addAll(existingLoot);
                totalExp += droppedExp;
            } else {
                entityDrops.put(thisEntity, new EntityStackMultipleDeathEvent.EntityDrops(new ArrayList<>(existingLoot), droppedExp));
            }

            for (LivingEntity entity : internalEntities) {
                // Propagate fire ticks and last damage cause
                entity.setFireTicks(thisEntity.getFireTicks());
                entity.setLastDamageCause(thisEntity.getLastDamageCause());
                nmsHandler.setLastHurtBy(entity, thisEntity.getKiller());

                int iterations = 1;
                if (isSlime) {
                    Slime slime = (Slime) entity;
                    if (isAccurateSlime) {
                        int totalSlimes = 1;
                        int size = slime.getSize();
                        while (size > 1) {
                            size /= 2;
                            int currentSlimes = totalSlimes;
                            totalSlimes = StackerUtils.randomInRange(currentSlimes * 2, currentSlimes * 4);
                        }
                        iterations = totalSlimes;
                    }
                    slime.setSize(slime.getType() == EntityType.SLIME ? 1 : 2); // Slimes require size 1 to drop items, magma cubes require > size 1
                }

                boolean isBaby = isAnimal && !((Animals) entity).isAdult();
                int desiredExp = isBaby ? 0 : droppedExp;
                for (int i = 0; i < iterations; i++) {
                    Collection<ItemStack> entityLoot = isBaby ? List.of() : EntityUtils.getEntityLoot(entity, thisEntity.getKiller(), thisEntity.getLocation());
                    if (callEvents) {
                        EntityDeathEvent deathEvent = new AsyncEntityDeathEvent(entity, new ArrayList<>(entityLoot), desiredExp);
                        Bukkit.getPluginManager().callEvent(deathEvent);
                        totalExp += deathEvent.getDroppedExp();
                        loot.addAll(deathEvent.getDrops());
                        // Withers always drop nether stars on death, however this isn't in the actual wither loot table for some reason
                        if (isWither)
                            loot.add(new ItemStack(Material.NETHER_STAR));
                        if (killedByWither)
                            loot.add(new ItemStack(Material.WITHER_ROSE));
                    } else {
                        List<ItemStack> entityLootList = new ArrayList<>(entityLoot);
                        if (isWither)
                            entityLootList.add(new ItemStack(Material.NETHER_STAR));
                        if (killedByWither)
                            entityLootList.add(new ItemStack(Material.WITHER_ROSE));

                        if (entityDrops.containsKey(entity)) {
                            EntityStackMultipleDeathEvent.EntityDrops drops = entityDrops.get(entity);
                            drops.setExperience(drops.getExperience() + desiredExp);
                            drops.getDrops().addAll(entityLootList);
                        } else {
                            entityDrops.put(entity, new EntityStackMultipleDeathEvent.EntityDrops(entityLootList, desiredExp));
                        }
                    }
                }

                // Prevent magma cubes from splitting
                if (isSlime && entity.getType() == EntityType.MAGMA_CUBE)
                    ((MagmaCube) entity).setSize(1);
            }

            // Call the EntityStackMultipleDeathEvent if enabled
            if (!callEvents) {
                EntityStackMultipleDeathEvent event = new EntityStackMultipleDeathEvent(this, entityDrops);
                Bukkit.getPluginManager().callEvent(event);
                for (EntityStackMultipleDeathEvent.EntityDrops value : event.getEntityDrops().values()) {
                    loot.addAll(value.getDrops());
                    totalExp += value.getExperience();
                }
            }

            // Multiply loot
            Collection<ItemStack> finalEntityLoot;
            if (multiplier != 1) {
                finalEntityLoot = ItemUtils.getMultipliedItemStacks(loot, multiplier);
            } else {
                finalEntityLoot = loot;
            }

            int finalTotalExp = (int) Math.min(Math.round(totalExp * multiplier), Integer.MAX_VALUE);
            Runnable finishTask = () -> {
                RoseStacker.getInstance().getManager(StackManager.class).preStackItems(finalEntityLoot, thisEntity.getLocation());
                if (Setting.ENTITY_DROP_ACCURATE_EXP.getBoolean() && finalTotalExp > 0)
                    StackerUtils.dropExperience(thisEntity.getLocation(), finalTotalExp, finalTotalExp, finalTotalExp / 2);
            };

            if (!Bukkit.isPrimaryThread()) {
                ThreadUtils.runSync(finishTask);
            } else {
                finishTask.run();
            }
        };

        if (async && Bukkit.isPrimaryThread()) {
            ThreadUtils.runAsync(mainTask);
        } else if (!async && !Bukkit.isPrimaryThread()) {
            ThreadUtils.runSync(mainTask);
        } else {
            mainTask.run();
        }
    }

    /**
     * @return true if this entity should stay stacked, otherwise false
     */
    public boolean shouldStayStacked() {
        if (this.entity == null || this.stackedEntityDataStorage.isEmpty())
            return true;

        // Ender dragons call an EnderDragonChangePhaseEvent upon entity construction
        // We want to be able to do this check async, we just won't let ender dragons unstack without dying
        if (this.entity instanceof EnderDragon)
            return true;

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        LivingEntity entity = nmsHandler.createEntityFromNBT(this.stackedEntityDataStorage.peek(), this.entity.getLocation(), false, this.entity.getType());
        StackedEntity stackedEntity = new StackedEntity(entity, nmsHandler.createEntityDataStorage(entity, RoseStacker.getInstance().getManager(StackManager.class).getEntityDataStorageType()));
        return this.stackSettings.testCanStackWith(this, stackedEntity, true);
    }

    @Override
    public int getStackSize() {
        return this.stackedEntityDataStorage.size() + 1;
    }

    @Override
    public Location getLocation() {
        return this.entity.getLocation();
    }

    public String getDisplayName() {
        if (this.displayName != null)
            return this.displayName;

        if (!Setting.ENTITY_DISPLAY_TAGS.getBoolean() || this.stackSettings == null || this.entity == null) {
            this.displayNameVisible = false;
            return this.displayName = this.entity == null ? null : this.entity.getCustomName();
        }

        if (this.entity.isDead()) {
            this.displayNameVisible = false;
            return null;
        }

        String customName = this.entity.getCustomName();
        if (this.getStackSize() > 1 || Setting.ENTITY_DISPLAY_TAGS_SINGLE.getBoolean()) {
            String displayString;
            if (customName != null && Setting.ENTITY_DISPLAY_TAGS_CUSTOM_NAME.getBoolean()) {
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("entity-stack-display-custom-name", StringPlaceholders.builder("amount", StackerUtils.formatNumber(this.getStackSize()))
                        .addPlaceholder("name", customName).build());
            } else {
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("entity-stack-display", StringPlaceholders.builder("amount", StackerUtils.formatNumber(this.getStackSize()))
                        .addPlaceholder("name", this.stackSettings.getDisplayName()).build());
            }

            this.displayNameVisible = !Setting.ENTITY_DISPLAY_TAGS_HOVER.getBoolean();
            return this.displayName = displayString;
        } else if (this.getStackSize() == 1 && customName != null) {
            this.displayNameVisible = false;
            return this.displayName = this.entity.getCustomName();
        }

        this.displayNameVisible = false;
        return null;
    }

    public boolean isDisplayNameVisible() {
        return this.displayNameVisible;
    }

    @Override
    public void updateDisplay() {
        this.displayName = null;
        String displayName = this.getDisplayName();
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        for (Player player : this.getPlayersInVisibleRange())
            nmsHandler.updateEntityNameTagForPlayer(player, this.entity, displayName, this.displayNameVisible);
    }

    @Override
    public EntityStackSettings getStackSettings() {
        return this.stackSettings;
    }

    /**
     * Gets the StackedEntity that two stacks should stack into
     *
     * @param stack2 the second StackedEntity
     * @return a positive int if this stack should be preferred, or a negative int if the other should be preferred
     */
    @Override
    public int compareTo(StackedEntity stack2) {
        Entity entity1 = this.getEntity();
        Entity entity2 = stack2.getEntity();

        if (this == stack2)
            return 0;

        if (Setting.ENTITY_STACK_FLYING_DOWNWARDS.getBoolean() && this.stackSettings.getEntityTypeData().flyingMob())
            return entity1.getLocation().getY() < entity2.getLocation().getY() ? 3 : -3;

        if (this.getStackSize() == stack2.getStackSize())
            return entity1.getTicksLived() > entity2.getTicksLived() ? 2 : -2;

        return this.getStackSize() > stack2.getStackSize() ? 1 : -1;
    }

    /**
     * Checks if the entity stack should die at once
     *
     * @param overrideKiller The player that is causing the entity to die, nullable
     * @return true if the whole stack should die, otherwise false
     */
    public boolean isEntireStackKilledOnDeath(@Nullable Player overrideKiller) {
        EntityDamageEvent lastDamageCause = this.entity.getLastDamageCause();
        if (overrideKiller == null)
            overrideKiller = this.entity.getKiller();

        return this.stackSettings.shouldKillEntireStackOnDeath()
                || (Setting.SPAWNER_DISABLE_MOB_AI_OPTIONS_KILL_ENTIRE_STACK_ON_DEATH.getBoolean() && PersistentDataUtils.isAiDisabled(this.entity))
                || (lastDamageCause != null && Setting.ENTITY_KILL_ENTIRE_STACK_CONDITIONS.getStringList().stream().anyMatch(x -> x.equalsIgnoreCase(lastDamageCause.getCause().name())))
                || (overrideKiller != null && overrideKiller.hasPermission("rosestacker.killentirestack"));
    }

    /**
     * @return true if the whole stack should die at once, otherwise false
     */
    public boolean isEntireStackKilledOnDeath() {
        return this.isEntireStackKilledOnDeath(null);
    }

    /**
     * Kills the entire entity stack and drops its loot
     *
     * @param event The event that caused the entity to die, nullable
     */
    public void killEntireStack(@Nullable EntityDeathEvent event) {
        int experience = event != null ? event.getDroppedExp() : EntityUtils.getApproximateExperience(this.stackSettings.getEntityType().getEntityClass());
        if (Setting.ENTITY_DROP_ACCURATE_ITEMS.getBoolean()) {
            if (this.entity instanceof Slime)
                ((Slime) this.entity).setSize(1);

            if (event == null) {
                this.dropStackLoot(new ArrayList<>(), experience);
            } else {
                this.dropStackLoot(new ArrayList<>(event.getDrops()), experience);
                event.getDrops().clear();
            }
        } else if (Setting.ENTITY_DROP_ACCURATE_EXP.getBoolean()) {
            if (event == null) {
                this.entity.getWorld().spawn(this.entity.getLocation(), ExperienceOrb.class, x -> x.setExperience(experience));
            } else {
                event.setDroppedExp(experience * this.getStackSize());
            }
        }

        Player killer = this.entity.getKiller();
        if (killer != null && this.getStackSize() - 1 > 0)
            killer.incrementStatistic(Statistic.KILL_ENTITY, this.entity.getType(), this.getStackSize() - 1);

        RoseStacker.getInstance().getManager(StackManager.class).removeEntityStack(this);

        if (!this.entity.isDead())
            this.entity.remove();
    }

    /**
     * Kills the entire entity stack and drops its loot
     */
    public void killEntireStack() {
        this.killEntireStack(null);
    }

    public void killPartialStack(@Nullable EntityDeathEvent event, int amount) {
        if (amount == 1) {
            if (this.getStackSize() == 1) {
                RoseStacker.getInstance().getManager(StackManager.class).removeEntityStack(this);
            } else {
                this.decreaseStackSize();
            }
            return;
        }

        List<StackedEntityDataEntry<?>> removed = this.stackedEntityDataStorage.pop(amount - 1);
        List<LivingEntity> entities = new ArrayList<>(removed.size());
        for (StackedEntityDataEntry<?> entry : removed)
            entities.add(NMSAdapter.getHandler().createEntityFromNBT(entry, this.entity.getLocation(), false, this.entity.getType()));

        int experience = event != null ? event.getDroppedExp() : EntityUtils.getApproximateExperience(this.stackSettings.getEntityType().getEntityClass());
        if (Setting.ENTITY_DROP_ACCURATE_ITEMS.getBoolean()) {
            if (this.entity instanceof Slime)
                ((Slime) this.entity).setSize(1);

            if (event == null) {
                this.dropPartialStackLoot(entities, 1, new ArrayList<>(), experience);
            } else {
                this.dropPartialStackLoot(entities, 1, new ArrayList<>(event.getDrops()), experience);
                event.getDrops().clear();
            }
        } else if (Setting.ENTITY_DROP_ACCURATE_EXP.getBoolean()) {
            if (event == null) {
                this.entity.getWorld().spawn(this.entity.getLocation(), ExperienceOrb.class, x -> x.setExperience(experience));
            } else {
                event.setDroppedExp(experience * this.getStackSize());
            }
        }

        this.decreaseStackSize();

        Player killer = this.entity.getKiller();
        if (killer != null && this.getStackSize() - 1 > 0)
            killer.incrementStatistic(Statistic.KILL_ENTITY, this.entity.getType(), this.getStackSize() - 1);
    }

}
