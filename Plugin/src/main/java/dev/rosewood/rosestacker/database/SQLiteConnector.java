package dev.rosewood.rosestacker.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.plugin.Plugin;

public class SQLiteConnector implements DatabaseConnector {

    private final Plugin plugin;
    private final String connectionString;
    private Connection connection;
    private final AtomicInteger openConnections;
    private final Object lock;

    public SQLiteConnector(Plugin plugin) {
        this(plugin, plugin.getDescription().getName().toLowerCase());
    }

    public SQLiteConnector(Plugin plugin, String dbName) {
        this.plugin = plugin;
        this.connectionString = "jdbc:sqlite:" + plugin.getDataFolder() + File.separator + dbName + ".db";
        this.openConnections = new AtomicInteger();
        this.lock = new Object();

        try {
            Class.forName("org.sqlite.JDBC"); // Make sure the driver is actually registered
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

//        this.connect((connection) -> {
//            connection.createStatement().execute("PRAGMA journal_mode=MEMORY; ");
//            connection.createStatement().execute("PRAGMA temp_store=MEMORY");
//            connection.createStatement().execute("PRAGMA synchronous=NORMAL");
//        });
    }

    @Override
    public void closeConnection() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException ex) {
            this.plugin.getLogger().severe("An error occurred closing the SQLite database connection: " + ex.getMessage());
        }
    }

    @Override
    public void connect(ConnectionCallback callback) {
        if (this.connection == null) {
            try {
                this.connection = DriverManager.getConnection(this.connectionString);
                this.connection.setAutoCommit(false);
            } catch (SQLException ex) {
                this.plugin.getLogger().severe("An error occurred retrieving the SQLite database connection: " + ex.getMessage());
            }
        }

        this.openConnections.incrementAndGet();
        try {
            callback.accept(this.connection);
            try {
                this.connection.commit();
            } catch (SQLException e) {
                if (e.getMessage() != null && !e.getMessage().contains("transaction"))
                    throw e;

                try {
                    this.connection.rollback();
                } catch (SQLException ignored) { }
            }
        } catch (Exception ex) {
            this.plugin.getLogger().severe("An error occurred executing an SQLite query: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            int open = this.openConnections.decrementAndGet();
            synchronized (this.lock) {
                if (open == 0)
                    this.lock.notify();
            }
        }
    }

    @Override
    public Object getLock() {
        return this.lock;
    }

    @Override
    public boolean isFinished() {
        return this.openConnections.get() == 0;
    }

    @Override
    public void cleanup() {
        if (this.connection != null) {
            try {
                this.connection.createStatement().execute("VACUUM");
            } catch (SQLException ignored) { }
        }
    }

}

