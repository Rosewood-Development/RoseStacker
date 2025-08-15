package dev.rosewood.rosestacker;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.hook.RoseStackerPlaceholderExpansion;
import dev.rosewood.rosestacker.hook.ShopGuiPlusHook;
import dev.rosewood.rosestacker.hook.WildChestsStackerProvider;
import dev.rosewood.rosestacker.hook.WorldGuardHook;
import dev.rosewood.rosestacker.listener.BeeListener;
import dev.rosewood.rosestacker.listener.BlockListener;
import dev.rosewood.rosestacker.listener.BreedingListener;
import dev.rosewood.rosestacker.listener.EntitiesLoadListener;
import dev.rosewood.rosestacker.listener.EntityListener;
import dev.rosewood.rosestacker.listener.InteractListener;
import dev.rosewood.rosestacker.listener.ItemListener;
import dev.rosewood.rosestacker.listener.SkyblockHookListener;
import dev.rosewood.rosestacker.listener.StackToolListener;
import dev.rosewood.rosestacker.listener.WorldListener;
import dev.rosewood.rosestacker.manager.CommandManager;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import dev.rosewood.rosestacker.manager.HologramManager;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

/**
 * @author Esophose
 */
public class RoseStacker extends RosePlugin {

    /**
     * The running instance of RoseStacker on the server
     */
    private static RoseStacker instance;

    public static RoseStacker getInstance() {
        return instance;
    }

    public RoseStacker() {
        super(82729, 5517, null, LocaleManager.class, CommandManager.class);

        instance = this;
    }

    @Override
    public void onLoad() {
        if (!NMSAdapter.isValidVersion()) {
            this.getLogger().severe(String.format("RoseStacker only supports %s through %s. The plugin has been disabled.", StackerUtils.MIN_SUPPORTED_VERSION, StackerUtils.MAX_SUPPORTED_VERSION));
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        WorldGuardHook.registerFlag();
    }

    @Override
    public void enable() {
        // Register listeners
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BlockListener(this), this);
        pluginManager.registerEvents(new WorldListener(this), this);
        pluginManager.registerEvents(new EntityListener(this), this);
        pluginManager.registerEvents(new InteractListener(this), this);
        pluginManager.registerEvents(new ItemListener(this), this);
        pluginManager.registerEvents(new StackToolListener(this), this);
        pluginManager.registerEvents(new BreedingListener(this), this);
        pluginManager.registerEvents(new BeeListener(this), this);

        if (NMSUtil.getVersionNumber() >= 17) {
            try {
                Class.forName("org.bukkit.event.world.EntitiesLoadEvent");
                pluginManager.registerEvents(new EntitiesLoadListener(this), this);
            } catch (ClassNotFoundException e) {
                this.getLogger().severe("Your version of 1.17 is VERY OUTDATED! Stacked entities and items WILL NOT LOAD until you update!");
            }
        }

        // Try to hook with PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
            new RoseStackerPlaceholderExpansion(this).register();

        // Try to hook with ShopGuiPlus and skyblock plugins
        ThreadUtils.runSync(() -> {
            if (Bukkit.getPluginManager().isPluginEnabled("ShopGUIPlus"))
                ShopGuiPlusHook.setupSpawners(this);

            SkyblockHookListener skyblockHookListener = new SkyblockHookListener();
            if (skyblockHookListener.isEnabled())
                pluginManager.registerEvents(skyblockHookListener, this);
        });

        // Try to hook with WildChests
        if (Bukkit.getPluginManager().isPluginEnabled("WildChests"))
            WildChestsStackerProvider.register();
    }

    @Override
    public void reload() {
        super.reload();
        StackerUtils.clearCache();
    }

    @Override
    public void disable() {
        this.getScheduler().cancelAllTasks();
    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return List.of(
                HologramManager.class,
                StackSettingManager.class,
                CommandManager.class,
                EntityCacheManager.class,
                StackManager.class
        );
    }

    @Override
    protected SettingHolder getRoseConfigSettingHolder() {
        return SettingKey.INSTANCE;
    }

    @Override
    protected String[] getRoseConfigHeader() {
        return new String[]{
                "     __________                      _________ __                 __                 ",
                "     \\______   \\ ____  ______ ____  /   _____//  |______    ____ |  | __ ___________ ",
                "      |       _//  _ \\/  ___// __ \\ \\_____  \\\\   __\\__  \\ _/ ___\\|  |/ // __ \\_  __ \\",
                "      |    |   (  <_> )___ \\\\  ___/ /        \\|  |  / __ \\\\  \\___|    <\\  ___/|  | \\/",
                "      |____|_  /\\____/____  >\\___  >_______  /|__| (____  /\\___  >__|_ \\\\___  >__|   ",
                "             \\/           \\/     \\/        \\/           \\/     \\/     \\/    \\/       "
        };
    }

}
