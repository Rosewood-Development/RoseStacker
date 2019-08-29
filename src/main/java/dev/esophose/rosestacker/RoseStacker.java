package dev.esophose.rosestacker;

import dev.esophose.rosestacker.listeners.ChunkListener;
import dev.esophose.rosestacker.listeners.EntityListener;
import dev.esophose.rosestacker.manager.CommandManager;
import dev.esophose.rosestacker.manager.ConfigurationManager;
import dev.esophose.rosestacker.manager.DataManager;
import dev.esophose.rosestacker.manager.DataMigrationManager;
import dev.esophose.rosestacker.manager.LocaleManager;
import dev.esophose.rosestacker.manager.Manager;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.utils.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Esophose
 */
public class RoseStacker extends JavaPlugin {

    private static RoseStacker INSTANCE;

    private Set<Manager> managers;

    private CommandManager commandManager;
    private ConfigurationManager configurationManager;
    private DataManager dataManager;
    private DataMigrationManager dataMigrationManager;
    private LocaleManager localeManager;
    private StackManager stackManager;

    public static RoseStacker getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        // bStats Metrics
        new Metrics(this);

        // Register managers
        this.managers = new HashSet<>();
        this.commandManager = this.registerManager(CommandManager.class);
        this.configurationManager = new ConfigurationManager(this);
        this.dataManager = new DataManager(this);
        this.dataMigrationManager = new DataMigrationManager(this);
        this.localeManager = this.registerManager(LocaleManager.class);
        this.stackManager = this.registerManager(StackManager.class);

        // Load managers
        this.reload();

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new ChunkListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityListener(this), this);
    }

    @Override
    public void onDisable() {
        this.disable();
    }

    /**
     * Reloads the plugin's settings and data
     */
    public void reload() {
        this.configurationManager.reload();
        this.dataManager.reload();
        this.dataMigrationManager.reload();
        this.managers.forEach(Manager::reload);
    }

    /**
     * Disables most of the plugin
     */
    public void disable() {
        this.configurationManager.disable();
        this.dataManager.disable();
        this.dataMigrationManager.disable();
        this.managers.forEach(Manager::disable);
    }

    /**
     * Registers a manager
     *
     * @param managerClass The class of the manager to create a new instance of
     * @param <T> extends Manager
     * @return A new instance of the given manager class
     */
    private <T extends Manager> T registerManager(Class<T> managerClass) {
        try {
            T newManager = managerClass.getConstructor(RoseStacker.class).newInstance(this);
            this.managers.add(newManager);
            return newManager;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * @return the CommandManager instance
     */
    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    /**
     * @return the ConfigurationManager instance
     */
    public ConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    /**
     * @return the DataManager instance
     */
    public DataManager getDataManager() {
        return this.dataManager;
    }

    /**
     * @return the DataMigrationManager instance
     */
    public DataMigrationManager getDataMigrationManager() {
        return this.dataMigrationManager;
    }

    /**
     * @return the StackManager instance
     */
    public StackManager getStackManager() {
        return this.stackManager;
    }

    /**
     * @return the LocaleManager instance
     */
    public LocaleManager getLocaleManager() {
        return this.localeManager;
    }

}
