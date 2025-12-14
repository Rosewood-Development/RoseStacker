package dev.rosewood.rosestacker.stack;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.rosewood.rosegarden.utils.EntitySpawnUtil;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.event.EntityStackMultipleDeathEvent;
import dev.rosewood.rosestacker.event.EntityStackMultipleDeathEvent.EntityDrops;
import dev.rosewood.rosestacker.hook.NPCsHook;
import dev.rosewood.rosestacker.hook.SpawnerFlagPersistenceHook;
import dev.rosewood.rosestacker.hook.WorldGuardHook;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.storage.EntityDataEntry;
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
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Frog;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class StackedEntity extends Stack<EntityStackSettings> implements Comparable<StackedEntity> {

    private LivingEntity entity;
    private StackedEntityDataStorage stackedEntityDataStorage;
    private int npcCheckCounter;
    private boolean npc;

    private String displayName;
    private boolean displayNameVisible;
    private double x, y, z;

    private EntityStackSettings stackSettings;

    public StackedEntity(LivingEntity entity, StackedEntityDataStorage stackedEntityDataStorage, boolean updateDisplay) {
        this.entity = entity;
        this.stackedEntityDataStorage = stackedEntityDataStorage;
        this.npcCheckCounter = NPCsHook.anyEnabled() ? 5 : 0;

        this.displayName = null;
        this.displayNameVisible = false;

        if (this.entity != null) {
            this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getEntityStackSettings(this.entity);
            if (updateDisplay)
                this.updateDisplay();
        }
    }

    public StackedEntity(LivingEntity entity, StackedEntityDataStorage stackedEntityDataStorage) {
        this(entity, stackedEntityDataStorage, true);
    }

    public StackedEntity(LivingEntity entity) {
        this(entity, NMSAdapter.getHandler().createEntityDataStorage(entity, RoseStacker.getInstance().getManager(StackManager.class).getEntityDataStorageType(entity.getType())), true);
    }

    // We are going to check if this entity is an NPC multiple times, since MythicMobs annoyingly doesn't
    // actually register it as an NPC until a few ticks after it spawns
    public boolean checkNPC() {
        if (!this.npc && this.npcCheckCounter > 0) {
            if (NPCsHook.isNPC(this.entity))
                this.npc = true;
            this.npcCheckCounter--;
        }
        return this.npc;
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
        this.resetHasMoved();
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
        this.stackedEntityDataStorage.addAll(serializedStackedEntities);
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
        EntityCacheManager entityCacheManager = RoseStacker.getInstance().getManager(EntityCacheManager.class);
        LivingEntity oldEntity = this.entity;

        stackManager.setEntityStackingTemporarilyDisabled(true);
        this.entity = this.stackedEntityDataStorage.pop().createEntity(oldEntity.getLocation(), true, oldEntity.getType());
        stackManager.setEntityStackingTemporarilyDisabled(false);
        this.stackSettings.applyUnstackProperties(this.entity, oldEntity);
        stackManager.updateStackedEntityKey(oldEntity, this);
        entityCacheManager.preCacheEntity(this.entity);
        this.entity.setVelocity(this.entity.getVelocity().add(Vector.getRandom().subtract(new Vector(0.5, 0.5, 0.5)).multiply(0.01))); // Nudge the entity to unstack it from the old entity

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
        return new StackedEntity(oldEntity, NMSAdapter.getHandler().createEntityDataStorage(oldEntity, RoseStacker.getInstance().getManager(StackManager.class).getEntityDataStorageType(oldEntity.getType())));
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
        this.dropPartialStackLoot(this.getStackSize(), existingLoot, droppedExp);
    }

    /**
     * Drops loot for entities that are part of the stack.
     * Does not include loot for the current entity.
     *
     * @param existingLoot The loot from this.entity, nullable
     * @param droppedExp The exp dropped from this.entity
     */
    public void dropPartialStackLoot(int count, Collection<ItemStack> existingLoot, int droppedExp) {
        int originalStackSize = this.getStackSize();
        this.calculateAndDropPartialStackLoot(() -> this.calculateEntityDrops(count - 1, false, droppedExp, null, null, new EntityDrops(new ArrayList<>(existingLoot), droppedExp), originalStackSize, count));
    }

    /**
     * @deprecated this should be static, it doesn't really use the stacked entity state at all
     */
    @Deprecated
    public void dropPartialStackLoot(Collection<LivingEntity> internalEntities) {
        int killedEntities = internalEntities.size();
        int originalStackSize = this.getStackSize() + killedEntities;
        this.calculateAndDropPartialStackLoot(() -> {
            List<EntityDataEntry> entityDataEntries = internalEntities.stream().map(EntityDataEntry::createFromEntity).toList();
            return this.calculateEntityDrops(entityDataEntries, EntityUtils.getApproximateExperience(this.entity), null, null, null, originalStackSize, killedEntities);
        });
    }

    /**
     * @deprecated this should be static, it doesn't really use the stacked entity state at all
     */
    @Deprecated
    public void dropPartialStackLoot(Collection<EntityDataEntry> internalEntityData, Collection<ItemStack> existingLoot, int droppedExp) {
        LivingEntity thisEntity = this.entity;
        int killedEntities = internalEntityData.size() + 1;
        int originalStackSize = this.getStackSize() + internalEntityData.size() + 1;
        this.calculateAndDropPartialStackLoot(() ->
                this.calculateEntityDrops(internalEntityData, droppedExp, null, thisEntity, new EntityDrops(new ArrayList<>(existingLoot), droppedExp), originalStackSize, killedEntities));
    }

    private void calculateAndDropPartialStackLoot(Supplier<EntityDrops> calculator) {
        // The stack loot can either be processed synchronously or asynchronously depending on a setting
        // It should always be processed async unless errors are caused by other plugins
        Player killer = this.entity.getKiller();
        boolean async = SettingKey.ENTITY_DEATH_EVENT_RUN_ASYNC.get();
        Runnable mainTask = () -> {
            EntityDrops drops = calculator.get();

            Runnable finishTask = () -> {
                Location location = this.entity.getLocation();
                RoseStacker.getInstance().getManager(StackManager.class).preStackItems(drops.getDrops(), location, false);
                int finalDroppedExp = drops.getExperience();
                if (SettingKey.ENTITY_DROP_ACCURATE_EXP.get() && finalDroppedExp > 0 && WorldGuardHook.testCanDropExperience(killer, location))
                    StackerUtils.dropExperience(location, finalDroppedExp, finalDroppedExp, finalDroppedExp / 2);
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
     * Calculates the entity drops. May be called async or sync.
     *
     * @param count The number of entities to drop items for
     * @param includeMainEntity Whether to include the main entity in the calculation
     * @param entityExpValue The exp value of the entity
     * @return The calculated entity drops
     */
    @ApiStatus.Internal
    public EntityDrops calculateEntityDrops(int count, boolean includeMainEntity, int entityExpValue) {
        return this.calculateEntityDrops(count, includeMainEntity, entityExpValue, null);
    }

    /**
     * Calculates the entity drops. May be called async or sync.
     *
     * @param count The number of entities to drop items for
     * @param includeMainEntity Whether to include the main entity in the calculation
     * @param entityExpValue The exp value of the entity
     * @param lootingModifier The looting modifier, nullable
     * @return The calculated entity drops
     */
    @ApiStatus.Internal
    public EntityDrops calculateEntityDrops(int count, boolean includeMainEntity, int entityExpValue, Integer lootingModifier) {
        return this.calculateEntityDrops(count, includeMainEntity, entityExpValue, lootingModifier, null, null, null, null);
    }

    /**
     * Calculates the entity drops. May be called async or sync.
     *
     * @param internalEntities The entities to calculate drops for, unapproximated
     * @param entityExpValue The exp value of the entity
     * @param lootingModifier The looting modifier, nullable, defaults to the killer's looting value
     * @param mainEntity The main entity to use for loot calculations, primarily used to copy entity properties such as the killer, nullable, defaults to the stack entity
     * @param mainEntityDrops The main entity drops to include in the loot calculations, nullable
     * @return The calculated entity drops
     */
    @ApiStatus.Internal
    public EntityDrops calculateEntityDrops(Collection<EntityDataEntry> internalEntities, int entityExpValue, Integer lootingModifier,
                                            LivingEntity mainEntity, EntityDrops mainEntityDrops,
                                            Integer originalStackSize, Integer entityKillCount) {
        // Cache the current entity just in case it somehow changes while we are processing the loot
        if (mainEntity == null)
            mainEntity = this.entity;

        int count = internalEntities.size();
        Location location = this.entity.getLocation();
        EntityType type = this.entity.getType();
        List<LivingEntity> finalEntities = new ArrayList<>();
        double multiplier = 1;
        int threshold = SettingKey.ENTITY_LOOT_APPROXIMATION_THRESHOLD.get();
        int approximationAmount = SettingKey.ENTITY_LOOT_APPROXIMATION_AMOUNT.get();
        if (SettingKey.ENTITY_LOOT_APPROXIMATION_ENABLED.get() && count > threshold) {
            Iterator<EntityDataEntry> entryIterator = internalEntities.iterator();
            int offset = mainEntityDrops != null ? 1 : 0; // If main entity drops are present, we've already approximated one entity, make sure to account for it
            while (entryIterator.hasNext() && finalEntities.size() < approximationAmount - offset)
                finalEntities.add(entryIterator.next().createEntity(location, false, type));
            multiplier = (internalEntities.size() + offset) / (double) approximationAmount;
        } else {
            finalEntities.addAll(internalEntities.stream().map(x -> x.createEntity(location, false, type)).toList());
        }

        return this.calculateEntityDrops(finalEntities, multiplier, entityExpValue, lootingModifier, mainEntity, mainEntityDrops, originalStackSize, entityKillCount);
    }

    /**
     * Calculates the entity drops. May be called async or sync.
     *
     * @param count The number of entities to drop items for
     * @param includeMainEntity Whether to include the main entity in the calculation
     * @param entityExpValue The exp value of the entity
     * @param lootingModifier The looting modifier, nullable, defaults to the killer's looting value
     * @param mainEntity The main entity to use for loot calculations, primarily used to copy entity properties such as the killer, nullable, defaults to the stack entity
     * @param mainEntityDrops The main entity drops to include in the loot calculations, nullable
     * @return The calculated entity drops
     */
    @ApiStatus.Internal
    public EntityDrops calculateEntityDrops(int count, boolean includeMainEntity, int entityExpValue, Integer lootingModifier,
                                            LivingEntity mainEntity, EntityDrops mainEntityDrops,
                                            Integer originalStackSize, Integer entityKillCount) {
        // Cache the current entity just in case it somehow changes while we are processing the loot
        if (mainEntity == null)
            mainEntity = this.entity;

        count = Math.min(count, this.getStackSize() - 1);

        List<LivingEntity> finalEntities = new ArrayList<>();
        if (includeMainEntity && mainEntityDrops == null) {
            finalEntities.add(mainEntity);
            count++;
        }

        double multiplier = 1;
        int threshold = SettingKey.ENTITY_LOOT_APPROXIMATION_THRESHOLD.get();
        int approximationAmount = SettingKey.ENTITY_LOOT_APPROXIMATION_AMOUNT.get();
        if (SettingKey.ENTITY_LOOT_APPROXIMATION_ENABLED.get() && count > threshold) {
            int offset = mainEntityDrops != null ? 1 : 0; // If main entity drops are present, we've already approximated one entity, make sure to account for it
            this.stackedEntityDataStorage.forEachCapped(approximationAmount - offset, finalEntities::add);
            multiplier = (count + offset) / (double) approximationAmount;
        } else {
            this.stackedEntityDataStorage.forEachCapped(count, finalEntities::add);
        }

        return this.calculateEntityDrops(finalEntities, multiplier, entityExpValue, lootingModifier, mainEntity, mainEntityDrops, originalStackSize, entityKillCount);
    }

    private EntityDrops calculateEntityDrops(List<LivingEntity> stackEntities, double multiplier, int entityExpValue, Integer lootingModifier,
                                             LivingEntity mainEntity, EntityDrops mainEntityDrops, Integer originalStackSize, Integer entityKillCount) {
        if (originalStackSize == null)
            originalStackSize = this.getStackSize();

        if (entityKillCount == null)
            entityKillCount = stackEntities.size() + (mainEntityDrops != null ? 1 : 0);

        boolean propagateKiller = SettingKey.ENTITY_LOOT_PROPAGATE_KILLER.get();
        boolean fromSpawner = PersistentDataUtils.isSpawnedFromSpawner(this.entity);
        Location location = mainEntity.getLocation();
        Player killer = propagateKiller ? mainEntity.getKiller() : null;
        Entity froglightKiller = NMSUtil.getVersionNumber() >= 19 && mainEntity.getType() == EntityType.MAGMA_CUBE && mainEntity.getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent && damageEvent.getDamager().getType() == EntityType.FROG ? damageEvent.getDamager() : null;
        boolean callEvents = !RoseStackerAPI.getInstance().isEntityStackMultipleDeathEventCalled();
        boolean isAnimal = mainEntity instanceof Animals;
        boolean isWither = mainEntity.getType() == EntityType.WITHER;
        boolean killedByWither = mainEntity.getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent
                && (damageEvent.getDamager().getType() == EntityType.WITHER || damageEvent.getDamager().getType() == EntityType.WITHER_SKULL);
        boolean isSlime = mainEntity instanceof Slime;
        boolean isAccurateSlime = isSlime && this.stackSettings.getSettingValue(EntityStackSettings.SLIME_ACCURATE_DROPS_WITH_KILL_ENTIRE_STACK_ON_DEATH).getBoolean();

        ListMultimap<LivingEntity, EntityDrops> entityDrops = MultimapBuilder.linkedHashKeys().arrayListValues().build();
        if (mainEntityDrops != null)
            entityDrops.put(mainEntity, mainEntityDrops);

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        for (LivingEntity entity : stackEntities) {
            // Propagate fire ticks and last damage cause
            entity.setFireTicks(mainEntity.getFireTicks());
            if (fromSpawner)
                SpawnerFlagPersistenceHook.flagSpawnerSpawned(entity);
            nmsHandler.setLastHurtBy(entity, killer);
            entity.setLastDamageCause(propagateKiller ? mainEntity.getLastDamageCause() : null);

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
            int desiredExp = isBaby ? 0 : entityExpValue;
            for (int i = 0; i < iterations; i++) {
                List<ItemStack> entityItems;
                if (isBaby) {
                    entityItems = new ArrayList<>();
                } else {
                    if (lootingModifier != null) {
                        entityItems = new ArrayList<>(EntityUtils.getEntityLoot(entity, killer, location, lootingModifier));
                    } else {
                        entityItems = new ArrayList<>(EntityUtils.getEntityLoot(entity, killer, location));
                    }
                }

                if (isWither)
                    entityItems.add(new ItemStack(Material.NETHER_STAR));
                if (killedByWither)
                    entityItems.add(new ItemStack(Material.WITHER_ROSE));
                if (froglightKiller != null) {
                    Frog frog = (Frog) froglightKiller;
                    Material froglightType = switch (frog.getVariant().getKey().getKey()) {
                        case "cold" -> Material.VERDANT_FROGLIGHT;
                        case "temperate" -> Material.OCHRE_FROGLIGHT;
                        case "warm" -> Material.PEARLESCENT_FROGLIGHT;
                        default -> {
                            RoseStacker.getInstance().getLogger().warning("Unhandled frog type: " + frog.getVariant().getKey());
                            yield null;
                        }
                    };
                    if (froglightType != null)
                        entityItems.add(new ItemStack(froglightType));
                }

                int entityExperience;
                if (callEvents) {
                    EntityDeathEvent deathEvent = nmsHandler.createAsyncEntityDeathEvent(entity, entityItems, desiredExp);
                    Bukkit.getPluginManager().callEvent(deathEvent);
                    entityExperience = deathEvent.getDroppedExp();
                } else {
                    entityExperience = desiredExp;
                }

                entityDrops.put(entity, new EntityDrops(entityItems, entityExperience));
            }

            // Prevent magma cubes from splitting
            if (isSlime && entity.getType() == EntityType.MAGMA_CUBE)
                ((MagmaCube) entity).setSize(1);

            if (fromSpawner)
                SpawnerFlagPersistenceHook.unflagSpawnerSpawned(entity);
        }

        // Call the EntityStackMultipleDeathEvent if enabled
        if (!callEvents) {
            EntityStackMultipleDeathEvent event = new EntityStackMultipleDeathEvent(this, entityDrops, originalStackSize, entityKillCount, multiplier, mainEntity, killer, this::calculateFinalEntityDrops);
            Bukkit.getPluginManager().callEvent(event);
        }

        return this.calculateFinalEntityDrops(entityDrops, multiplier);
    }

    private EntityDrops calculateFinalEntityDrops(Multimap<LivingEntity, EntityDrops> entityDrops, double multiplier) {
        List<ItemStack> finalItems = new ArrayList<>();
        int finalExp = 0;
        for (EntityDrops drops : entityDrops.values()) {
            finalItems.addAll(drops.getDrops());
            finalExp += drops.getExperience();
        }

        // Multiply loot
        if (multiplier > 1) {
            finalItems = ItemUtils.getMultipliedItemStacks(finalItems, multiplier, true);
            finalExp = (int) Math.min(Math.round(finalExp * multiplier), Integer.MAX_VALUE);
        }

        return new EntityDrops(finalItems, finalExp);
    }

    /**
     * @return true if this entity should stay stacked, otherwise false
     */
    public boolean shouldStayStacked() {
        if (this.entity == null || this.stackSettings == null || this.stackedEntityDataStorage.isEmpty())
            return true;

        // Ender dragons call an EnderDragonChangePhaseEvent upon entity construction
        // We want to be able to do this check async, we just won't let ender dragons unstack without dying
        if (this.entity instanceof EnderDragon)
            return true;

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        LivingEntity entity = this.stackedEntityDataStorage.peek().createEntity(this.entity.getLocation(), false, this.entity.getType());
        StackedEntity stackedEntity = new StackedEntity(entity, nmsHandler.createEntityDataStorage(entity, RoseStacker.getInstance().getManager(StackManager.class).getEntityDataStorageType(entity.getType())));
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

        if (!SettingKey.ENTITY_DISPLAY_TAGS.get() || this.stackSettings == null || this.entity == null) {
            this.displayNameVisible = false;
            return this.displayName = this.entity == null ? null : this.entity.getCustomName();
        }

        if (this.entity.isDead()) {
            this.displayNameVisible = false;
            return null;
        }

        String customName = this.entity.getCustomName();
        if (this.getStackSize() > 1 || SettingKey.ENTITY_DISPLAY_TAGS_SINGLE.get()) {
            String displayString;
            StringPlaceholders.Builder placeholders = StringPlaceholders.builder("amount", StackerUtils.formatNumber(this.getStackSize()));
            NPCsHook.addCustomPlaceholders(this.entity, placeholders);

            if (customName != null && SettingKey.ENTITY_DISPLAY_TAGS_CUSTOM_NAME.get()) {
                placeholders.add("name", customName);
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("entity-stack-display-custom-name", placeholders.build());
            } else {
                placeholders.add("name", this.stackSettings.getDisplayName());
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("entity-stack-display", placeholders.build());
            }

            this.displayNameVisible = !SettingKey.ENTITY_DISPLAY_TAGS_HOVER.get();
            return this.displayName = displayString;
        } else if (this.getStackSize() == 1 && customName != null) {
            this.displayNameVisible = this.entity.isCustomNameVisible();
            return this.displayName = customName;
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

        if (SettingKey.ENTITY_STACK_FLYING_DOWNWARDS.get() && this.stackSettings.getEntityTypeData().flyingMob())
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
                || (SettingKey.SPAWNER_DISABLE_MOB_AI_OPTIONS_KILL_ENTIRE_STACK_ON_DEATH.get() && PersistentDataUtils.isAiDisabled(this.entity))
                || (lastDamageCause != null && SettingKey.ENTITY_KILL_ENTIRE_STACK_CONDITIONS.get().stream().anyMatch(x -> x.equalsIgnoreCase(lastDamageCause.getCause().name())))
                || (overrideKiller != null && SettingKey.ENTITY_KILL_ENTIRE_STACK_ON_DEATH_PERMISSION.get() && overrideKiller.hasPermission("rosestacker.killentirestack"));
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
        int amount = this.getStackSize();
        int experience = event != null ? event.getDroppedExp() : EntityUtils.getApproximateExperience(this.entity);
        if (SettingKey.ENTITY_DROP_ACCURATE_ITEMS.get()) {
            // Make sure the entity size is correct to allow drops
            if (this.entity.getType() == EntityType.SLIME) {
                ((Slime) this.entity).setSize(1);
            } else if (this.entity.getType() == EntityType.MAGMA_CUBE) {
                ((MagmaCube) this.entity).setSize(2);
            }

            if (event == null) {
                this.dropStackLoot(new ArrayList<>(), experience);
            } else {
                this.dropStackLoot(new ArrayList<>(event.getDrops()), experience);
                event.getDrops().clear();
            }

            if (this.entity.getType() == EntityType.MAGMA_CUBE)
                ((MagmaCube) this.entity).setSize(1); // Make sure it doesn't split
        } else if (SettingKey.ENTITY_DROP_ACCURATE_EXP.get()) {
            if (event == null) {
                EntitySpawnUtil.spawn(this.entity.getLocation(), ExperienceOrb.class, x -> x.setExperience(experience));
            } else {
                event.setDroppedExp(experience * amount);
            }
        }

        Player killer = this.entity.getKiller();
        if (killer != null && amount - 1 > 0 && SettingKey.MISC_STACK_STATISTICS.get())
            killer.incrementStatistic(Statistic.KILL_ENTITY, this.entity.getType(), amount - 1);

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

        List<EntityDataEntry> killedEntities = this.stackedEntityDataStorage.pop(amount - 1);
        if (SettingKey.ENTITY_DROP_ACCURATE_ITEMS.get()) {
            if (event == null) {
                this.dropPartialStackLoot(killedEntities, new ArrayList<>(), EntityUtils.getApproximateExperience(this.entity));
            } else {
                this.dropPartialStackLoot(killedEntities, new ArrayList<>(event.getDrops()), event.getDroppedExp());
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
        } else if (SettingKey.ENTITY_DROP_ACCURATE_EXP.get()) {
            if (event == null) {
                EntitySpawnUtil.spawn(this.entity.getLocation(), ExperienceOrb.class, x -> x.setExperience(EntityUtils.getApproximateExperience(this.entity)));
            } else {
                event.setDroppedExp(event.getDroppedExp() * (killedEntities.size() + 1));
            }
        }

        LivingEntity originalEntity = this.entity;

        this.decreaseStackSize();

        // Prevent the entity from splitting
        if (originalEntity instanceof Slime slime)
            slime.setSize(1);

        Player killer = originalEntity.getKiller();
        if (killer != null && amount - 1 > 0 && SettingKey.MISC_STACK_STATISTICS.get())
            killer.incrementStatistic(Statistic.KILL_ENTITY, this.entity.getType(), amount - 1);
    }

    /**
     * Checks if multiple entities are dying from the EntityDeathEvent and an {@link EntityStackMultipleDeathEvent} is
     * going to be called.
     *
     * @param event The EntityDeathEvent to check
     * @return true if multiple entities are dying during this event, false otherwise
     */
    public boolean areMultipleEntitiesDying(@NotNull EntityDeathEvent event) {
        // Individual events will be called if we are triggering death events
        if (SettingKey.ENTITY_TRIGGER_DEATH_EVENT_FOR_ENTIRE_STACK_KILL.get()
                || !SettingKey.ENTITY_DROP_ACCURATE_ITEMS.get()
                || this.getStackSize() == 1)
            return false;

        // Ignore if entire stack kill
        if (this.isEntireStackKilledOnDeath())
            return true;

        // Is partial stack kill
        Player killer = event.getEntity().getKiller();
        if (!SettingKey.ENTITY_MULTIKILL_ENABLED.get())
            return false;

        if (SettingKey.ENTITY_MULTIKILL_PLAYER_ONLY.get() && killer == null)
            return false;

        if (!SettingKey.ENTITY_MULTIKILL_ENCHANTMENT_ENABLED.get())
            return true;

        if (killer == null)
            return false;

        Enchantment requiredEnchantment = Enchantment.getByKey(NamespacedKey.fromString(SettingKey.ENTITY_MULTIKILL_ENCHANTMENT_TYPE.get()));
        if (requiredEnchantment == null)
            return false;

        return killer.getInventory().getItemInMainHand().getEnchantmentLevel(requiredEnchantment) > 0;
    }

    /**
     * @return true if the entity has moved since the last time this method was called
     */
    public boolean hasMoved() {
        Location location = this.entity.getLocation();
        boolean moved = location.getX() != this.x || location.getY() != this.y || location.getZ() != this.z;
        if (moved) {
            this.x = location.getX();
            this.y = location.getY();
            this.z = location.getZ();
        }
        return moved;
    }

    public void resetHasMoved() {
        this.x = this.y = this.z = 0;
    }

}
