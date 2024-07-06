package dev.rosewood.rosestacker.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.spawner.SpawnerType;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.ItemStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.ConditionTags;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

public class StackSettingManager extends Manager {

    private final Map<Material, BlockStackSettings> blockSettings;
    private final Map<EntityType, EntityStackSettings> entitySettings;
    private final Map<Material, ItemStackSettings> itemSettings;
    private final Map<SpawnerType, SpawnerStackSettings> spawnerSettings;

    private boolean registeredPermissions = false;

    public StackSettingManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.blockSettings = new LinkedHashMap<>();
        this.entitySettings = new LinkedHashMap<>();
        this.itemSettings = new LinkedHashMap<>();
        this.spawnerSettings = new LinkedHashMap<>();
    }

    @Override
    public void reload() {
        // Settings files
        File blockSettingsFile = this.getBlockSettingsFile();
        File entitySettingsFile = this.getEntitySettingsFile();
        File itemSettingsFile = this.getItemSettingsFile();
        File spawnerSettingsFile = this.getSpawnerSettingsFile();

        // Flags for if we should save the files
        AtomicBoolean saveBlockSettingsFile = new AtomicBoolean(false);
        AtomicBoolean saveEntitySettingsFile = new AtomicBoolean(false);
        AtomicBoolean saveItemSettingsFile = new AtomicBoolean(false);
        AtomicBoolean saveSpawnerSettingsFile = new AtomicBoolean(false);

        // Load block settings
        CommentedFileConfiguration blockSettingsConfiguration = CommentedFileConfiguration.loadConfiguration(blockSettingsFile);
        StackerUtils.getPossibleStackableBlockMaterials().forEach(x -> {
            BlockStackSettings blockStackSettings = new BlockStackSettings(blockSettingsConfiguration, x);
            this.blockSettings.put(x, blockStackSettings);
            if (blockStackSettings.hasChanges())
                saveBlockSettingsFile.set(true);
        });

        // Load entity settings and data from entity_data.json
        CommentedFileConfiguration entitySettingsConfiguration = CommentedFileConfiguration.loadConfiguration(entitySettingsFile);
        try (InputStream entityDataStream = this.getClass().getResourceAsStream("/entity_data.json");
             Reader entityDataReader = new InputStreamReader(entityDataStream)) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(entityDataReader).getAsJsonObject();

            Set<String> invalidKeys = new TreeSet<>();
            TreeSet<Map.Entry<String, JsonElement>> entries = new TreeSet<>(Entry.comparingByKey());
            entries.addAll(jsonObject.entrySet());
            entries.forEach(entry -> {
                String name = entry.getKey();
                EntityType entityType;
                try {
                    entityType = EntityType.valueOf(name);
                } catch (Exception e) {
                    JsonObject entityObject = entry.getValue().getAsJsonObject();
                    if (entityObject.has("alternate_id")) {
                        String alternateId = entityObject.get("alternate_id").getAsString();
                        try {
                            entityType = EntityType.valueOf(alternateId);
                        } catch (Exception e2) {
                            invalidKeys.add(name);
                            return;
                        }
                    } else {
                        invalidKeys.add(name);
                        return;
                    }
                }

                EntityStackSettings entityStackSetting = new EntityStackSettings(entitySettingsConfiguration, entry.getValue().getAsJsonObject(), entityType);
                this.entitySettings.put(entityType, entityStackSetting);
                if (entityStackSetting.hasChanges())
                    saveEntitySettingsFile.set(true);
            });

            if (!invalidKeys.isEmpty())
                this.rosePlugin.getLogger().warning("Ignored loading stack settings for entities: " + invalidKeys);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load item settings
        CommentedFileConfiguration itemSettingsConfiguration = CommentedFileConfiguration.loadConfiguration(itemSettingsFile);
        Stream.of(Material.values()).sorted(Comparator.comparing(Enum::name)).forEach(x -> {
            ItemStackSettings itemStackSettings = new ItemStackSettings(itemSettingsConfiguration, x);
            this.itemSettings.put(x, itemStackSettings);
            if (itemStackSettings.hasChanges())
                saveItemSettingsFile.set(true);
        });

        // Load spawner settings
        boolean addSpawnerHeaderComments = !spawnerSettingsFile.exists();
        CommentedFileConfiguration spawnerSettingsConfiguration = CommentedFileConfiguration.loadConfiguration(spawnerSettingsFile);
        if (addSpawnerHeaderComments) {
            saveSpawnerSettingsFile.set(true);
            Map<String, String> conditionTags = ConditionTags.getTagDescriptionMap();
            spawnerSettingsConfiguration.addComments("Available Spawn Requirements:", "");
            for (Entry<String, String> entry : conditionTags.entrySet()) {
                String tag = entry.getKey();
                String description = entry.getValue();
                spawnerSettingsConfiguration.addComments(tag + " - " + description);
            }

            spawnerSettingsConfiguration.addComments(
                    "",
                    "Valid Blocks: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html",
                    "Valid Biomes: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Biome.html",
                    "",
                    "Want to remove all requirements? Set the value to the following:",
                    "spawn-requirements: []"
            );
        }

        TreeSet<SpawnerType> spawnerTypes = new TreeSet<>(Comparator.comparing(SpawnerType::getEnumName));
        if (NMSAdapter.getHandler().supportsEmptySpawners())
            spawnerTypes.add(SpawnerType.empty());
        this.entitySettings.keySet().forEach(x -> spawnerTypes.add(SpawnerType.of(x)));
        spawnerTypes.forEach(spawnerType -> {
            SpawnerStackSettings spawnerStackSettings = new SpawnerStackSettings(spawnerSettingsConfiguration, spawnerType);
            this.spawnerSettings.put(spawnerType, spawnerStackSettings);
            if (spawnerStackSettings.hasChanges())
                saveSpawnerSettingsFile.set(true);
        });

        // Save files if changes were made
        if (saveBlockSettingsFile.get())
            blockSettingsConfiguration.save(blockSettingsFile, true);
        if (saveEntitySettingsFile.get())
            entitySettingsConfiguration.save(entitySettingsFile, true);
        if (saveItemSettingsFile.get())
            itemSettingsConfiguration.save(itemSettingsFile, true);
        if (saveSpawnerSettingsFile.get())
            spawnerSettingsConfiguration.save(spawnerSettingsFile, true);

        // Register dynamic permissions on first load
        if (!this.registeredPermissions) {
            PluginManager pluginManager = Bukkit.getPluginManager();

            List<Permission> silktouch = new ArrayList<>();
            List<Permission> nosilk = new ArrayList<>();
            List<Permission> spawnerplace = new ArrayList<>();

            for (EntityType entityType : this.entitySettings.keySet()) {
                String type = entityType.name().toLowerCase();
                silktouch.add(new Permission("rosestacker.silktouch." + type));
                nosilk.add(new Permission("rosestacker.nosilk." + type));
                spawnerplace.add(new Permission("rosestacker.spawnerplace." + type));
            }

            // Register silktouch permissions
            silktouch.forEach(pluginManager::addPermission);
            pluginManager.addPermission(new Permission("rosestacker.silktouch.*", silktouch.stream().collect(Collectors.toMap(Permission::getName, x -> true))));

            // Register nosilk permissions
            nosilk.forEach(pluginManager::addPermission);
            pluginManager.addPermission(new Permission("rosestacker.nosilk.*", nosilk.stream().collect(Collectors.toMap(Permission::getName, x -> true))));

            // Register spawnerplace permissions
            spawnerplace.forEach(pluginManager::addPermission);
            pluginManager.addPermission(new Permission("rosestacker.spawnerplace.*", spawnerplace.stream().collect(Collectors.toMap(Permission::getName, x -> true))));

            this.registeredPermissions = true;
        }
    }

    @Override
    public void disable() {
        this.blockSettings.clear();
        this.entitySettings.clear();
        this.itemSettings.clear();
        this.spawnerSettings.clear();
    }

    public File getBlockSettingsFile() {
        return new File(this.rosePlugin.getDataFolder(), "block_settings.yml");
    }

    public File getEntitySettingsFile() {
        return new File(this.rosePlugin.getDataFolder(), "entity_settings.yml");
    }

    public File getItemSettingsFile() {
        return new File(this.rosePlugin.getDataFolder(), "item_settings.yml");
    }

    public File getSpawnerSettingsFile() {
        return new File(this.rosePlugin.getDataFolder(), "spawner_settings.yml");
    }

    /**
     * Gets the BlockStackSettings for a block type
     *
     * @param material The block material to get the settings of
     * @return The BlockStackSettings for the block type, or null if the block type is not stackable
     */
    public BlockStackSettings getBlockStackSettings(Material material) {
        return this.blockSettings.get(material);
    }

    /**
     * Gets the BlockStackSettings for a block
     *
     * @param block The block to get the settings of
     * @return The BlockStackSettings for the block, or null if the block type is not stackable
     */
    public BlockStackSettings getBlockStackSettings(Block block) {
        return this.getBlockStackSettings(block.getType());
    }

    /**
     * Gets the EntityStackSettings for an entity type
     *
     * @param entityType The entity type to get the settings of
     * @return The EntityStackSettings for the entity type
     */
    public EntityStackSettings getEntityStackSettings(EntityType entityType) {
        return this.entitySettings.get(entityType);
    }

    /**
     * Gets the EntityStackSettings for an entity
     *
     * @param entity The entity to get the settings of
     * @return The EntityStackSettings for the entity
     */
    public EntityStackSettings getEntityStackSettings(LivingEntity entity) {
        return this.getEntityStackSettings(entity.getType());
    }

    /**
     * Gets the EntityStackSettings for a spawn egg material
     *
     * @param material The spawn egg material to get the settings of
     * @return The EntityStackSettings for the spawn egg material, or null if the material is not a spawn egg
     */
    public EntityStackSettings getEntityStackSettings(Material material) {
        if (!ItemUtils.isSpawnEgg(material))
            return null;

        for (EntityStackSettings settings : this.entitySettings.values())
            if (settings.getEntityTypeData().spawnEggMaterial() == material)
                return settings;

        return null;
    }

    /**
     * Gets the ItemStackSettings for an item type
     *
     * @param material The item type to get the settings of
     * @return The ItemStackSettings for the item type
     */
    public ItemStackSettings getItemStackSettings(Material material) {
        return this.itemSettings.get(material);
    }

    /**
     * Gets the ItemStackSettings for an item
     *
     * @param item The item to get the settings of
     * @return The ItemStackSettings for the item
     */
    public ItemStackSettings getItemStackSettings(Item item) {
        return this.getItemStackSettings(item.getItemStack().getType());
    }

    /**
     * Gets the SpawnerStackSettings for a spawner entity type
     *
     * @param spawnerType The spawner type to get the settings of
     * @return The SpawnerStackSettings for the spawner entity type
     */
    public SpawnerStackSettings getSpawnerStackSettings(SpawnerType spawnerType) {
        return this.spawnerSettings.get(spawnerType);
    }

    /**
     * Gets the SpawnerStackSettings for a spawner entity type
     *
     * @param entityType The spawner entity type to get the settings of
     * @return The SpawnerStackSettings for the spawner entity type
     */
    public SpawnerStackSettings getSpawnerStackSettings(EntityType entityType) {
        return this.getSpawnerStackSettings(SpawnerType.of(entityType));
    }

    /**
     * Gets the SpawnerStackSettings for a spawner
     *
     * @param creatureSpawner The spawner to get the settings of
     * @return The SpawnerStackSettings for the spawner
     */
    public SpawnerStackSettings getSpawnerStackSettings(CreatureSpawner creatureSpawner) {
        return this.getSpawnerStackSettings(creatureSpawner.getSpawnedType());
    }

    public Set<EntityType> getStackableEntityTypes() {
        return this.entitySettings.values().stream()
                .filter(EntityStackSettings::isStackingEnabled)
                .map(EntityStackSettings::getEntityType)
                .collect(Collectors.toSet());
    }

    public Set<Material> getStackableItemTypes() {
        return this.itemSettings.values().stream()
                .filter(ItemStackSettings::isStackingEnabled)
                .map(ItemStackSettings::getType)
                .collect(Collectors.toSet());
    }

    public Set<Material> getStackableBlockTypes() {
        return this.blockSettings.values().stream()
                .filter(BlockStackSettings::isStackingEnabled)
                .map(BlockStackSettings::getType)
                .collect(Collectors.toSet());
    }

    public Set<SpawnerType> getStackableSpawnerTypes() {
        return this.spawnerSettings.values().stream()
                .filter(SpawnerStackSettings::isStackingEnabled)
                .map(SpawnerStackSettings::getSpawnerType)
                .collect(Collectors.toSet());
    }

}
