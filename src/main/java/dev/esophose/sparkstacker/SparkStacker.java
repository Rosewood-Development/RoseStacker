package dev.esophose.sparkstacker;

import dev.esophose.sparkstacker.hook.PlaceholderAPIHook;
import dev.esophose.sparkstacker.hook.SparkStackerPlaceholderExpansion;
import dev.esophose.sparkstacker.listener.BlockListener;
import dev.esophose.sparkstacker.listener.ChunkListener;
import dev.esophose.sparkstacker.listener.EntityListener;
import dev.esophose.sparkstacker.listener.InteractListener;
import dev.esophose.sparkstacker.listener.ItemListener;
import dev.esophose.sparkstacker.manager.CommandManager;
import dev.esophose.sparkstacker.manager.ConfigurationManager;
import dev.esophose.sparkstacker.manager.ConversionManager;
import dev.esophose.sparkstacker.manager.DataManager;
import dev.esophose.sparkstacker.manager.DataMigrationManager;
import dev.esophose.sparkstacker.manager.HologramManager;
import dev.esophose.sparkstacker.manager.LocaleManager;
import dev.esophose.sparkstacker.manager.Manager;
import dev.esophose.sparkstacker.manager.SpawnerSpawnManager;
import dev.esophose.sparkstacker.manager.StackManager;
import dev.esophose.sparkstacker.manager.StackSettingManager;
import dev.esophose.sparkstacker.utils.Metrics;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Esophose
 */
public class SparkStacker extends JavaPlugin {

    private static SparkStacker INSTANCE;

    private Set<Manager> managers;

    private CommandManager commandManager;
    private ConfigurationManager configurationManager;
    private ConversionManager conversionManager;
    private DataManager dataManager;
    private DataMigrationManager dataMigrationManager;
    private HologramManager hologramManager;
    private LocaleManager localeManager;
    private StackManager stackManager;
    private StackSettingManager stackSettingManager;
    private SpawnerSpawnManager spawnerSpawnManager;

    public static SparkStacker getInstance() {
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
        this.conversionManager = this.registerManager(ConversionManager.class);
        this.dataManager = new DataManager(this);
        this.dataMigrationManager = new DataMigrationManager(this);
        this.hologramManager = this.registerManager(HologramManager.class);
        this.localeManager = new LocaleManager(this);
        this.stackManager = this.registerManager(StackManager.class);
        this.stackSettingManager = new StackSettingManager(this);
        this.spawnerSpawnManager = this.registerManager(SpawnerSpawnManager.class);

        // Load managers
        this.reload();

        // Register listeners
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BlockListener(this), this);
        pluginManager.registerEvents(new ChunkListener(this), this);
        pluginManager.registerEvents(new EntityListener(this), this);
        pluginManager.registerEvents(new InteractListener(this), this);
        pluginManager.registerEvents(new ItemListener(this), this);

        // Try to hook with PlaceholderAPI
        if (PlaceholderAPIHook.enabled())
            new SparkStackerPlaceholderExpansion(this).register();
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
            T newManager = managerClass.getConstructor(SparkStacker.class).newInstance(this);
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
     * @return the ConversionManager instance
     */
    public ConversionManager getConversionManager() {
        return this.conversionManager;
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

    /**
     * @return the SpawnerSpawnManager instance
     */
    public SpawnerSpawnManager getSpawnerSpawnManager() {
        return this.spawnerSpawnManager;
    }

}
