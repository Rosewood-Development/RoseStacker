package dev.esophose.sparkstacker.stack;

import dev.esophose.sparkstacker.SparkStacker;
import dev.esophose.sparkstacker.event.AsyncEntityDeathEvent;
import dev.esophose.sparkstacker.manager.ConfigurationManager.Setting;
import dev.esophose.sparkstacker.manager.StackManager;
import dev.esophose.sparkstacker.stack.settings.EntityStackSettings;
import dev.esophose.sparkstacker.utils.EntitySerializer;
import dev.esophose.sparkstacker.utils.StackerUtils;
import dev.esophose.sparkstacker.utils.StringPlaceholders;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class StackedEntity extends Stack {

    private LivingEntity entity;
    private String originalCustomName;
    private List<String> serializedStackedEntities;

    private EntityStackSettings stackSettings;

    public StackedEntity(int id, LivingEntity entity, List<String> serializedStackedEntities, String originalCustomName) {
        super(id);

        this.entity = entity;
        this.serializedStackedEntities = serializedStackedEntities;

        if (this.entity != null) {
            this.originalCustomName = originalCustomName;
            this.stackSettings = SparkStacker.getInstance().getStackSettingManager().getEntityStackSettings(this.entity);

            if (Bukkit.isPrimaryThread())
                this.updateDisplay();
        }
    }

    public StackedEntity(LivingEntity entity, List<String> serializedStackedEntities) {
        this(-1, entity, serializedStackedEntities, null);
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public String getOriginalCustomName() {
        return this.originalCustomName;
    }

    public void updateOriginalCustomName() {
        this.originalCustomName = this.entity.getCustomName();
        this.updateDisplay();
    }

    public void increaseStackSize(LivingEntity entity) {
        this.increaseStackSize(entity, true);
    }

    public void increaseStackSize(LivingEntity entity, boolean updateDisplay) {
        if (Setting.ENTITY_STACK_TO_BOTTOM.getBoolean()) {
            this.serializedStackedEntities.add(EntitySerializer.toNBTString(entity));
        } else {
            this.serializedStackedEntities.add(0, EntitySerializer.toNBTString(entity));
        }

        if (updateDisplay)
            this.updateDisplay();
    }

    public void increaseStackSize(List<String> entityNBTStrings) {
        if (Setting.ENTITY_STACK_TO_BOTTOM.getBoolean()) {
            this.serializedStackedEntities.addAll(entityNBTStrings);
        } else {
            this.serializedStackedEntities.addAll(0, entityNBTStrings);
        }
        this.updateDisplay();
    }

    public void decreaseStackSize() {
        LivingEntity oldEntity = this.entity;
        Location location = this.entity.getLocation();
        this.entity = null; // Null it first so the CreatureSpawnEvent doesn't conflict with this Stack
        this.entity = EntitySerializer.fromNBTString(this.serializedStackedEntities.remove(0), location);
        this.updateOriginalCustomName();
        SparkStacker.getInstance().getStackManager().updateStackedEntityKey(oldEntity, this.entity);
    }

    public List<String> getStackedEntityNBTStrings() {
        return Collections.unmodifiableList(this.serializedStackedEntities);
    }

    /**
     * Drops all loot and experience for all internally-stacked entities.
     * Does not include loot for the current entity.
     *
     * @param existingLoot The loot from this.entity, nullable
     * @param droppedExp The exp dropped from this.entity
     */
    public void dropStackLoot(Collection<ItemStack> existingLoot, int droppedExp) {
        LivingEntity thisEntity = this.entity;
        Collection<ItemStack> loot = new ArrayList<>();
        if (existingLoot != null)
            loot.addAll(existingLoot);

        Bukkit.getScheduler().runTaskAsynchronously(SparkStacker.getInstance(), () -> {
            boolean callEvents = Setting.ENTITY_TRIGGER_DEATH_EVENT_FOR_ENTIRE_STACK_KILL.getBoolean();
            int fireTicks = thisEntity.getFireTicks(); // Propagate fire ticks so meats cook as you would expect
            int totalExp = droppedExp;
            for (String entityNBT : this.serializedStackedEntities) {
                LivingEntity entity = EntitySerializer.getNBTStringAsEntity(thisEntity.getType(), thisEntity.getLocation(), entityNBT);
                if (entity != null) {
                    entity.setFireTicks(fireTicks);
                    Collection<ItemStack> entityLoot = StackerUtils.getEntityLoot(entity, thisEntity.getKiller(), thisEntity.getLocation());
                    if (callEvents) {
                        EntityDeathEvent deathEvent = new AsyncEntityDeathEvent(entity, new ArrayList<>(entityLoot), droppedExp);
                        Bukkit.getPluginManager().callEvent(deathEvent);
                        totalExp += deathEvent.getDroppedExp();
                        loot.addAll(deathEvent.getDrops());
                    } else {
                        loot.addAll(entityLoot);
                        totalExp += droppedExp;
                    }
                }
            }

            int finalTotalExp = totalExp;
            World world = this.entity.getLocation().getWorld();
            if (world != null) {
                Bukkit.getScheduler().runTask(SparkStacker.getInstance(), () -> {
                    SparkStacker.getInstance().getStackManager().preStackItems(loot, this.entity.getLocation());
                    if (Setting.ENTITY_DROP_ACCURATE_EXP.getBoolean() && finalTotalExp > 0) {
                        ExperienceOrb experienceOrb = world.spawn(this.entity.getLocation(), ExperienceOrb.class);
                        experienceOrb.setExperience(finalTotalExp);
                    }
                });
            }
        });
    }

    /**
     * @return true if this entity should stay stacked, otherwise false
     */
    public boolean shouldStayStacked() {
        if (this.serializedStackedEntities.isEmpty())
            return true;

        LivingEntity entity = EntitySerializer.getNBTStringAsEntity(this.entity.getType(), this.entity.getLocation(), this.serializedStackedEntities.get(0));
        StackedEntity stackedEntity = new StackedEntity(entity, Collections.emptyList());
        return this.stackSettings.canStackWith(this, stackedEntity, true);
    }

    public StackedEntity split() {
        if (this.serializedStackedEntities.isEmpty())
            throw new IllegalStateException();

        StackManager stackManager = SparkStacker.getInstance().getStackManager();

        LivingEntity oldEntity = this.entity;
        stackManager.setEntityStackingTemporarilyDisabled(true);
        this.entity = EntitySerializer.fromNBTString(this.serializedStackedEntities.remove(0), oldEntity.getLocation());
        stackManager.setEntityStackingTemporarilyDisabled(false);
        this.stackSettings.applyUnstackProperties(this.entity, oldEntity);
        stackManager.updateStackedEntityKey(oldEntity, this.entity);
        this.updateDisplay();
        return new StackedEntity(oldEntity, new LinkedList<>());
    }

    public EntityStackSettings getStackSettings() {
        return this.stackSettings;
    }

    @Override
    public int getStackSize() {
        return this.serializedStackedEntities.size() + 1;
    }

    @Override
    public Location getLocation() {
        return this.entity.getLocation();
    }

    @Override
    public void updateDisplay() {
        if (!Setting.ENTITY_DISPLAY_TAGS.getBoolean())
            return;

        if (this.getStackSize() == 1 && this.originalCustomName != null) {
            this.entity.setCustomNameVisible(true);
            this.entity.setCustomName(this.originalCustomName);
        } else if (this.getStackSize() > 1 || Setting.ENTITY_DISPLAY_TAGS_SINGLE.getBoolean()) {
            String displayString;
            if (this.originalCustomName != null && Setting.ENTITY_DISPLAY_TAGS_CUSTOM_NAME.getBoolean()) {
                displayString = SparkStacker.getInstance().getLocaleManager().getLocaleMessage("entity-stack-display-custom-name", StringPlaceholders.builder("amount", this.getStackSize())
                        .addPlaceholder("name", this.originalCustomName).build());
            } else {
                displayString = SparkStacker.getInstance().getLocaleManager().getLocaleMessage("entity-stack-display", StringPlaceholders.builder("amount", this.getStackSize())
                        .addPlaceholder("name", this.stackSettings.getDisplayName()).build());
            }

            this.entity.setCustomNameVisible(!Setting.ENTITY_DISPLAY_TAGS_HOVER.getBoolean());
            this.entity.setCustomName(displayString);
        } else {
            this.entity.setCustomNameVisible(false);
            this.entity.setCustomName(null);
        }
    }

}
