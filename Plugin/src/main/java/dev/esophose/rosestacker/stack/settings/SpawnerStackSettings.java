package dev.esophose.rosestacker.stack.settings;

import com.google.common.collect.ImmutableSet;
import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.stack.StackedSpawner;
import dev.esophose.rosestacker.utils.StackerUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.WaterMob;

public class SpawnerStackSettings extends StackSettings<StackedSpawner> {

    private static Map<EntityType, SpawnConditions> defaultSpawnConditions;

    static {
        Map<EntityType, SpawnConditions> exceptions = new HashMap<>();
        exceptions.put(EntityType.BAT, new SpawnConditions(ImmutableSet.of(Material.AIR), LightLevel.DARK));
        exceptions.put(EntityType.ELDER_GUARDIAN, new SpawnConditions(ImmutableSet.of(Material.WATER), LightLevel.DARK));
        exceptions.put(EntityType.GUARDIAN, new SpawnConditions(ImmutableSet.of(Material.WATER), LightLevel.DARK));
        exceptions.put(EntityType.MAGMA_CUBE, new SpawnConditions(ImmutableSet.of(Material.AIR), LightLevel.ANY));
        exceptions.put(EntityType.MUSHROOM_COW, new SpawnConditions(ImmutableSet.of(Material.MYCELIUM), LightLevel.ANY));
        exceptions.put(EntityType.OCELOT, new SpawnConditions(ImmutableSet.of(Material.GRASS_BLOCK, Material.ACACIA_LEAVES, Material.BIRCH_LEAVES, Material.DARK_OAK_LEAVES, Material.JUNGLE_LEAVES, Material.OAK_LEAVES, Material.SPRUCE_LEAVES), LightLevel.LIGHT));
        exceptions.put(EntityType.PARROT, new SpawnConditions(ImmutableSet.of(Material.AIR), LightLevel.ANY));
        exceptions.put(EntityType.SKELETON_HORSE, new SpawnConditions(ImmutableSet.of(Material.AIR), LightLevel.DARK));
        exceptions.put(EntityType.TURTLE, new SpawnConditions(ImmutableSet.of(Material.AIR), LightLevel.ANY));
        exceptions.put(EntityType.WOLF, new SpawnConditions(ImmutableSet.of(Material.AIR), LightLevel.ANY));
        exceptions.put(EntityType.ZOMBIE_HORSE, new SpawnConditions(ImmutableSet.of(Material.AIR), LightLevel.DARK));

        defaultSpawnConditions = new HashMap<>();
        for (EntityType entityType : StackerUtils.getAlphabeticalStackableEntityTypes()) {
            if (exceptions.containsKey(entityType)) {
                defaultSpawnConditions.put(entityType, exceptions.get(entityType));
                continue;
            }

            Class<? extends Entity> clazz = entityType.getEntityClass();
            if (clazz == null)
                continue;

            if (Monster.class.isAssignableFrom(clazz)) {
                defaultSpawnConditions.put(entityType, new SpawnConditions(ImmutableSet.of(Material.AIR), LightLevel.DARK));
            } else if (Animals.class.isAssignableFrom(clazz)) {
                defaultSpawnConditions.put(entityType, new SpawnConditions(ImmutableSet.of(Material.GRASS_BLOCK), LightLevel.LIGHT));
            } else if (WaterMob.class.isAssignableFrom(clazz)) {
                defaultSpawnConditions.put(entityType, new SpawnConditions(ImmutableSet.of(Material.WATER), LightLevel.ANY));
            } else if (NPC.class.isAssignableFrom(clazz)) {
                defaultSpawnConditions.put(entityType, new SpawnConditions(ImmutableSet.of(Material.AIR), LightLevel.ANY));
            } else if (Flying.class.isAssignableFrom(clazz)) {
                defaultSpawnConditions.put(entityType, new SpawnConditions(ImmutableSet.of(Material.AIR), LightLevel.ANY));
            } else {
                defaultSpawnConditions.put(entityType, new SpawnConditions(ImmutableSet.of(Material.AIR), LightLevel.ANY));
            }
        }
    }

    private EntityType entityType;
    private boolean enabled;
    private String displayName;
    private boolean disableMobAI;
    private SpawnConditions spawnConditions;
    private int spawnCountStackSizeMultiplier;
    private int minSpawnDelay;
    private int maxSpawnDelay;
    private int maxNearbyEntities;
    private int playerActivationRange;
    private int spawnRange;

    public SpawnerStackSettings(CommentedFileConfiguration settingsConfiguration, EntityType entityType) {
        super(settingsConfiguration);
        this.entityType = entityType;
        this.setDefaults();

        this.enabled = this.settingsConfiguration.getBoolean("enabled");
        this.displayName = this.settingsConfiguration.getString("display-name");
        this.disableMobAI = this.settingsConfiguration.getBoolean("disable-mob-ai");
        this.spawnCountStackSizeMultiplier = this.settingsConfiguration.getInt("spawn-count-stack-size-multiplier");
        this.minSpawnDelay = this.settingsConfiguration.getInt("spawn-delay-minimum");
        this.maxSpawnDelay = this.settingsConfiguration.getInt("spawn-delay-maximum");
        this.maxNearbyEntities = this.settingsConfiguration.getInt("max-nearby-entities");
        this.playerActivationRange = this.settingsConfiguration.getInt("player-activation-range");
        this.spawnRange = this.settingsConfiguration.getInt("spawn-range");

        List<String> spawnBlockString = this.settingsConfiguration.getStringList("spawn-blocks");
        Set<Material> spawnBlocks = spawnBlockString.stream().map(Material::getMaterial).filter(Objects::nonNull).collect(Collectors.toSet());

        String requiredLightLevelString = this.settingsConfiguration.getString("required-light-level");
        if (requiredLightLevelString == null)
            requiredLightLevelString = LightLevel.ANY.name();
        LightLevel requiredLightLevel = LightLevel.getLightLevel(requiredLightLevelString);

        this.spawnConditions = new SpawnConditions(spawnBlocks, requiredLightLevel);
    }

    @Override
    public boolean canStackWith(StackedSpawner stack1, StackedSpawner stack2, boolean comparingForUnstack) {
        if (!this.enabled)
            return false;

        return true;
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        this.setIfNotExists("enabled", true);
        this.setIfNotExists("display-name", StackerUtils.formatName(this.entityType.name() + '_' + Material.SPAWNER.name()));
        this.setIfNotExists("disable-mob-ai", false);
        this.setIfNotExists("spawn-count-stack-size-multiplier", -1);
        this.setIfNotExists("spawn-delay-minimum", -1);
        this.setIfNotExists("spawn-delay-maximum", -1);
        this.setIfNotExists("max-nearby-entities", -1);
        this.setIfNotExists("player-activation-range", -1);
        this.setIfNotExists("spawn-range", -1);

        SpawnConditions defaults = defaultSpawnConditions.get(this.entityType);
        this.setIfNotExists("spawn-blocks", defaults.getSpawnBlocks().stream().map(Enum::name).collect(Collectors.toList()));
        this.setIfNotExists("required-light-level", defaults.getRequiredLightLevel().name());
    }

    @Override
    protected String getConfigurationSectionKey() {
        return this.entityType.name();
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public boolean isStackingEnabled() {
        return this.enabled;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean isMobAIDisabled() {
        return this.disableMobAI;
    }

    public SpawnConditions getSpawnConditions() {
        return this.spawnConditions;
    }

    public int getSpawnCountStackSizeMultiplier() {
        if (this.spawnCountStackSizeMultiplier != -1)
            return Math.max(this.spawnCountStackSizeMultiplier, 1);
        return Math.max(Setting.SPAWNER_SPAWN_COUNT_STACK_SIZE_MULTIPLIER.getInt(), 1);
    }

    public int getMinSpawnDelay() {
        if (this.minSpawnDelay != -1)
            return Math.max(this.minSpawnDelay, 5);
        return Math.max(Setting.SPAWNER_SPAWN_DELAY_MINIMUM.getInt(), 5);
    }

    public int getMaxSpawnDelay() {
        if (this.maxSpawnDelay != -1)
            return Math.max(this.maxSpawnDelay, this.getMinSpawnDelay());
        return Math.max(Setting.SPAWNER_SPAWN_DELAY_MAXIMUM.getInt(), this.getMinSpawnDelay());
    }

    public int getMaxNearbyEntities() {
        if (this.maxNearbyEntities != -1)
            return Math.max(this.maxNearbyEntities, 1);
        return Math.max(Setting.SPAWNER_SPAWN_MAX_NEARBY_ENTITIES.getInt(), 1);
    }

    public int getPlayerActivationRange() {
        if (this.playerActivationRange != -1)
            return Math.max(this.playerActivationRange, 1);
        return Math.max(Setting.SPAWNER_SPAWN_PLAYER_ACTIVATION_RANGE.getInt(), 1);
    }

    public int getSpawnRange() {
        if (this.spawnRange != -1)
            return Math.max(this.spawnRange, 1);
        return Math.max(Setting.SPAWNER_SPAWN_RANGE.getInt(), 1);
    }

    public static class SpawnConditions {

        private Set<Material> spawnBlocks;
        private LightLevel requiredLightLevel;

        private SpawnConditions(Set<Material> spawnBlocks, LightLevel requiredLightLevel) {
            this.spawnBlocks = spawnBlocks;
            this.requiredLightLevel = requiredLightLevel;
        }

        public Set<Material> getSpawnBlocks() {
            return this.spawnBlocks;
        }

        public LightLevel getRequiredLightLevel() {
            return this.requiredLightLevel;
        }

    }

    public enum LightLevel {
        LIGHT,
        DARK,
        ANY;

        private static LightLevel getLightLevel(String string) {
            for (LightLevel lightLevel : LightLevel.values())
                if (lightLevel.name().equalsIgnoreCase(string))
                    return lightLevel;
            return null;
        }
    }

}
