package dev.esophose.rosestacker.database.migrations;

import dev.esophose.rosestacker.database.DataMigration;
import dev.esophose.rosestacker.database.DatabaseConnector;
import dev.esophose.rosestacker.database.MySQLConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _1_Create_Tables_Stacks extends DataMigration {

    public _1_Create_Tables_Stacks() {
        super(1);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection, String tablePrefix) throws SQLException {
        String autoIncrement = connector instanceof MySQLConnector ? " AUTO_INCREMENT" : "";
        String blob = connector instanceof MySQLConnector ? "LONGBLOB" : "BLOB";

        // Create StackedEntity table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "stacked_entity (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "entity_uuid VARCHAR(36) NOT NULL, " +
                    "stack_entities " + blob + " NOT NULL, " +
                    "world VARCHAR(255) NOT NULL, " +
                    "chunk_x INTEGER NOT NULL, " +
                    "chunk_z INTEGER NOT NULL, " +
                    "UNIQUE (entity_uuid)" +
                    ")");
        }

        // Create StackedItem table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "stacked_item (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "entity_uuid VARCHAR(36) NOT NULL, " +
                    "stack_size INTEGER NOT NULL, " +
                    "world VARCHAR(255) NOT NULL, " +
                    "chunk_x INTEGER NOT NULL, " +
                    "chunk_z INTEGER NOT NULL, " +
                    "UNIQUE (entity_uuid)" +
                    ")");
        }

        // Create StackedBlock table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "stacked_block (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "stack_size INTEGER NOT NULL, " +
                    "world VARCHAR(255) NOT NULL, " +
                    "chunk_x INTEGER NOT NULL, " +
                    "chunk_z INTEGER NOT NULL, " +
                    "block_x INTEGER NOT NULL, " +
                    "block_y INTEGER NOT NULL, " +
                    "block_z INTEGER NOT NULL, " +
                    "UNIQUE (world, chunk_x, chunk_z, block_x, block_y, block_z)" +
                    ")");
        }

        // Create StackedSpawner table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "stacked_spawner (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "stack_size INTEGER NOT NULL, " +
                    "world VARCHAR(255) NOT NULL, " +
                    "chunk_x INTEGER NOT NULL, " +
                    "chunk_z INTEGER NOT NULL, " +
                    "block_x INTEGER NOT NULL, " +
                    "block_y INTEGER NOT NULL, " +
                    "block_z INTEGER NOT NULL, " +
                    "UNIQUE (world, chunk_x, chunk_z, block_x, block_y, block_z)" +
                    ")");
        }

        // Index the tables by world, chunk_x, and chunk_z
        try (Statement statement = connection.createStatement()) {
            statement.addBatch("CREATE INDEX " + tablePrefix + "stacked_block_index ON " + tablePrefix + "stacked_block (world, chunk_x, chunk_z)");
            statement.addBatch("CREATE INDEX " + tablePrefix + "stacked_entity_index ON " + tablePrefix + "stacked_entity (world, chunk_x, chunk_z)");
            statement.addBatch("CREATE INDEX " + tablePrefix + "stacked_item_index ON " + tablePrefix + "stacked_item (world, chunk_x, chunk_z)");
            statement.addBatch("CREATE INDEX " + tablePrefix + "stacked_spawner_index ON " + tablePrefix + "stacked_spawner (world, chunk_x, chunk_z)");
            statement.executeBatch();
        }

    }

}

