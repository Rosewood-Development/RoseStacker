package dev.esophose.rosestacker.database.migrations;

import dev.esophose.rosestacker.database.DataMigration;
import dev.esophose.rosestacker.database.DatabaseConnector;
import dev.esophose.rosestacker.database.MySQLConnector;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _1_InitialMigration extends DataMigration {

    public _1_InitialMigration() {
        super(1);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection, String tablePrefix) throws SQLException {
        String autoIncrement = connector instanceof MySQLConnector ? " AUTO_INCREMENT" : "";

        // Create StackedBlock table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "stacked_block (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "stack_size INTEGER, " +
                    "chunk_x INTEGER, " +
                    "chunk_z INTEGER, " +
                    "block_x INTEGER, " +
                    "block_y INTEGER, " +
                    "block_z INTEGER" +
                    ")");
        }

        // Create StackedEntity table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "stacked_entity (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "stack_size INTEGER, " +
                    "stack_entities TEXT, " +
                    "chunk_x INTEGER, " +
                    "chunk_z INTEGER, " +
                    "entity_uuid VARCHAR(36), " +
                    ")");
        }

        // Create StackedItem table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "stacked_item (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "stack_size INTEGER, " +
                    "chunk_x INTEGER, " +
                    "chunk_z INTEGER, " +
                    "entity_uuid VARCHAR(36), " +
                    ")");
        }

        // Create StackedSpawner table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "stacked_spawner (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "stack_size INTEGER, " +
                    "chunk_x INTEGER, " +
                    "chunk_z INTEGER, " +
                    "block_x INTEGER, " +
                    "block_y INTEGER, " +
                    "block_z INTEGER" +
                    ")");
        }

    }

}

