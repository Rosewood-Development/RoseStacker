package dev.esophose.sparkstacker.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseConnector {

    /**
     * Closes all open connections to the database
     */
    void closeConnection();

    /**
     * Executes a callback with a Connection passed and automatically closes it when finished
     *
     * @param callback The callback to execute once the connection is retrieved
     */
    void connect(ConnectionCallback callback);

    /**
     * @return the lock to notify when all connections have been finalized
     */
    Object getLock();

    /**
     * @return true if all connections have finished, otherwise false
     */
    boolean isFinished();

    /**
     * Wraps a connection in a callback which will automagically handle catching sql errors
     */
    interface ConnectionCallback {
        void accept(Connection connection) throws SQLException;
    }

}
