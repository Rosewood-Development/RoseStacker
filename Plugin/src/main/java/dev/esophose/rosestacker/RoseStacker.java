package dev.esophose.rosestacker;

import dev.esophose.rosestacker.hook.PlaceholderAPIHook;
import dev.esophose.rosestacker.hook.RoseStackerPlaceholderExpansion;
import dev.esophose.rosestacker.hook.ShopGuiPlusHook;
import dev.esophose.rosestacker.listener.BeeListener;
import dev.esophose.rosestacker.listener.BlockListener;
import dev.esophose.rosestacker.listener.ClearlagListener;
import dev.esophose.rosestacker.listener.EntityListener;
import dev.esophose.rosestacker.listener.InteractListener;
import dev.esophose.rosestacker.listener.ItemListener;
import dev.esophose.rosestacker.listener.WorldListener;
import dev.esophose.rosestacker.manager.CommandManager;
import dev.esophose.rosestacker.manager.ConfigurationManager;
import dev.esophose.rosestacker.manager.ConversionManager;
import dev.esophose.rosestacker.manager.DataManager;
import dev.esophose.rosestacker.manager.DataMigrationManager;
import dev.esophose.rosestacker.manager.HologramManager;
import dev.esophose.rosestacker.manager.LocaleManager;
import dev.esophose.rosestacker.manager.Manager;
import dev.esophose.rosestacker.manager.SpawnerSpawnManager;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.manager.StackSettingManager;
import dev.esophose.rosestacker.nms.NMSUtil;
import dev.esophose.rosestacker.utils.Metrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Esophose
 */
public class RoseStacker extends JavaPlugin {

    /**
     * The running instance of RoseStacker on the server
     */
    private static RoseStacker INSTANCE;

    /**
     * The plugin managers
     */
    private Map<Class<? extends Manager>, Manager> managers;

    public static RoseStacker getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        this.getLogger().info("Detected server API version as " + NMSUtil.getVersion());
        if (!NMSUtil.isValidVersion()) {
            this.getLogger().severe("This version of RoseStacker only supports 1.13.2 through 1.15.2. The plugin has been disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        INSTANCE = this;

        // bStats Metrics
        new Metrics(this);

        // Register managers
        this.managers = new LinkedHashMap<>();

        // Load managers
        this.reload();

        // Register listeners
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BlockListener(this), this);
        pluginManager.registerEvents(new WorldListener(this), this);
        pluginManager.registerEvents(new EntityListener(this), this);
        pluginManager.registerEvents(new InteractListener(this), this);
        pluginManager.registerEvents(new ItemListener(this), this);

        // Bees are only in 1.15+
        if (NMSUtil.getVersionNumber() >= 15)
            pluginManager.registerEvents(new BeeListener(this), this);

        // Try to hook with PlaceholderAPI
        if (PlaceholderAPIHook.enabled())
            new RoseStackerPlaceholderExpansion(this).register();

        // Try to hook with ShopGuiPlus
        if (Bukkit.getPluginManager().isPluginEnabled("ShopGuiPlus"))
            ShopGuiPlusHook.setupSpawners(this);

        // Try to hook with Clearlag
        if (Bukkit.getPluginManager().isPluginEnabled("Clearlag"))
            pluginManager.registerEvents(new ClearlagListener(this), this);
    }

    @Override
    public void onDisable() {
        if (INSTANCE == null)
            return;

        this.disableManagers();
        this.managers.clear();

        Bukkit.getScheduler().cancelTasks(this);
    }

    /**
     * Reloads the plugin's settings and data
     */
    public void reload() {
        this.disableManagers();
        this.managers.values().forEach(Manager::reload);

        this.getManager(ConfigurationManager.class);
        this.getManager(DataManager.class);
        this.getManager(DataMigrationManager.class);
        this.getManager(LocaleManager.class);
        this.getManager(StackSettingManager.class);
        this.getManager(CommandManager.class);
        this.getManager(ConversionManager.class);
        this.getManager(HologramManager.class);
        this.getManager(StackManager.class);
        this.getManager(SpawnerSpawnManager.class);
    }

    private void disableManagers() {
        List<Manager> managers = new ArrayList<>(this.managers.values());
        Collections.reverse(managers);
        managers.forEach(Manager::disable);
    }

    /**
     * Gets a manager instance
     *
     * @param managerClass The class of the manager to get
     * @param <T> extends Manager
     * @return A new instance of the given manager class
     */
    @SuppressWarnings("unchecked")
    public <T extends Manager> T getManager(Class<T> managerClass) {
        if (this.managers.containsKey(managerClass))
            return (T) this.managers.get(managerClass);

        try {
            T manager = managerClass.getConstructor(RoseStacker.class).newInstance(this);
            this.managers.put(managerClass, manager);
            manager.reload();
            return manager;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
