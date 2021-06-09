package dev.rosewood.rosestacker.database.migrations;

import dev.rosewood.rosegarden.database.DataMigration;
import dev.rosewood.rosegarden.database.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _4_Alter_Spawner_Table_Player_Placed extends DataMigration {

    public _4_Alter_Spawner_Table_Player_Placed() {
        super(4);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection, String tablePrefix) throws SQLException {
        // Add placed_by_player column to stacked_spawner table, default to 0 (false)
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tablePrefix + "stacked_spawner ADD COLUMN placed_by_player TINYINT NOT NULL DEFAULT 0");
        }
    }

}

