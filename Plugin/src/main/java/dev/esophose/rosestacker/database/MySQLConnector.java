package dev.esophose.rosestacker.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.plugin.Plugin;

public class MySQLConnector implements DatabaseConnector {

    private final Plugin plugin;
    private HikariDataSource hikari;
    private final AtomicInteger openConnections;
    private final Object lock;

    public MySQLConnector(Plugin plugin, String hostname, int port, String database, String username, String password, boolean useSSL) {
        this.plugin = plugin;
        this.openConnections = new AtomicInteger();
        this.lock = new Object();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?useSSL=" + useSSL);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(5);

        try {
            this.hikari = new HikariDataSource(config);
        } catch (Exception ignored) { }
    }

    @Override
    public void closeConnection() {
        this.hikari.close();
    }

    @Override
    public void connect(ConnectionCallback callback) {
        this.openConnections.incrementAndGet();
        try (Connection connection = this.hikari.getConnection()) {
            callback.accept(connection);
        } catch (SQLException ex) {
            this.plugin.getLogger().severe("An error occurred executing a MySQL query: " + ex.getMessage());
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

}
