package dev.rosewood.rosestacker.database.migrations;

import dev.rosewood.rosegarden.database.DataMigration;
import dev.rosewood.rosegarden.database.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _3_Create_Tables_Translation_Locales extends DataMigration {

    public _3_Create_Tables_Translation_Locales() {
        super(3);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection, String tablePrefix) throws SQLException {
        // Create translation locales table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "translation_locale (" +
                    "version VARCHAR(10) NOT NULL, " +
                    "name VARCHAR(50) NOT NULL" +
                    ")");
        }
    }

}

