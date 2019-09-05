package dev.esophose.rosestacker.stack.settings;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.Merchant;
import org.bukkit.material.Colorable;

public abstract class EntityStackSettings {

    private ConfigurationSection entitySettingsConfiguration;

    private boolean enabled;
    private String displayName;
    private int minStackSize;
    private int maxStackSize;

    private boolean dontStackIfDifferentColor;
    private boolean dontStackIfSitting;
    private boolean dontStackIfTamed;
    private boolean dontStackIfDifferentOwners;
    private boolean dontStackIfDifferentAge;
    private boolean dontStackIfBaby;
    private boolean dontStackIfBreeding;
    private boolean dontStackIfSaddled;
    private boolean dontStackIfChested;
    private boolean dontStackIfPatrolLeader;
    private boolean dontStackIfTrading;

    public EntityStackSettings(YamlConfiguration entitySettingsYamlConfiguration) {
        this.entitySettingsConfiguration = entitySettingsYamlConfiguration.getConfigurationSection(this.getEntityType().name());
        if (this.entitySettingsConfiguration == null)
            this.entitySettingsConfiguration = entitySettingsYamlConfiguration.createSection(this.getEntityType().name());

        this.setDefaults();

        this.enabled = this.entitySettingsConfiguration.getBoolean("enabled");
        this.displayName = this.entitySettingsConfiguration.getString("display-name");
        this.minStackSize = this.entitySettingsConfiguration.getInt("min-stack-size");
        this.maxStackSize = this.entitySettingsConfiguration.getInt("max-stack-size");

        if (this.isEntityColorable())
            this.dontStackIfDifferentColor = this.entitySettingsConfiguration.getBoolean("dont-stack-if-different-color");

        if (this.isEntitySittable())
            this.dontStackIfSitting = this.entitySettingsConfiguration.getBoolean("dont-stack-if-sitting");

        if (this.isEntityTameable()) {
            this.dontStackIfTamed = this.entitySettingsConfiguration.getBoolean("dont-stack-if-tamed");
            this.dontStackIfDifferentOwners = this.entitySettingsConfiguration.getBoolean("dont-stack-if-different-owners");
        }

        if (this.isEntityAnimals()) {
            this.dontStackIfDifferentAge = this.entitySettingsConfiguration.getBoolean("dont-stack-if-different-age");
            this.dontStackIfBaby = this.entitySettingsConfiguration.getBoolean("dont-stack-if-baby");
            this.dontStackIfBreeding = this.entitySettingsConfiguration.getBoolean("dont-stack-if-breeding");
        }

        if (this.isEntityAbstractHorse())
            this.dontStackIfSaddled = this.entitySettingsConfiguration.getBoolean("dont-stack-if-saddled");

        if (this.isEntityChestedHorse())
            this.dontStackIfChested = this.entitySettingsConfiguration.getBoolean("dont-stack-if-chested");

        if (this.isEntityRaider())
            this.dontStackIfPatrolLeader = this.entitySettingsConfiguration.getBoolean("dont-stack-if-patrol-leader");

        if (this.isEntityMerchant())
            this.dontStackIfTrading = this.entitySettingsConfiguration.getBoolean("dont-stack-if-trading");
    }

    private void setDefaults() {
        this.setIfNotExists("enabled", true);
        this.setIfNotExists("display-name", StackerUtils.formatName(this.getEntityType().name()));
        this.setIfNotExists("min-stack-size", -1);
        this.setIfNotExists("max-stack-size", -1);

        if (this.isEntityColorable())
            this.setIfNotExists("dont-stack-if-different-color", false);

        if (this.isEntitySittable())
            this.setIfNotExists("dont-stack-if-sitting", false);

        if (this.isEntityTameable()) {
            this.setIfNotExists("dont-stack-if-tamed", false);
            this.setIfNotExists("dont-stack-if-different-owners", false);
        }

        if (this.isEntityAnimals()) {
            this.setIfNotExists("dont-stack-if-different-age", false);
            this.setIfNotExists("dont-stack-if-baby", false);
            this.setIfNotExists("dont-stack-if-breeding", false);
        }

        if (this.isEntityAbstractHorse())
            this.setIfNotExists("dont-stack-if-saddled", false);

        if (this.isEntityChestedHorse())
            this.setIfNotExists("dont-stack-if-chested", false);

        if (this.isEntityRaider())
            this.setIfNotExists("dont-stack-if-patrol-leader", false);

        if (this.isEntityMerchant())
            this.setIfNotExists("dont-stack-if-trading", false);

        this.setDefaultsInternal();
    }

    public boolean canStackWith(StackedEntity stack1, StackedEntity stack2) {
        if (!this.enabled)
            return false;

        if (stack1.getStackSize() + stack2.getStackSize() > this.maxStackSize)
            return false;

        LivingEntity entity1 = stack1.getEntity();
        LivingEntity entity2 = stack2.getEntity();

        if (this.isEntityColorable()) {
            Colorable colorable1 = (Colorable) entity1;
            Colorable colorable2 = (Colorable) entity2;

            if (this.dontStackIfDifferentColor && colorable1.getColor() != colorable2.getColor())
                return false;
        }

        if (this.isEntitySittable()) {
            Sittable sittable1 = (Sittable) entity1;
            Sittable sittable2 = (Sittable) entity2;

            if (this.dontStackIfSitting && (sittable1.isSitting() || sittable2.isSitting()))
                return false;
        }

        if (this.isEntityTameable()) {
            Tameable tameable1 = (Tameable) entity1;
            Tameable tameable2 = (Tameable) entity2;

            if (this.dontStackIfTamed && (tameable1.isTamed() || tameable2.isTamed()))
                return false;

            if (this.dontStackIfDifferentOwners) {
                AnimalTamer tamer1 = tameable1.getOwner();
                AnimalTamer tamer2 = tameable2.getOwner();

                if (tamer1 != null && tamer2 != null && !tamer1.getUniqueId().equals(tamer2.getUniqueId()))
                    return false;
            }
        }

        if (this.isEntityAnimals()) {
            Animals animals1 = (Animals) entity1;
            Animals animals2 = (Animals) entity2;

            if (this.dontStackIfDifferentAge && animals1.isAdult() != animals2.isAdult())
                return false;

            if (this.dontStackIfBaby && (!animals1.isAdult() || !animals2.isAdult()))
                return false;

            if (this.dontStackIfBreeding && (animals1.isLoveMode() || animals2.isLoveMode() || !animals1.canBreed() || !animals2.canBreed()))
                return false;
        }

        if (this.isEntityAbstractHorse()) {
            AbstractHorse abstractHorse1 = (AbstractHorse) entity1;
            AbstractHorse abstractHorse2 = (AbstractHorse) entity2;

            if (this.dontStackIfSaddled && (abstractHorse1.getInventory().getSaddle() != null || abstractHorse2.getInventory().getSaddle() != null))
                return false;
        }

        if (this.isEntityChestedHorse()) {
            ChestedHorse chestedHorse1 = (ChestedHorse) entity1;
            ChestedHorse chestedHorse2 = (ChestedHorse) entity2;

            if (this.dontStackIfChested && (chestedHorse1.isCarryingChest() || chestedHorse2.isCarryingChest()))
                return false;
        }

        if (this.isEntityRaider()) {
            Raider raider1 = (Raider) entity1;
            Raider raider2 = (Raider) entity2;

            if (this.dontStackIfPatrolLeader && raider1.isPatrolLeader() || raider2.isPatrolLeader())
                return false;
        }

        if (this.isEntityMerchant()) {
            Merchant merchant1 = (Merchant) entity1;
            Merchant merchant2 = (Merchant) entity2;

            if (this.dontStackIfTrading && (merchant1.isTrading() || merchant2.isTrading()))
                return false;
        }

        return this.canStackWithInternal(stack1, stack2);
    }

    protected void setIfNotExists(String setting, Object value) {
        if (this.entitySettingsConfiguration.get(setting) == null)
            this.entitySettingsConfiguration.set(setting, value);
    }

    private boolean isEntityColorable() {
        Class<?> entityClass = this.getEntityType().getEntityClass();
        if (entityClass == null)
            return false;

        return Colorable.class.isAssignableFrom(entityClass);
    }

    private boolean isEntitySittable() {
        Class<?> entityClass = this.getEntityType().getEntityClass();
        if (entityClass == null)
            return false;

        return Sittable.class.isAssignableFrom(entityClass);
    }

    private boolean isEntityTameable() {
        Class<?> entityClass = this.getEntityType().getEntityClass();
        if (entityClass == null)
            return false;

        return Tameable.class.isAssignableFrom(entityClass);
    }

    private boolean isEntityAnimals() {
        Class<?> entityClass = this.getEntityType().getEntityClass();
        if (entityClass == null)
            return false;

        return Animals.class.isAssignableFrom(entityClass);
    }

    private boolean isEntityAbstractHorse() {
        Class<?> entityClass = this.getEntityType().getEntityClass();
        if (entityClass == null)
            return false;

        return AbstractHorse.class.isAssignableFrom(entityClass);
    }

    private boolean isEntityChestedHorse() {
        Class<?> entityClass = this.getEntityType().getEntityClass();
        if (entityClass == null)
            return false;

        return ChestedHorse.class.isAssignableFrom(entityClass);
    }

    private boolean isEntityRaider() {
        Class<?> entityClass = this.getEntityType().getEntityClass();
        if (entityClass == null)
            return false;

        return Raider.class.isAssignableFrom(entityClass);
    }

    private boolean isEntityMerchant() {
        Class<?> entityClass = this.getEntityType().getEntityClass();
        if (entityClass == null)
            return false;

        return Merchant.class.isAssignableFrom(entityClass);
    }

    public boolean isStackingEnabled() {
        return this.enabled;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getMinStackSize() {
        return this.minStackSize;
    }

    public int getMaxStackSize() {
        return this.maxStackSize;
    }

    protected abstract void setDefaultsInternal();

    protected abstract boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2);

    public abstract EntityType getEntityType();

}
