package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.database.DataMigration;
import dev.rosewood.rosegarden.database.SQLiteConnector;
import dev.rosewood.rosegarden.manager.AbstractDataManager;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.conversion.ConverterType;
import dev.rosewood.rosestacker.database.migrations._1_Create_Tables_Stacks;
import dev.rosewood.rosestacker.database.migrations._2_Create_Tables_Convert_Stacks;
import dev.rosewood.rosestacker.database.migrations._3_Create_Tables_Translation_Locales;
import dev.rosewood.rosestacker.database.migrations._4_Alter_Spawner_Table_Player_Placed;
import dev.rosewood.rosestacker.stack.StackType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.entity.Entity;

public class DataManager extends AbstractDataManager {

    public DataManager(RosePlugin rosePlugin) {
        super(rosePlugin);
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

    @Override
    public List<Class<? extends DataMigration>> getDataMigrations() {
        return List.of(
                _1_Create_Tables_Stacks.class,
                _2_Create_Tables_Convert_Stacks.class,
                _3_Create_Tables_Translation_Locales.class,
                _4_Alter_Spawner_Table_Player_Placed.class
        );
    }

}
