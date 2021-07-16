package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.database.SQLiteConnector;
import dev.rosewood.rosegarden.manager.AbstractDataManager;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.conversion.ConverterType;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedItem;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.EntitySerializer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;

public class DataManager extends AbstractDataManager {

    public DataManager(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    public void getStackedEntities(Collection<Chunk> chunks, Map<UUID, Entity> chunkEntities, String where, Consumer<Set<StackedEntity>> callback) {
        if (chunks.isEmpty())
            callback.accept(Collections.emptySet());

        String query = "SELECT * FROM " + this.getTablePrefix() + "stacked_entity WHERE " + where;
        Set<StackedEntityData> stackedEntityData = new HashSet<>();
        this.databaseConnector.connect(connection -> {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    stackedEntityData.add(new StackedEntityData(
                            result.getInt("id"),
                            UUID.fromString(result.getString("entity_uuid")),
                            result.getBytes("stack_entities")
                    ));
                }
            }
        });

        Runnable task = () -> {
            Set<StackedEntity> stackedEntities = new HashSet<>();
            for (StackedEntityData stackData : stackedEntityData) {
                Entity entity = chunkEntities.get(stackData.entityUUID);
                if (entity != null)
                    stackedEntities.add(EntitySerializer.fromBlob((LivingEntity) entity, stackData.stackEntities));
            }

            callback.accept(stackedEntities);
        };

        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(this.rosePlugin, task);
        }
    }

    public void getStackedItems(Collection<Chunk> chunks, Map<UUID, Entity> chunkEntities, String where, Consumer<Set<StackedItem>> callback) {
        if (chunks.isEmpty())
            callback.accept(Collections.emptySet());

        String query = "SELECT * FROM " + this.getTablePrefix() + "stacked_item WHERE " + where;
        Set<StackedItemData> stackedItemData = new HashSet<>();
        this.databaseConnector.connect(connection -> {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    stackedItemData.add(new StackedItemData(
                            result.getInt("id"),
                            result.getInt("stack_size"),
                            UUID.fromString(result.getString("entity_uuid"))
                    ));
                }
            }
        });

        Runnable task = () -> {
            Set<StackedItem> stackedItems = new HashSet<>();
            for (StackedItemData stackData : stackedItemData) {
                Entity entity = chunkEntities.get(stackData.entityUUID);
                if (entity != null)
                    stackedItems.add(new StackedItem(stackData.stackSize, (Item) entity));
            }

            callback.accept(stackedItems);
        };

        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(this.rosePlugin, task);
        }
    }

    public void getStackedBlocks(Collection<Chunk> chunks, String where, Consumer<Set<StackedBlock>> callback) {
        if (chunks.isEmpty())
            callback.accept(Collections.emptySet());

        String query = "SELECT * FROM " + this.getTablePrefix() + "stacked_block WHERE " + where;
        Set<StackedBlockData> stackedBlockData = new HashSet<>();
        this.databaseConnector.connect(connection -> {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    stackedBlockData.add(new StackedBlockData(
                            result.getInt("id"),
                            result.getInt("stack_size"),
                            result.getInt("chunk_x"),
                            result.getInt("chunk_z"),
                            result.getInt("block_x"),
                            result.getInt("block_y"),
                            result.getInt("block_z"),
                            result.getString("world")
                    ));
                }
            }
        });

        Runnable task = () -> {
            Set<StackedBlock> stackedBlocks = new HashSet<>();
            for (StackedBlockData stackData : stackedBlockData) {
                World world = Bukkit.getWorld(stackData.world);
                Block block = null;

                boolean invalid = world == null;
                if (!invalid) {
                    block = world.getBlockAt((stackData.chunkX << 4) + stackData.blockX, stackData.blockY, (stackData.chunkZ << 4) + stackData.blockZ);
                    if (block.getType() == Material.AIR)
                        invalid = true;
                }

                if (!invalid)
                    stackedBlocks.add(new StackedBlock(stackData.stackSize, block));
            }

            callback.accept(stackedBlocks);
        };

        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(this.rosePlugin, task);
        }
    }

    public void getStackedSpawners(Collection<Chunk> chunks, String where, Consumer<Set<StackedSpawner>> callback) {
        if (chunks.isEmpty())
            callback.accept(Collections.emptySet());

        String query = "SELECT * FROM " + this.getTablePrefix() + "stacked_spawner WHERE " + where;
        Set<StackedSpawnerData> stackedSpawnerData = new HashSet<>();
        this.databaseConnector.connect(connection -> {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    stackedSpawnerData.add(new StackedSpawnerData(
                            result.getInt("id"),
                            result.getInt("stack_size"),
                            result.getInt("chunk_x"),
                            result.getInt("chunk_z"),
                            result.getInt("block_x"),
                            result.getInt("block_y"),
                            result.getInt("block_z"),
                            result.getString("world"),
                            result.getBoolean("placed_by_player")
                    ));
                }
            }
        });

        Runnable task = () -> {
            Set<StackedSpawner> stackedSpawners = new HashSet<>();
            for (StackedSpawnerData stackData : stackedSpawnerData) {
                World world = Bukkit.getWorld(stackData.world);
                Block block = null;

                boolean invalid = world == null;
                if (!invalid) {
                    block = world.getBlockAt((stackData.chunkX << 4) + stackData.blockX, stackData.blockY, (stackData.chunkZ << 4) + stackData.blockZ);
                    if (block.getType() != Material.SPAWNER)
                        invalid = true;
                }

                if (!invalid)
                    stackedSpawners.add(new StackedSpawner(stackData.stackSize, (CreatureSpawner) block.getState(), stackData.placedByPlayer));
            }

            callback.accept(stackedSpawners);
        };

        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(this.rosePlugin, task);
        }
    }

    public void setConversionHandlers(Set<ConverterType> converterTypes) {
        this.databaseConnector.connect(connection -> {
            String insertIgnore;
            if (this.databaseConnector instanceof SQLiteConnector) {
                insertIgnore = "INSERT OR IGNORE INTO ";
            } else {
                insertIgnore = "INSERT IGNORE ";
            }

            String query = insertIgnore + this.getTablePrefix() + "convert_handler (name) VALUES (?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (ConverterType converterType : converterTypes) {
                    statement.setString(1, converterType.name());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        });
    }

    public Set<ConverterType> getConversionHandlers() {
        Set<ConverterType> conversionHandlers = EnumSet.noneOf(ConverterType.class);
        this.databaseConnector.connect(connection -> {
            String query = "SELECT name FROM " + this.getTablePrefix() + "convert_handler";
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    ConverterType converterType = ConverterType.get(result.getString(1));
                    if (converterType != null)
                        conversionHandlers.add(converterType);
                }
            }
        });
        return conversionHandlers;
    }

    public void setConversionData(Map<StackType, Set<ConversionData>> conversionData) {
        if (conversionData.isEmpty())
            return;

        this.databaseConnector.connect(connection -> {
            Set<ConversionData> entityData = conversionData.get(StackType.ENTITY);
            if (entityData != null && !entityData.isEmpty()) {
                String entityInsert = "INSERT INTO " + this.getTablePrefix() + "convert_stacked_entity (entity_uuid, stack_size) VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(entityInsert)) {
                    for (ConversionData data : entityData) {
                        statement.setString(1, data.getUniqueId().toString());
                        statement.setInt(2, data.getStackSize());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }

            Set<ConversionData> itemData = conversionData.get(StackType.ITEM);
            if (itemData != null && !itemData.isEmpty()) {
                String itemInsert = "INSERT INTO " + this.getTablePrefix() + "convert_stacked_item (entity_uuid, stack_size) VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(itemInsert)) {
                    for (ConversionData data : itemData) {
                        statement.setString(1, data.getUniqueId().toString());
                        statement.setInt(2, data.getStackSize());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
        });
    }

    public Map<StackType, Set<ConversionData>> getConversionData(List<Entity> entities, Set<StackType> requiredStackTypes) {
        Map<StackType, Set<ConversionData>> conversionData = new HashMap<>();
        if (requiredStackTypes.isEmpty())
            return conversionData;

        this.databaseConnector.connect(connection -> {
            if (requiredStackTypes.contains(StackType.ENTITY))
                conversionData.put(StackType.ENTITY, this.getConversionData(entities, "entity", connection));

            if (requiredStackTypes.contains(StackType.ITEM))
                conversionData.put(StackType.ITEM, this.getConversionData(entities, "item", connection));
        });

        return conversionData;
    }

    private Set<ConversionData> getConversionData(List<Entity> entities, String tableName, Connection connection) throws SQLException {
        Set<ConversionData> conversionData = new HashSet<>();

        Map<UUID, Entity> entityMap = new HashMap<>();
        for (Entity entity : entities)
            entityMap.put(entity.getUniqueId(), entity);

        if (entityMap.isEmpty())
            return conversionData;

        String entityUniqueIdsString = entityMap.keySet().stream().map(UUID::toString).map(x -> "'" + x + "'").collect(Collectors.joining(","));

        // Get data
        try (Statement statement = connection.createStatement()) {
            String query = "SELECT entity_uuid, stack_size FROM " + this.getTablePrefix() + "convert_stacked_" + tableName + " WHERE entity_uuid IN (" + entityUniqueIdsString + ")";
            ResultSet result = statement.executeQuery(query);
            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString("entity_uuid"));
                Entity entity = entityMap.get(uuid);
                int stackSize = result.getInt("stack_size");
                conversionData.add(new ConversionData(entity, stackSize));
            }
        }

        // Delete data
        try (Statement statement = connection.createStatement()) {
            String query = "DELETE FROM " + this.getTablePrefix() + "convert_stacked_" + tableName + " WHERE entity_uuid IN (" + entityUniqueIdsString + ")";
            statement.executeUpdate(query);
        }

        return conversionData;
    }

    public List<String> getTranslationLocales(String requiredVersion) {
        List<String> locales = new ArrayList<>();

        this.databaseConnector.connect(connection -> {
            String delete = "DELETE FROM " + this.getTablePrefix() + "translation_locale WHERE version != ?";
            try (PreparedStatement statement = connection.prepareStatement(delete)) {
                statement.setString(1, requiredVersion);
                statement.executeUpdate();
            }

            String query = "SELECT name FROM " + this.getTablePrefix() + "translation_locale";
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(query);
                while (result.next())
                    locales.add(result.getString("name"));
            }
        });

        locales.sort(String::compareTo);

        return locales;
    }

    public void saveTranslationLocales(String version, List<String> locales) {
        this.databaseConnector.connect(connection -> {
            String insertQuery = "INSERT INTO " + this.getTablePrefix() + "translation_locale (version, name) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                for (String locale : locales) {
                    statement.setString(1, version);
                    statement.setString(2, locale);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        });
    }

    private static class StackedBlockData {
        public int id;
        public int stackSize;
        public int chunkX, chunkZ;
        public int blockX, blockY, blockZ;
        public String world;

        public StackedBlockData(int id, int stackSize, int chunkX, int chunkZ, int blockX, int blockY, int blockZ, String world) {
            this.id = id;
            this.stackSize = stackSize;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.blockX = blockX;
            this.blockY = blockY;
            this.blockZ = blockZ;
            this.world = world;
        }
    }

    private static class StackedSpawnerData extends StackedBlockData {
        public boolean placedByPlayer;

        public StackedSpawnerData(int id, int stackSize, int chunkX, int chunkZ, int blockX, int blockY, int blockZ, String world, boolean placedByPlayer) {
            super(id, stackSize, chunkX, chunkZ, blockX, blockY, blockZ, world);
            this.placedByPlayer = placedByPlayer;
        }
    }

    private static class StackedEntityData {
        public int id;
        public UUID entityUUID;
        public byte[] stackEntities;

        public StackedEntityData(int id, UUID entityUUID, byte[] stackEntities) {
            this.id = id;
            this.entityUUID = entityUUID;
            this.stackEntities = stackEntities;
        }
    }

    private static class StackedItemData {
        public int id;
        public int stackSize;
        public UUID entityUUID;

        public StackedItemData(int id, int stackSize, UUID entityUUID) {
            this.id = id;
            this.stackSize = stackSize;
            this.entityUUID = entityUUID;
        }
    }

}
