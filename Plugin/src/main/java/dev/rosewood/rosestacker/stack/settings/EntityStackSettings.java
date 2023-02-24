package dev.rosewood.rosestacker.stack.settings;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.utils.RoseGardenUtils;
import dev.rosewood.rosestacker.hook.SpawnerFlagPersistenceHook;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.conditions.entity.StackConditions;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.ApiStatus;

public class EntityStackSettings extends StackSettings {

    // Entity-specific settings
    public static final String CHICKEN_MULTIPLY_EGG_DROPS_BY_STACK_SIZE = "multiply-egg-drops-by-stack-size";
    public static final String SHEEP_SHEAR_ALL_SHEEP_IN_STACK = "shear-all-sheep-in-stack";
    public static final String SHEEP_PERCENTAGE_OF_WOOL_TO_REGROW_PER_GRASS_EATEN = "percentage-of-wool-to-regrow-per-grass-eaten";
    public static final String SLIME_ACCURATE_DROPS_WITH_KILL_ENTIRE_STACK_ON_DEATH = "accurate-drops-with-kill-entire-stack-on-death";
    public static final String MOOSHROOM_DROP_ADDITIONAL_MUSHROOMS_FOR_EACH_COW_IN_STACK = "drop-additional-mushrooms-for-each-cow-in-stack";
    public static final String MOOSHROOM_EXTRA_MUSHROOMS_PER_COW_IN_STACK = "extra-mushrooms-per-cow-in-stack";

    // Data pertaining to this EntityType
    private final EntityType entityType;
    private final Class<? extends Entity> entityClass;
    private final EntityTypeData entityTypeData;
    private final Map<Class<?>, Boolean> assignableClassMap;

    // Conditions and settings loaded specifically for this entity type
    private final List<StackConditionEntry<?>> stackConditions;
    private final Map<String, EntitySetting> extraSettings;

    // Settings that apply to every entity
    private final boolean enabled;
    private final String displayName;
    private final int minStackSize;
    private final int maxStackSize;
    private final Boolean killEntireStackOnDeath;
    private final double mergeRadius;
    private final Boolean onlyStackFromSpawners;

    public EntityStackSettings(CommentedFileConfiguration settingsFileConfiguration, JsonObject jsonObject, EntityType entityType) {
        super(settingsFileConfiguration);

        this.entityType = entityType;
        this.entityClass = entityType.getEntityClass();

        if (this.entityClass == null)
            throw new IllegalArgumentException("EntityType " + this.entityType.name() + " has no entity class");

        this.assignableClassMap = new HashMap<>();

        List<StackConditions.StackCondition<?>> stackConditions = StackConditions.getEligibleConditions(this.entityClass);
        this.stackConditions = new ArrayList<>(stackConditions.size());
        for (StackConditions.StackCondition<?> stackCondition : stackConditions)
            this.stackConditions.add(new StackConditionEntry<>(stackCondition));

        this.extraSettings = new HashMap<>();
        switch (this.entityType) {
            case CHICKEN -> this.putSetting(CHICKEN_MULTIPLY_EGG_DROPS_BY_STACK_SIZE, true);
            case SHEEP -> {
                this.putSetting(SHEEP_SHEAR_ALL_SHEEP_IN_STACK, true);
                this.putSetting(SHEEP_PERCENTAGE_OF_WOOL_TO_REGROW_PER_GRASS_EATEN, 25.0);
            }
            case SLIME, MAGMA_CUBE -> this.putSetting(SLIME_ACCURATE_DROPS_WITH_KILL_ENTIRE_STACK_ON_DEATH, true);
            case MUSHROOM_COW -> {
                this.putSetting(MOOSHROOM_DROP_ADDITIONAL_MUSHROOMS_FOR_EACH_COW_IN_STACK, true);
                this.putSetting(MOOSHROOM_EXTRA_MUSHROOMS_PER_COW_IN_STACK, 5);
            }
        }

        this.setDefaults();

        // Read EntityTypeData
        Gson gson = new Gson();
        boolean isSwimmingMob = jsonObject.get("is_swimming_mob").getAsBoolean();
        boolean isFlyingMob = jsonObject.get("is_flying_mob").getAsBoolean();
        JsonElement spawnEggMaterialElement = jsonObject.get("spawn_egg_material");
        Material spawnEggMaterial = spawnEggMaterialElement != null ? Material.getMaterial(spawnEggMaterialElement.getAsString()) : null;
        Type stringListType = new TypeToken<List<String>>(){}.getType();
        List<String> defaultSpawnRequirements = gson.fromJson(jsonObject.get("default_spawn_requirements").getAsJsonArray(), stringListType);
        String skullTexture = jsonObject.get("skull_texture").getAsString();
        List<String> breedingMaterialsStrings = gson.fromJson(jsonObject.get("breeding_materials").getAsJsonArray(), stringListType);
        Set<Material> breedingMaterials = breedingMaterialsStrings.stream().map(Material::getMaterial).filter(Objects::nonNull).collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));
        String spawnCategory = jsonObject.get("spawn_category").getAsString();
        List<String> standardEquipmentStrings = gson.fromJson(jsonObject.get("standard_equipment").getAsJsonArray(), stringListType);
        Set<Material> standardEquipment = standardEquipmentStrings.stream().map(Material::getMaterial).filter(Objects::nonNull).collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));
        this.entityTypeData = new EntityTypeData(isSwimmingMob, isFlyingMob, spawnEggMaterial, defaultSpawnRequirements, skullTexture, breedingMaterials, spawnCategory, standardEquipment);

        this.enabled = this.settingsConfiguration.getBoolean("enabled");
        this.displayName = this.settingsConfiguration.getString("display-name");
        this.minStackSize = this.settingsConfiguration.getInt("min-stack-size");
        this.maxStackSize = this.settingsConfiguration.getInt("max-stack-size");
        this.killEntireStackOnDeath = this.settingsConfiguration.getDefaultedBoolean("kill-entire-stack-on-death");
        this.mergeRadius = this.settingsConfiguration.getDouble("merge-radius");
        this.onlyStackFromSpawners = this.settingsConfiguration.getDefaultedBoolean("only-stack-from-spawners");

        this.stackConditions.forEach(StackConditionEntry::load);
        this.extraSettings.values().forEach(EntitySetting::load);
    }

    private void putSetting(String key, Object defaultValue) {
        this.extraSettings.put(key, new EntitySetting(key, defaultValue));
    }

    @ApiStatus.Experimental
    public EntitySetting getSettingValue(String key) {
        return this.extraSettings.get(key);
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        this.setIfNotExists("enabled", !this.isEntity(Boss.class));
        this.setIfNotExists("display-name", StackerUtils.formatName(this.entityType.name()));
        this.setIfNotExists("min-stack-size", -1);
        this.setIfNotExists("max-stack-size", -1);
        this.setIfNotExists("kill-entire-stack-on-death", "default");
        this.setIfNotExists("merge-radius", -1);
        this.setIfNotExists("only-stack-from-spawners", "default");

        this.stackConditions.forEach(StackConditionEntry::setDefaults);
        this.extraSettings.values().forEach(EntitySetting::setDefaults);
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
        return this.testCanStackWith(stack1, stack2, comparingForUnstack, false);
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

        for (StackConditionEntry<?> stackCondition : this.stackConditions) {
            EntityStackComparisonResult result = stackCondition.apply(this, stack1, stack2, entity1, entity2, comparingForUnstack, ignorePositions);
            if (result != EntityStackComparisonResult.CAN_STACK)
                return result;
        }

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    public String getConfigurationSectionKey() {
        return this.entityType.name();
    }

    private boolean isEntity(Class<?> assignableClass) {
        return this.assignableClassMap.computeIfAbsent(assignableClass, x -> x.isAssignableFrom(this.entityClass));
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
        if (this.maxStackSize != -1)
            return this.maxStackSize;
        return Setting.ENTITY_MAX_STACK_SIZE.getInt();
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

    public boolean shouldOnlyStackFromSpawners() {
        if (this.onlyStackFromSpawners != null)
            return this.onlyStackFromSpawners;
        return Setting.ENTITY_ONLY_STACK_FROM_SPAWNERS.getBoolean();
    }

    /**
     * Applies special properties to an entity when it stacks
     *
     * @param stacking The entity getting stacked into another
     * @param stack The entity at the top of the result stack
     */
    public void applyStackProperties(LivingEntity stacking, LivingEntity stack) {
        switch (this.entityType) {
            case CREEPER -> NMSAdapter.getHandler().unigniteCreeper((Creeper) stacking);
        }

        SpawnerFlagPersistenceHook.unflagSpawnerSpawned(stacking);
    }

    /**
     * Applies special properties to an entity when it unstacks
     *
     * @param stacked The entity that's still stacked
     * @param unstacked The unstacked entity
     */
    public void applyUnstackProperties(LivingEntity stacked, LivingEntity unstacked) {
        if (this.isEntity(Mob.class)) {
            Mob stackedMob = (Mob) stacked;
            Mob unstackedMob = (Mob) unstacked;

            stackedMob.setTarget(unstackedMob.getTarget());
        }

        if (this.isEntity(Animals.class) && Setting.ENTITY_CUMULATIVE_BREEDING.getBoolean()) {
            Animals stackedAnimals = (Animals) stacked;
            Animals unstackedAnimals = (Animals) unstacked;

            // The age determines how long the animal has to wait before it can breed again
            // Aging counts down until it reaches 0, at which it will stop and is capable of breeding
            stackedAnimals.setAge(unstackedAnimals.getAge());
        }

        switch (this.entityType) {
            case BEE -> ((Bee) stacked).setAnger(((Bee) unstacked).getAnger());
            case WOLF -> ((Wolf) stacked).setAngry(((Wolf) unstacked).isAngry());
            case ZOMBIFIED_PIGLIN -> ((PigZombie) stacked).setAngry(((PigZombie) unstacked).isAngry());
        }

        SpawnerFlagPersistenceHook.setPersistence(stacked);

        stacked.setLastDamageCause(unstacked.getLastDamageCause());

        if (Setting.ENTITY_KILL_TRANSFER_FIRE.getBoolean())
            stacked.setFireTicks(unstacked.getFireTicks());
    }

    /**
     * Applies properties to an entity after being spawned by a spawner
     *
     * @param entity The entity being spawned
     */
    public void applySpawnerSpawnedProperties(LivingEntity entity) {
        SpawnerFlagPersistenceHook.flagSpawnerSpawned(entity);
        PersistentDataUtils.tagSpawnedFromSpawner(entity);

        if (this.isEntity(Raider.class) && Setting.SPAWNER_NERF_PATROL_LEADERS.getBoolean())
            ((Raider) entity).setPatrolLeader(false);

        if (Setting.SPAWNER_REMOVE_EQUIPMENT.getBoolean()) {
            EntityEquipment equipment = entity.getEquipment();
            if (equipment != null)
                equipment.clear();
        }

        if (this.isEntity(Ageable.class))
            ((Ageable) entity).setAdult();
    }

    /**
     * @return the EntityType for this EntityStackSettings
     */
    public EntityType getEntityType() {
        return this.entityType;
    }

    /**
     * @return the data associated with this stack setting's EntityType
     */
    public EntityTypeData getEntityTypeData() {
        return this.entityTypeData;
    }

    @SuppressWarnings("unchecked")
    private class StackConditionEntry<T> {

        private final StackConditions.StackCondition<T> condition;
        private boolean enabled;

        public StackConditionEntry(StackConditions.StackCondition<T> condition) {
            this.condition = condition;
            this.enabled = true;
        }

        public EntityStackComparisonResult apply(EntityStackSettings stackSettings, StackedEntity stack1,
                                                 StackedEntity stack2, Entity entity1, Entity entity2, boolean comparingForUnstack,
                                                 boolean ignorePositions) {
            if (!this.enabled)
                return EntityStackComparisonResult.CAN_STACK;
            return this.condition.function().apply(stackSettings, stack1, stack2, (T) entity1, (T) entity2, comparingForUnstack, ignorePositions);
        }

        public void setDefaults() {
            StackConditions.ConfigProperties configProperties = this.condition.configProperties();
            if (configProperties != null)
                EntityStackSettings.this.setIfNotExists(configProperties.key(), configProperties.defaultValue());
        }

        public void load() {
            StackConditions.ConfigProperties configProperties = this.condition.configProperties();
            if (configProperties != null)
                this.enabled = EntityStackSettings.this.settingsConfiguration.getBoolean(configProperties.key(), configProperties.defaultValue());
        }

    }

    public class EntitySetting {

        private final String key;
        private final Object defaultValue;
        private Object value;

        public EntitySetting(String key, Object defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public void setDefaults() {
            EntityStackSettings.this.setIfNotExists(this.key, this.defaultValue);
        }

        public void load() {
            this.value = EntityStackSettings.this.settingsConfiguration.get(this.key, this.defaultValue);
        }

        public boolean getBoolean() {
            return (boolean) this.value;
        }

        public int getInt() {
            return (int) RoseGardenUtils.getNumber(this.value);
        }

        public double getDouble() {
            return RoseGardenUtils.getNumber(this.value);
        }

    }

}
