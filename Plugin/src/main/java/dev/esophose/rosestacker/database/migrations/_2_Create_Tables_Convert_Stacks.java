package dev.esophose.rosestacker.database.migrations;

import dev.esophose.rosestacker.database.DataMigration;
import dev.esophose.rosestacker.database.DatabaseConnector;
import dev.esophose.rosestacker.database.MySQLConnector;
import java.sql.Connection;
import java.sql.SQLException;

public class _2_Create_Tables_Convert_Stacks extends DataMigration {

    public _2_Create_Tables_Convert_Stacks() {
        super(2);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection, String tablePrefix) throws SQLException {
        String autoIncrement = connector instanceof MySQLConnector ? " AUTO_INCREMENT" : "";

        // TODO
    }

}

