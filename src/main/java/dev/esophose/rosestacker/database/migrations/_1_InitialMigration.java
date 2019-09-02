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
                    "stack_size INTEGER NOT NULL, " +
                    "world TEXT NOT NULL, " +
                    "chunk_x INTEGER NOT NULL, " +
                    "chunk_z INTEGER NOT NULL, " +
                    "block_x INTEGER NOT NULL, " +
                    "block_y INTEGER NOT NULL, " +
                    "block_z INTEGER NOT NULL" +
                    ")");
        }

        // Create StackedEntity table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "stacked_entity (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "entity_uuid VARCHAR(36) NOT NULL, " +
                    "stack_entities TEXT NOT NULL, " +
                    "world TEXT NOT NULL, " +
                    "chunk_x INTEGER NOT NULL, " +
                    "chunk_z INTEGER NOT NULL" +
                    ")");
        }

        // Create StackedItem table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "stacked_item (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "stack_size INTEGER NOT NULL, " +
                    "entity_uuid VARCHAR(36) NOT NULL, " +
                    "world TEXT NOT NULL, " +
                    "chunk_x INTEGER NOT NULL, " +
                    "chunk_z INTEGER NOT NULL" +
                    ")");
        }

        // Create StackedSpawner table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "stacked_spawner (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "stack_size INTEGER NOT NULL, " +
                    "world TEXT NOT NULL, " +
                    "chunk_x INTEGER NOT NULL, " +
                    "chunk_z INTEGER NOT NULL, " +
                    "block_x INTEGER NOT NULL, " +
                    "block_y INTEGER NOT NULL, " +
                    "block_z INTEGER NOT NULL" +
                    ")");
        }

    }

}

