package dev.rosewood.rosestacker.stack.settings;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.listener.RaidListener;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boss;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.Merchant;
import org.bukkit.material.Colorable;

public abstract class EntityStackSettings extends StackSettings {

    // Data that is part of this EntityType
    private final EntityTypeData entityTypeData;

    // Settings that apply to every entity
    private final boolean enabled;
    private final String displayName;
    private final int minStackSize;
    private final int maxStackSize;
    private final Boolean killEntireStackOnDeath;
    private final double mergeRadius;

    // Settings that apply to multiple entities through interfaces
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

    // Cached entity types
    private Boolean isMob;
    private Boolean isBoss;
    private Boolean isColorable;
    private Boolean isSittable;
    private Boolean isTameable;
    private Boolean isAnimals;
    private Boolean isAbstractHorse;
    private Boolean isChestedHorse;
    private Boolean isRaider;
    private Boolean isMerchant;

    public EntityStackSettings(CommentedFileConfiguration settingsFileConfiguration, JsonObject jsonObject) {
        super(settingsFileConfiguration);
        this.setDefaults();

        // Read EntityTypeData
        Gson gson = new Gson();
        JsonObject entityTypeDataObject = jsonObject.getAsJsonObject(this.getEntityType().name());
        boolean isSwimmingMob = entityTypeDataObject.get("is_swimming_mob").getAsBoolean();
        boolean isFlyingMob = entityTypeDataObject.get("is_flying_mob").getAsBoolean();
        JsonElement spawnEggMaterialElement = entityTypeDataObject.get("spawn_egg_material");
        Material spawnEggMaterial = spawnEggMaterialElement != null ? Material.getMaterial(spawnEggMaterialElement.getAsString()) : null;
        Type stringListType = new TypeToken<List<String>>(){}.getType();
        List<String> defaultSpawnRequirements = gson.fromJson(entityTypeDataObject.get("default_spawn_requirements").getAsJsonArray(), stringListType);
        String skullTexture = entityTypeDataObject.get("skull_texture").getAsString();
        List<String> breedingMaterialsStrings = gson.fromJson(entityTypeDataObject.get("breeding_materials").getAsJsonArray(), stringListType);
        List<Material> breedingMaterials = breedingMaterialsStrings.stream().map(Material::getMaterial).filter(Objects::nonNull).collect(Collectors.toList());
        this.entityTypeData = new EntityTypeData(isSwimmingMob, isFlyingMob, spawnEggMaterial, defaultSpawnRequirements, skullTexture, breedingMaterials);

        this.enabled = this.settingsConfiguration.getBoolean("enabled");
        this.displayName = this.settingsConfiguration.getString("display-name");
        this.minStackSize = this.settingsConfiguration.getInt("min-stack-size");
        this.maxStackSize = this.settingsConfiguration.getInt("max-stack-size");
        this.killEntireStackOnDeath = this.settingsConfiguration.getDefaultedBoolean("kill-entire-stack-on-death");
        this.mergeRadius = this.settingsConfiguration.getDouble("merge-radius");

        if (this.isEntityColorable())
            this.dontStackIfDifferentColor = this.settingsConfiguration.getBoolean("dont-stack-if-different-color");

        if (this.isEntitySittable())
            this.dontStackIfSitting = this.settingsConfiguration.getBoolean("dont-stack-if-sitting");

        if (this.isEntityTameable()) {
            this.dontStackIfTamed = this.settingsConfiguration.getBoolean("dont-stack-if-tamed");
            this.dontStackIfDifferentOwners = this.settingsConfiguration.getBoolean("dont-stack-if-different-owners");
        }

        if (this.isEntityAnimals()) {
            this.dontStackIfDifferentAge = this.settingsConfiguration.getBoolean("dont-stack-if-different-age");
            this.dontStackIfBaby = this.settingsConfiguration.getBoolean("dont-stack-if-baby");
            this.dontStackIfBreeding = this.settingsConfiguration.getBoolean("dont-stack-if-breeding");
        }

        if (this.isEntityAbstractHorse())
            this.dontStackIfSaddled = this.settingsConfiguration.getBoolean("dont-stack-if-saddled");

        if (this.isEntityChestedHorse())
            this.dontStackIfChested = this.settingsConfiguration.getBoolean("dont-stack-if-chested");

        if (this.isEntityRaider())
            this.dontStackIfPatrolLeader = this.settingsConfiguration.getBoolean("dont-stack-if-patrol-leader");

        if (this.isEntityMerchant())
            this.dontStackIfTrading = this.settingsConfiguration.getBoolean("dont-stack-if-trading");
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        this.setIfNotExists("enabled", !this.isEntityBoss());
        this.setIfNotExists("display-name", StackerUtils.formatName(this.getEntityType().name()));
        this.setIfNotExists("min-stack-size", -1);
        this.setIfNotExists("max-stack-size", -1);
        this.setIfNotExists("kill-entire-stack-on-death", "default");
        this.setIfNotExists("merge-radius", -1);

        if (this.isEntityColorable())
            this.setIfNotExists("dont-stack-if-different-color", false);

        if (this.isEntitySittable())
            this.setIfNotExists("dont-stack-if-sitting", false);

        if (this.isEntityTameable()) {
            this.setIfNotExists("dont-stack-if-tamed", false);
            this.setIfNotExists("dont-stack-if-different-owners", false);
        }

        if (this.isEntityAnimals()) {
            this.setIfNotExists("dont-stack-if-different-age", true);
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

    /**
     * Tests if one StackedEntity can stack with another
     *
     * @param stack1 The first stack
     * @param stack2 The second stack
     * @param comparingForUnstack true if the comparison is being made for unstacking, false otherwise
     * @return true if the two entities can stack into each other, false otherwise
     */
    public boolean testCanStackWith(StackedEntity stack1, StackedEntity stack2, boolean comparingForUnstack) {
        return this.canStackWith(stack1, stack2, comparingForUnstack, false) == EntityStackComparisonResult.CAN_STACK;
    }

    /**
     * Tests if one StackedEntity can stack with another
     *
     * @param stack1 The first stack
     * @param stack2 The second stack
     * @param comparingForUnstack true if the comparison is being made for unstacking, false otherwise
     * @param ignorePositions true if position checks for the entities should be ignored, false otherwise
     * @return true if the two entities can stack into each other, false otherwise
     */
    public boolean testCanStackWith(StackedEntity stack1, StackedEntity stack2, boolean comparingForUnstack, boolean ignorePositions) {
        return this.canStackWith(stack1, stack2, comparingForUnstack, ignorePositions) == EntityStackComparisonResult.CAN_STACK;
    }

    /**
     * Checks if one StackedEntity can stack with another and returns the comparison result
     *
     * @param stack1 The first stack
     * @param stack2 The second stack
     * @param comparingForUnstack true if the comparison is being made for unstacking, false otherwise
     * @param ignorePositions true if position checks for the entities should be ignored, false otherwise
     * @return the comparison result
     */
    public EntityStackComparisonResult canStackWith(StackedEntity stack1, StackedEntity stack2, boolean comparingForUnstack, boolean ignorePositions) {
        LivingEntity entity1 = stack1.getEntity();
        LivingEntity entity2 = stack2.getEntity();

        if (entity1.getType() != entity2.getType())
            return EntityStackComparisonResult.DIFFERENT_ENTITY_TYPES;

        if (!this.enabled)
            return EntityStackComparisonResult.STACKING_NOT_ENABLED;

        int offset = comparingForUnstack ? -1 : 0;
        if (stack1.getStackSize() + stack2.getStackSize() + offset > this.getMaxStackSize())
            return EntityStackComparisonResult.STACK_SIZE_TOO_LARGE;

        if (PersistentDataUtils.isUnstackable(entity1) || PersistentDataUtils.isUnstackable(entity2))
            return EntityStackComparisonResult.MARKED_UNSTACKABLE;

        if (Setting.ENTITY_DONT_STACK_CUSTOM_NAMED.getBoolean() && (entity1.getCustomName() != null || entity2.getCustomName() != null))
            return EntityStackComparisonResult.CUSTOM_NAMED;

        if (!comparingForUnstack && !ignorePositions && !this.getEntityTypeData().isSwimmingMob() && !this.getEntityTypeData().isFlyingMob()) {
            if (Setting.ENTITY_ONLY_STACK_ON_GROUND.getBoolean() && (!entity1.isOnGround() || !entity2.isOnGround()))
                return EntityStackComparisonResult.NOT_ON_GROUND;

            if (Setting.ENTITY_DONT_STACK_IF_IN_WATER.getBoolean() &&
                    (entity1.getLocation().getBlock().getType() == Material.WATER || entity2.getLocation().getBlock().getType() == Material.WATER))
                return EntityStackComparisonResult.IN_WATER;
        }

        if (!comparingForUnstack && Setting.ENTITY_ONLY_STACK_FROM_SPAWNERS.getBoolean() &&
                (!PersistentDataUtils.isSpawnedFromSpawner(entity1) || !PersistentDataUtils.isSpawnedFromSpawner(entity2)))
            return EntityStackComparisonResult.NOT_SPAWNED_FROM_SPAWNER;

        // Don't stack if being ridden or is riding something
        if ((!entity1.getPassengers().isEmpty() || !entity2.getPassengers().isEmpty() || entity1.isInsideVehicle() || entity2.isInsideVehicle()) && !comparingForUnstack)
            return EntityStackComparisonResult.PART_OF_VEHICLE; // If comparing for unstack and is being ridden or is riding something, don't want to unstack it

        if (!comparingForUnstack && Setting.ENTITY_DONT_STACK_IF_LEASHED.getBoolean() && (entity1.isLeashed() || entity2.isLeashed()))
            return EntityStackComparisonResult.LEASHED;

        if (Setting.ENTITY_DONT_STACK_IF_INVULNERABLE.getBoolean() && (entity1.isInvulnerable() || entity2.isInvulnerable()))
            return EntityStackComparisonResult.INVULNERABLE;

        if (this.isEntityColorable()) {
            Colorable colorable1 = (Colorable) entity1;
            Colorable colorable2 = (Colorable) entity2;

            if (this.dontStackIfDifferentColor && colorable1.getColor() != colorable2.getColor())
                return EntityStackComparisonResult.DIFFERENT_COLORS;
        }

        if (this.isEntitySittable()) {
            Sittable sittable1 = (Sittable) entity1;
            Sittable sittable2 = (Sittable) entity2;

            if (this.dontStackIfSitting && (sittable1.isSitting() || sittable2.isSitting()))
                return EntityStackComparisonResult.SITTING;
        }

        if (this.isEntityTameable()) {
            Tameable tameable1 = (Tameable) entity1;
            Tameable tameable2 = (Tameable) entity2;

            if (this.dontStackIfTamed && (tameable1.isTamed() || tameable2.isTamed()))
                return EntityStackComparisonResult.TAMED;

            if (this.dontStackIfDifferentOwners) {
                AnimalTamer tamer1 = tameable1.getOwner();
                AnimalTamer tamer2 = tameable2.getOwner();

                if (tamer1 != null && tamer2 != null && !tamer1.getUniqueId().equals(tamer2.getUniqueId()))
                    return EntityStackComparisonResult.DIFFERENT_OWNERS;
            }
        }

        if (this.isEntityAnimals()) {
            Animals animals1 = (Animals) entity1;
            Animals animals2 = (Animals) entity2;

            if (this.dontStackIfDifferentAge && animals1.isAdult() != animals2.isAdult())
                return EntityStackComparisonResult.DIFFERENT_AGES;

            if (this.dontStackIfBaby && (!animals1.isAdult() || !animals2.isAdult()))
                return EntityStackComparisonResult.BABY;

            if (this.dontStackIfBreeding && (animals1.isLoveMode() || animals2.isLoveMode() || (!animals1.canBreed() && animals1.isAdult()) || (!animals2.canBreed() && animals2.isAdult())))
                return EntityStackComparisonResult.BREEDING;
        }

        if (this.isEntityAbstractHorse()) {
            AbstractHorse abstractHorse1 = (AbstractHorse) entity1;
            AbstractHorse abstractHorse2 = (AbstractHorse) entity2;

            if (this.dontStackIfSaddled && (abstractHorse1.getInventory().getSaddle() != null || abstractHorse2.getInventory().getSaddle() != null))
                return EntityStackComparisonResult.SADDLED;
        }

        if (this.isEntityChestedHorse()) {
            ChestedHorse chestedHorse1 = (ChestedHorse) entity1;
            ChestedHorse chestedHorse2 = (ChestedHorse) entity2;

            if (this.dontStackIfChested && (chestedHorse1.isCarryingChest() || chestedHorse2.isCarryingChest()))
                return EntityStackComparisonResult.HAS_CHEST;
        }

        if (this.isEntityRaider()) {
            Raider raider1 = (Raider) entity1;
            Raider raider2 = (Raider) entity2;

            if (this.dontStackIfPatrolLeader && (raider1.isPatrolLeader() || raider2.isPatrolLeader()))
                return EntityStackComparisonResult.PATROL_LEADER;

            if (Setting.ENTITY_DONT_STACK_IF_ACTIVE_RAIDER.getBoolean() && (RaidListener.isActiveRaider(raider1) || RaidListener.isActiveRaider(raider2)))
                return EntityStackComparisonResult.PART_OF_ACTIVE_RAID;
        }

        if (this.isEntityMerchant()) {
            Merchant merchant1 = (Merchant) entity1;
            Merchant merchant2 = (Merchant) entity2;

            if (this.dontStackIfTrading && (merchant1.isTrading() || merchant2.isTrading()))
                return EntityStackComparisonResult.TRADING;
        }

        return this.canStackWithInternal(stack1, stack2);
    }

    @Override
    public String getConfigurationSectionKey() {
        return this.getEntityType().name();
    }

    private boolean isEntityMob() {
        if (this.isMob == null) {
            Class<?> entityClass = this.getEntityType().getEntityClass();
            if (entityClass == null) {
                this.isMob = false;
            } else {
                this.isMob = Mob.class.isAssignableFrom(entityClass);
            }
        }

        return this.isMob;
    }

    private boolean isEntityBoss() {
        if (this.isBoss == null) {
            Class<?> entityClass = this.getEntityType().getEntityClass();
            if (entityClass == null) {
                this.isBoss = false;
            } else {
                this.isBoss = Boss.class.isAssignableFrom(entityClass);
            }
        }

        return this.isBoss;
    }

    private boolean isEntityColorable() {
        if (this.isColorable == null) {
            Class<?> entityClass = this.getEntityType().getEntityClass();
            if (entityClass == null) {
                this.isColorable = false;
            } else {
                this.isColorable = Colorable.class.isAssignableFrom(entityClass);
            }
        }

        return this.isColorable;
    }

    private boolean isEntitySittable() {
        if (this.isSittable == null) {
            Class<?> entityClass = this.getEntityType().getEntityClass();
            if (entityClass == null) {
                this.isSittable = false;
            } else {
                this.isSittable = Sittable.class.isAssignableFrom(entityClass);
            }
        }

        return this.isSittable;
    }

    private boolean isEntityTameable() {
        if (this.isTameable == null) {
            Class<?> entityClass = this.getEntityType().getEntityClass();
            if (entityClass == null) {
                this.isTameable = false;
            } else {
                this.isTameable = Tameable.class.isAssignableFrom(entityClass);
            }
        }

        return this.isTameable;
    }

    private boolean isEntityAnimals() {
        if (this.isAnimals == null) {
            Class<?> entityClass = this.getEntityType().getEntityClass();
            if (entityClass == null) {
                this.isAnimals = false;
            } else {
                this.isAnimals = Animals.class.isAssignableFrom(entityClass);
            }
        }

        return this.isAnimals;
    }

    private boolean isEntityAbstractHorse() {
        if (this.isAbstractHorse == null) {
            Class<?> entityClass = this.getEntityType().getEntityClass();
            if (entityClass == null) {
                this.isAbstractHorse = false;
            } else {
                this.isAbstractHorse = AbstractHorse.class.isAssignableFrom(entityClass);
            }
        }

        return this.isAbstractHorse;
    }

    private boolean isEntityChestedHorse() {
        if (this.isChestedHorse == null) {
            Class<?> entityClass = this.getEntityType().getEntityClass();
            if (entityClass == null) {
                this.isChestedHorse = false;
            } else {
                this.isChestedHorse = ChestedHorse.class.isAssignableFrom(entityClass);
            }
        }

        return this.isChestedHorse;
    }

    private boolean isEntityRaider() {
        if (this.isRaider == null) {
            Class<?> entityClass = this.getEntityType().getEntityClass();
            if (entityClass == null) {
                this.isRaider = false;
            } else {
                this.isRaider = Raider.class.isAssignableFrom(entityClass);
            }
        }

        return this.isRaider;
    }

    private boolean isEntityMerchant() {
        if (this.isMerchant == null) {
            Class<?> entityClass = this.getEntityType().getEntityClass();
            if (entityClass == null) {
                this.isMerchant = false;
            } else {
                this.isMerchant = Merchant.class.isAssignableFrom(entityClass);
            }
        }

        return this.isMerchant;
    }

    @Override
    public boolean isStackingEnabled() {
        return this.enabled;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    public int getMinStackSize() {
        if (this.minStackSize != -1)
            return this.minStackSize;
        return Setting.ENTITY_MIN_STACK_SIZE.getInt();
    }

    @Override
    public int getMaxStackSize() {
        int size;
        if (this.maxStackSize != -1) {
            size = this.maxStackSize;
        } else {
            size = Setting.ENTITY_MAX_STACK_SIZE.getInt();
        }
        return Math.min(size, 1_000_000); // Force a max entity stack size of one million, there can be data problems otherwise
    }

    public boolean shouldKillEntireStackOnDeath() {
        if (this.killEntireStackOnDeath != null)
            return this.killEntireStackOnDeath;
        return Setting.ENTITY_KILL_ENTIRE_STACK_ON_DEATH.getBoolean();
    }

    public double getMergeRadius() {
        if (this.mergeRadius != -1)
            return this.mergeRadius;
        return Setting.ENTITY_MERGE_RADIUS.getDouble();
    }

    protected abstract void setDefaultsInternal();

    protected abstract EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2);

    /**
     * Applies special properties to an entity when it stacks
     *
     * @param stacking The entity getting stacked into another
     * @param stack The entity at the top of the result stack
     */
    public void applyStackProperties(LivingEntity stacking, LivingEntity stack) {
        // Does nothing by default, override in a subclass to add functionality
    }

    /**
     * Applies special properties to an entity when it unstacks
     *
     * @param stacked The entity that's still stacked
     * @param unstacked The unstacked entity
     */
    public void applyUnstackProperties(LivingEntity stacked, LivingEntity unstacked) {
        if (this.isEntityMob()) {
            Mob stackedMob = (Mob) stacked;
            Mob unstackedMob = (Mob) unstacked;

            stackedMob.setTarget(unstackedMob.getTarget());
        }

        if (this.isEntityAnimals() && Setting.ENTITY_CUMULATIVE_BREEDING.getBoolean()) {
            Animals stackedAnimals = (Animals) stacked;
            Animals unstackedAnimals = (Animals) unstacked;

            // The age determines how long the animal has to wait before it can breed again
            // Aging counts down until it reaches 0, at which it still stop and is capable of breeding
            stackedAnimals.setAge(unstackedAnimals.getAge());
        }

        stacked.setLastDamageCause(unstacked.getLastDamageCause());
    }

    /**
     * Applies properties to an entity after being spawned by a spawner
     *
     * @param entity The entity being spawned
     */
    public void applySpawnerSpawnedProperties(LivingEntity entity) {
        if (this.isEntityRaider() && Setting.SPAWNER_NERF_PATROL_LEADERS.getBoolean()) {
            Raider raider = (Raider) entity;

            raider.setPatrolLeader(false);
        }
    }

    /**
     * @return the data associated with this stack setting's EntityType
     */
    public EntityTypeData getEntityTypeData() {
        return this.entityTypeData;
    }

    public abstract EntityType getEntityType();

}
