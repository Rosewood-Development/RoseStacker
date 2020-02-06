package dev.esophose.sparkstacker.stack.settings;

import com.google.common.collect.ImmutableSet;
import dev.esophose.sparkstacker.config.CommentedFileConfiguration;
import dev.esophose.sparkstacker.stack.StackedSpawner;
import dev.esophose.sparkstacker.utils.StackerUtils;
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


import static org.bukkit.Material.ACACIA_LEAVES;
import static org.bukkit.Material.AIR;
import static org.bukkit.Material.BIRCH_LEAVES;
import static org.bukkit.Material.DARK_OAK_LEAVES;
import static org.bukkit.Material.GRASS_BLOCK;
import static org.bukkit.Material.JUNGLE_LEAVES;
import static org.bukkit.Material.MYCELIUM;
import static org.bukkit.Material.OAK_LEAVES;
import static org.bukkit.Material.SPRUCE_LEAVES;
import static org.bukkit.Material.WATER;

public class SpawnerStackSettings extends StackSettings<StackedSpawner> {

    private static Map<EntityType, SpawnConditions> defaultSpawnConditions;

    static {
        Map<EntityType, SpawnConditions> exceptions = new HashMap<>();
        exceptions.put(EntityType.BAT, new SpawnConditions(ImmutableSet.of(AIR), LightLevel.DARK));
        exceptions.put(EntityType.ELDER_GUARDIAN, new SpawnConditions(ImmutableSet.of(WATER), LightLevel.DARK));
        exceptions.put(EntityType.GUARDIAN, new SpawnConditions(ImmutableSet.of(WATER), LightLevel.DARK));
        exceptions.put(EntityType.MAGMA_CUBE, new SpawnConditions(ImmutableSet.of(AIR), LightLevel.ANY));
        exceptions.put(EntityType.MUSHROOM_COW, new SpawnConditions(ImmutableSet.of(MYCELIUM), LightLevel.ANY));
        exceptions.put(EntityType.OCELOT, new SpawnConditions(ImmutableSet.of(GRASS_BLOCK, ACACIA_LEAVES, BIRCH_LEAVES, DARK_OAK_LEAVES, JUNGLE_LEAVES, OAK_LEAVES, SPRUCE_LEAVES), LightLevel.LIGHT));
        exceptions.put(EntityType.PARROT, new SpawnConditions(ImmutableSet.of(AIR), LightLevel.ANY));
        exceptions.put(EntityType.SKELETON_HORSE, new SpawnConditions(ImmutableSet.of(AIR), LightLevel.DARK));
        exceptions.put(EntityType.TURTLE, new SpawnConditions(ImmutableSet.of(AIR), LightLevel.ANY));
        exceptions.put(EntityType.WOLF, new SpawnConditions(ImmutableSet.of(AIR), LightLevel.ANY));
        exceptions.put(EntityType.ZOMBIE_HORSE, new SpawnConditions(ImmutableSet.of(AIR), LightLevel.DARK));

        defaultSpawnConditions = new HashMap<>();
        for (EntityType entityType : StackerUtils.getStackableEntityTypes()) {
            if (exceptions.containsKey(entityType)) {
                defaultSpawnConditions.put(entityType, exceptions.get(entityType));
                continue;
            }

            Class<? extends Entity> clazz = entityType.getEntityClass();
            if (clazz == null)
                continue;

            if (Monster.class.isAssignableFrom(clazz)) {
                defaultSpawnConditions.put(entityType, new SpawnConditions(ImmutableSet.of(AIR), LightLevel.DARK));
            } else if (Animals.class.isAssignableFrom(clazz)) {
                defaultSpawnConditions.put(entityType, new SpawnConditions(ImmutableSet.of(GRASS_BLOCK), LightLevel.LIGHT));
            } else if (WaterMob.class.isAssignableFrom(clazz)) {
                defaultSpawnConditions.put(entityType, new SpawnConditions(ImmutableSet.of(WATER), LightLevel.ANY));
            } else if (NPC.class.isAssignableFrom(clazz)) {
                defaultSpawnConditions.put(entityType, new SpawnConditions(ImmutableSet.of(AIR), LightLevel.ANY));
            } else if (Flying.class.isAssignableFrom(clazz)) {
                defaultSpawnConditions.put(entityType, new SpawnConditions(ImmutableSet.of(AIR), LightLevel.ANY));
            } else {
                defaultSpawnConditions.put(entityType, new SpawnConditions(ImmutableSet.of(AIR), LightLevel.ANY));
            }
        }
    }

    private EntityType entityType;
    private boolean enabled;
    private String displayName;
    private boolean disableMobAI;
    private SpawnConditions spawnConditions;

    public SpawnerStackSettings(CommentedFileConfiguration settingsConfiguration, EntityType entityType) {
        super(settingsConfiguration);
        this.entityType = entityType;
        this.setDefaults();

        this.enabled = this.settingsConfiguration.getBoolean("enabled");
        this.displayName = this.settingsConfiguration.getString("display-name");
        this.disableMobAI = this.settingsConfiguration.getBoolean("disable-mob-ai");

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
