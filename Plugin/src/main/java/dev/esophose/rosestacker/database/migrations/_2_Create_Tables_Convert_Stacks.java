package dev.esophose.rosestacker.database.migrations;

import dev.esophose.rosestacker.database.DataMigration;
import dev.esophose.rosestacker.database.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _2_Create_Tables_Convert_Stacks extends DataMigration {

    public _2_Create_Tables_Convert_Stacks() {
        super(2);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection, String tablePrefix) throws SQLException {
        // Create active conversion handler table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "convert_handler (" +
                    "name VARCHAR(50) NOT NULL, " +
                    "UNIQUE (name)" +
                    ")");
        }

        // Create StackedEntity table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "convert_stacked_entity (" +
                    "entity_uuid VARCHAR(36) NOT NULL, " +
                    "stack_size INTEGER NOT NULL, " +
                    "UNIQUE (entity_uuid)" +
                    ")");
        }

        // Create StackedItem table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "convert_stacked_item (" +
                    "entity_uuid VARCHAR(36) NOT NULL, " +
                    "stack_size INTEGER NOT NULL, " +
                    "UNIQUE (entity_uuid)" +
                    ")");
        }
    }

}

