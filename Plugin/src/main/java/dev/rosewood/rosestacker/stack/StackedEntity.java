package dev.rosewood.rosestacker.stack;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.event.AsyncEntityDeathEvent;
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
import dev.rosewood.rosestacker.stack.settings.entity.SlimeStackSettings;
import dev.rosewood.rosestacker.utils.DataUtils;
import dev.rosewood.rosestacker.utils.EntityUtils;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.util.Vector;

public class StackedEntity extends Stack<EntityStackSettings> implements Comparable<StackedEntity> {

    private LivingEntity entity;
    private StackedEntityDataStorage serializedStackedEntities;
    private int npcCheckCounter;

    private String displayName;
    private boolean displayNameVisible;

    private EntityStackSettings stackSettings;

    public StackedEntity(LivingEntity entity, StackedEntityDataStorage serializedStackedEntities) {
        this.entity = entity;
        this.serializedStackedEntities = serializedStackedEntities;
        this.npcCheckCounter = NPCsHook.anyEnabled() ? 5 : 0;

        this.displayName = null;
        this.displayNameVisible = false;

        if (this.entity != null) {
            this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getEntityStackSettings(this.entity);
            this.updateDisplay();
        }
    }

    public StackedEntity(LivingEntity entity) {
        this(entity, NMSAdapter.getHandler().createEntityDataStorage(entity));
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
        this.updateDisplay();
    }

    public void increaseStackSize(LivingEntity entity) {
        this.increaseStackSize(entity, true);
    }

    public void increaseStackSize(LivingEntity entity, boolean updateDisplay) {
        Runnable task = () -> {
            if (Setting.ENTITY_STACK_TO_BOTTOM.getBoolean()) {
                this.serializedStackedEntities.addLast(entity);
            } else {
                this.serializedStackedEntities.addFirst(entity);
            }

            if (updateDisplay)
                this.updateDisplay();
        };

        // VillagerAcquireTradeEvent and EnderDragonChangePhaseEvents are called when reading the entity NBT data.
        // Since we usually do this async and the event isn't allowed to be async, Spigot throws a fit.
        // We switch over to a non-async thread specifically for the entities of these events because of this.
        if (!Bukkit.isPrimaryThread() && (entity instanceof Merchant || entity instanceof EnderDragon)) {
            Bukkit.getScheduler().runTask(RoseStacker.getInstance(), task);
        } else {
            task.run();
        }
    }

    public void increaseStackSize(StackedEntityDataStorage serializedStackedEntities) {
        if (Setting.ENTITY_STACK_TO_BOTTOM.getBoolean()) {
            this.serializedStackedEntities.addAllLast(serializedStackedEntities.getAll());
        } else {
            this.serializedStackedEntities.addAllFirst(serializedStackedEntities.getAll());
        }
        this.updateDisplay();
    }

    /**
     * Unstacks the visible entity from the stack and moves the next in line to the front
     *
     * @return The new StackedEntity of size 1 that was just created
     */
    public StackedEntity decreaseStackSize() {
        if (this.serializedStackedEntities.isEmpty())
            throw new IllegalStateException();

        StackManager stackManager = RoseStacker.getInstance().getManager(StackManager.class);
        LivingEntity oldEntity = this.entity;

        stackManager.setEntityStackingTemporarilyDisabled(true);
        this.entity = NMSAdapter.getHandler().createEntityFromNBT(this.serializedStackedEntities.pop(), oldEntity.getLocation(), true, oldEntity.getType());
        stackManager.setEntityStackingTemporarilyDisabled(false);
        this.stackSettings.applyUnstackProperties(this.entity, oldEntity);
        stackManager.updateStackedEntityKey(oldEntity, this.entity);
        this.entity.setVelocity(this.entity.getVelocity().add(Vector.getRandom().multiply(0.01))); // Nudge the entity to unstack it from the old entity

        // Attempt to prevent adult entities from going into walls when a baby entity gets unstacked
        if (oldEntity instanceof Ageable) {
            Ageable ageable1 = (Ageable) oldEntity;
            Ageable ageable2 = (Ageable) this.entity;
            if (!ageable1.isAdult() && ageable2.isAdult()) {
                Location centered = ageable1.getLocation();
                centered.setX(centered.getBlockX() + 0.5);
                centered.setZ(centered.getBlockZ() + 0.5);
                ageable2.teleport(centered);
            }
        }

        this.updateDisplay();
        PersistentDataUtils.applyDisabledAi(this.entity);

        DataUtils.clearStackedEntityData(oldEntity);
        return new StackedEntity(oldEntity, NMSAdapter.getHandler().createEntityDataStorage(oldEntity));
    }

    public StackedEntityDataStorage getStackedEntityNBT() {
        return this.serializedStackedEntities;
    }

    /**
     * Warning! This should not be used outside of the plugin.
     * This method overwrites the serialized nbt and NOTHING ELSE.
     * If the stack size were to change, there would be no way of detecting it, you have been warned!
     *
     * @param serializedStackedEntities The nbt to overwrite with
     */
    public void setStackedEntityNBT(StackedEntityDataStorage serializedStackedEntities) {
        this.serializedStackedEntities = serializedStackedEntities;
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
        // Cache the current entity just in case it somehow changes while we are processing the loot
        LivingEntity thisEntity = this.entity;

        Bukkit.getScheduler().runTaskAsynchronously(RoseStacker.getInstance(), () -> {
            List<LivingEntity> internalEntities = new ArrayList<>();
            NMSHandler nmsHandler = NMSAdapter.getHandler();
            for (StackedEntityDataEntry<?> entityNBT : this.serializedStackedEntities.getAll()) {
                LivingEntity entity = nmsHandler.createEntityFromNBT(entityNBT, thisEntity.getLocation(), false, thisEntity.getType());
                if (entity == null)
                    continue;
                internalEntities.add(entity);
            }

            Bukkit.getScheduler().runTask(RoseStacker.getInstance(), () -> this.dropPartialStackLoot(internalEntities, existingLoot, droppedExp));
        });
    }

    /**
     * Drops loot for entities that are part of the stack.
     * Does not include loot for the current entity (except for nether stars for withers).
     *
     * @param internalEntities The entities which should be part of this stack
     * @param existingLoot The loot from this.entity, nullable
     * @param droppedExp The exp dropped from this.entity
     */
    public void dropPartialStackLoot(List<LivingEntity> internalEntities, Collection<ItemStack> existingLoot, int droppedExp) {
        // Cache the current entity just in case it somehow changes while we are processing the loot
        LivingEntity thisEntity = this.entity;
        Collection<ItemStack> loot = new ArrayList<>();
        if (existingLoot != null)
            loot.addAll(existingLoot);

        // The stack loot can either be processed synchronously or asynchronously depending on a setting
        // It should always be processed async unless errors are caused by other plugins
        boolean async = Setting.ENTITY_DEATH_EVENT_RUN_ASYNC.getBoolean();
        boolean multiplyCustomLoot = Setting.ENTITY_MULTIPLY_CUSTOM_LOOT.getBoolean();

        Runnable mainTask = () -> {
            boolean callEvents = Setting.ENTITY_TRIGGER_DEATH_EVENT_FOR_ENTIRE_STACK_KILL.getBoolean();
            int totalExp = droppedExp;
            NMSHandler nmsHandler = NMSAdapter.getHandler();
            boolean isAnimal = thisEntity instanceof Animals;
            boolean isSlime = thisEntity instanceof Slime;
            boolean isAccurateSlime = isSlime && ((SlimeStackSettings) this.stackSettings).isAccurateDropsWithKillEntireStackOnDeath();
            EntityType entityType = thisEntity.getType();
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
                    slime.setSize(1);
                }

                boolean isBaby = isAnimal && !((Animals) entity).isAdult();
                int desiredExp = isBaby ? 0 : droppedExp;
                for (int i = 0; i < iterations; i++) {
                    Collection<ItemStack> entityLoot = isBaby ? Collections.emptyList() : EntityUtils.getEntityLoot(entity, thisEntity.getKiller(), thisEntity.getLocation());
                    if (callEvents && !multiplyCustomLoot) {
                        EntityDeathEvent deathEvent = new AsyncEntityDeathEvent(entity, new ArrayList<>(entityLoot), desiredExp);
                        Bukkit.getPluginManager().callEvent(deathEvent);
                        totalExp += deathEvent.getDroppedExp();
                        loot.addAll(deathEvent.getDrops());
                    } else {
                        loot.addAll(entityLoot);
                        totalExp += desiredExp;
                    }
                }
            }

            if (multiplyCustomLoot) {
                EntityDeathEvent deathEvent = new EntityDeathEvent(thisEntity, new ArrayList<>(), 0);
                for (int i = 0, k = this.getStackSize() - 1; i < k; i++)
                    loot.addAll(deathEvent.getDrops());
                totalExp += deathEvent.getDroppedExp() * (this.getStackSize() - 1);
            }

            int finalTotalExp = totalExp;
            Runnable finishTask = () -> {
                RoseStacker.getInstance().getManager(StackManager.class).preStackItems(loot, thisEntity.getLocation());
                if (Setting.ENTITY_DROP_ACCURATE_EXP.getBoolean() && finalTotalExp > 0)
                    StackerUtils.dropExperience(thisEntity.getLocation(), finalTotalExp, finalTotalExp, finalTotalExp / 2);
            };

            // Withers always drop nether stars on death, however this isn't in the actual wither loot table for some reason
            if (this.entity.getType() == EntityType.WITHER)
                loot.addAll(GuiUtil.getMaterialAmountAsItemStacks(Material.NETHER_STAR, internalEntities.size()));

            if (async) {
                Bukkit.getScheduler().runTask(RoseStacker.getInstance(), finishTask);
            } else {
                finishTask.run();
            }
        };

        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(RoseStacker.getInstance(), mainTask);
        } else {
            mainTask.run();
        }
    }

    /**
     * @return true if this entity should stay stacked, otherwise false
     */
    public boolean shouldStayStacked() {
        if (this.entity == null || this.serializedStackedEntities.isEmpty())
            return true;

        // Ender dragons call an EnderDragonChangePhaseEvent upon entity construction
        // We want to be able to do this check async, we just won't let ender dragons unstack without dying
        if (this.entity instanceof EnderDragon)
            return true;

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        LivingEntity entity = nmsHandler.createEntityFromNBT(this.serializedStackedEntities.peek(), this.entity.getLocation(), false, this.entity.getType());
        StackedEntity stackedEntity = new StackedEntity(entity, nmsHandler.createEntityDataStorage(entity));
        return this.stackSettings.testCanStackWith(this, stackedEntity, true);
    }

    @Override
    public int getStackSize() {
        return this.serializedStackedEntities.size() + 1;
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
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("entity-stack-display-custom-name", StringPlaceholders.builder("amount", this.getStackSize())
                        .addPlaceholder("name", customName).build());
            } else {
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("entity-stack-display", StringPlaceholders.builder("amount", this.getStackSize())
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

        if (Setting.ENTITY_STACK_FLYING_DOWNWARDS.getBoolean() && this.stackSettings.getEntityTypeData().isFlyingMob())
            return entity1.getLocation().getY() < entity2.getLocation().getY() ? 3 : -3;

        if (this.getStackSize() == stack2.getStackSize())
            return entity1.getTicksLived() > entity2.getTicksLived() ? 2 : -2;

        return this.getStackSize() > stack2.getStackSize() ? 1 : -1;
    }

}
