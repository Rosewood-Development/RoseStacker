package dev.esophose.rosestacker;

import dev.esophose.rosestacker.listener.BlockListener;
import dev.esophose.rosestacker.listener.ChunkListener;
import dev.esophose.rosestacker.listener.EntityListener;
import dev.esophose.rosestacker.listener.InteractListener;
import dev.esophose.rosestacker.listener.ItemListener;
import dev.esophose.rosestacker.manager.CommandManager;
import dev.esophose.rosestacker.manager.ConfigurationManager;
import dev.esophose.rosestacker.manager.DataManager;
import dev.esophose.rosestacker.manager.DataMigrationManager;
import dev.esophose.rosestacker.manager.HologramManager;
import dev.esophose.rosestacker.manager.LocaleManager;
import dev.esophose.rosestacker.manager.Manager;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.manager.StackSettingManager;
import dev.esophose.rosestacker.utils.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
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
    private HologramManager hologramManager;
    private LocaleManager localeManager;
    private StackManager stackManager;
    private StackSettingManager stackSettingManager;

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
        this.hologramManager = this.registerManager(HologramManager.class);
        this.localeManager = new LocaleManager(this);
        this.stackManager = this.registerManager(StackManager.class);
        this.stackSettingManager = new StackSettingManager(this);

        // Load managers
        this.reload();

        // Register listeners
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BlockListener(this), this);
        pluginManager.registerEvents(new ChunkListener(this), this);
        pluginManager.registerEvents(new EntityListener(this), this);
        pluginManager.registerEvents(new InteractListener(this), this);
        pluginManager.registerEvents(new ItemListener(this), this);
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
        this.localeManager.reload();
        this.stackSettingManager.reload();
        this.dataManager.reload();
        this.dataMigrationManager.reload();
        this.managers.forEach(Manager::reload); // The order doesn't matter for the rest
    }

    /**
     * Disables most of the plugin
     */
    public void disable() {
        this.managers.forEach(Manager::disable);
        this.configurationManager.disable();
        this.localeManager.disable();
        this.stackSettingManager.disable();
        this.dataManager.disable();
        this.dataMigrationManager.disable();
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
     * @return the HologramManager instance
     */
    public HologramManager getHologramManager() {
        return this.hologramManager;
    }

    /**
     * @return the LocaleManager instance
     */
    public LocaleManager getLocaleManager() {
        return this.localeManager;
    }

    /**
     * @return the StackManager instance
     */
    public StackManager getStackManager() {
        return this.stackManager;
    }

    /**
     * @return the StackSettingManager instance
     */
    public StackSettingManager getStackSettingManager() {
        return this.stackSettingManager;
    }

}
