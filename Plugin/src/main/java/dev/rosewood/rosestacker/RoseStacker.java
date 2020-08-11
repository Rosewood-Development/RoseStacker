package dev.rosewood.rosestacker;

import dev.rosewood.rosestacker.hook.PlaceholderAPIHook;
import dev.rosewood.rosestacker.hook.RoseStackerPlaceholderExpansion;
import dev.rosewood.rosestacker.hook.ShopGuiPlusHook;
import dev.rosewood.rosestacker.listener.BeeListener;
import dev.rosewood.rosestacker.listener.BlockListener;
import dev.rosewood.rosestacker.listener.BlockShearListener;
import dev.rosewood.rosestacker.listener.ClearlagListener;
import dev.rosewood.rosestacker.listener.EntityListener;
import dev.rosewood.rosestacker.listener.InteractListener;
import dev.rosewood.rosestacker.listener.ItemListener;
import dev.rosewood.rosestacker.listener.WorldListener;
import dev.rosewood.rosestacker.manager.CommandManager;
import dev.rosewood.rosestacker.manager.ConfigurationManager;
import dev.rosewood.rosestacker.manager.ConversionManager;
import dev.rosewood.rosestacker.manager.DataManager;
import dev.rosewood.rosestacker.manager.DataMigrationManager;
import dev.rosewood.rosestacker.manager.HologramManager;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.Manager;
import dev.rosewood.rosestacker.manager.SpawnerSpawnManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSUtil;
import dev.rosewood.rosestacker.utils.Metrics;
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
    private static RoseStacker instance;

    /**
     * The plugin managers
     */
    private Map<Class<? extends Manager>, Manager> managers;

    public static RoseStacker getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        this.getLogger().info("Detected server API version as " + NMSUtil.getVersion());
        if (!NMSUtil.isValidVersion()) {
            this.getLogger().severe("This version of RoseStacker only supports 1.13.2 through 1.16.1. The plugin has been disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        instance = this;

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

        // Dispensers can only shear sheep in 1.14+
        if (NMSUtil.getVersionNumber() >= 14)
            pluginManager.registerEvents(new BlockShearListener(this), this);

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
        if (instance == null)
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
