package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.database.DatabaseConnector;
import dev.esophose.rosestacker.database.MySQLConnector;
import dev.esophose.rosestacker.database.SQLiteConnector;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.stack.StackedBlock;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.StackedItem;
import dev.esophose.rosestacker.stack.StackedSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class DataManager extends Manager {

    private DatabaseConnector databaseConnector;

    public DataManager(RoseStacker roseStacker) {
        super(roseStacker);
    }

    @Override
    public void reload() {
        if (this.databaseConnector != null)
            this.databaseConnector.closeConnection();

        try {
            if (Setting.MYSQL_ENABLED.getBoolean()) {
                String hostname = Setting.MYSQL_HOSTNAME.getString();
                int port = Setting.MYSQL_PORT.getInt();
                String database = Setting.MYSQL_DATABASE_NAME.getString();
                String username = Setting.MYSQL_USER_NAME.getString();
                String password = Setting.MYSQL_USER_PASSWORD.getString();
                boolean useSSL = Setting.MYSQL_USE_SSL.getBoolean();

                this.databaseConnector = new MySQLConnector(this.roseStacker, hostname, port, database, username, password, useSSL);
                this.roseStacker.getLogger().info("Data handler connected using MySQL.");
            } else {
                this.databaseConnector = new SQLiteConnector(this.roseStacker);
                this.roseStacker.getLogger().info("Data handler connected using SQLite.");
            }
        } catch (Exception ex) {
            this.roseStacker.getLogger().severe("Fatal error trying to connect to database. Please make sure all your connection settings are correct and try again. Plugin has been disabled.");
            Bukkit.getPluginManager().disablePlugin(this.roseStacker);
        }
    }

    @Override
    public void disable() {
        this.databaseConnector.closeConnection();
    }

    public Set<StackedBlock> getStackedBlocks(Chunk chunk) {
        Set<StackedBlock> stackedBlocks = new HashSet<>();

        return stackedBlocks;
    }

    public Set<StackedBlock> getStackedBlocks(Set<World> worlds) {
        Set<StackedBlock> stackedBlocks = new HashSet<>();

        return stackedBlocks;
    }

    public Set<StackedEntity> getStackedEntities(Chunk chunk) {
        Set<StackedEntity> stackedEntities = new HashSet<>();

        return stackedEntities;
    }

    public Set<StackedEntity> getStackedEntities(Set<World> worlds) {
        Set<StackedEntity> stackedEntities = new HashSet<>();

        return stackedEntities;
    }

    public Set<StackedItem> getStackedItems(Chunk chunk) {
        Set<StackedItem> stackedItems = new HashSet<>();

        return stackedItems;
    }

    public Set<StackedItem> getStackedItems(Set<World> worlds) {
        Set<StackedItem> stackedItems = new HashSet<>();

        return stackedItems;
    }

    public Set<StackedSpawner> getStackedSpawners(Chunk chunk) {
        Set<StackedSpawner> stackedSpawners = new HashSet<>();

        return stackedSpawners;
    }

    public Set<StackedSpawner> getStackedSpawners(Set<World> worlds) {
        Set<StackedSpawner> stackedSpawners = new HashSet<>();

        return stackedSpawners;
    }

    public void updateStackedBlocks(Set<StackedBlock> stackedBlocks) {

    }

    public void updateStackedEntities(Set<StackedEntity> stackedEntities) {

    }

    public void updateStackedItems(Set<StackedItem> stackedItems) {

    }

    public void updateStackedSpawners(Set<StackedSpawner> stackedSpawners) {

    }

    public void deleteStackedBlocks(Set<StackedBlock> stackedBlocks) {

    }

    public void deleteStackedEntities(Set<StackedEntity> stackedEntities) {

    }

    public void deleteStackedItems(Set<StackedItem> stackedItems) {

    }

    public void deleteSpawners(Set<StackedSpawner> stackedSpawners) {

    }

    /**
     * @return The connector to the database
     */
    public DatabaseConnector getDatabaseConnector() {
        return this.databaseConnector;
    }

    /**
     * @return the prefix to be used by all table names
     */
    public String getTablePrefix() {
        return this.roseStacker.getDescription().getName().toLowerCase() + '_';
    }

}
