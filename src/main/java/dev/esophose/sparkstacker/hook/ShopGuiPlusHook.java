package dev.esophose.sparkstacker.hook;

import dev.esophose.sparkstacker.SparkStacker;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.provider.spawner.SpawnerProvider;
import org.bukkit.Bukkit;

public final class ShopGuiPlusHook {

    private static Boolean enabled;

    /**
     * @return true if ShopGuiPlus is enabled, otherwise false
     */
    public static boolean enabled() {
        if (enabled != null)
            return enabled;
        return enabled = Bukkit.getPluginManager().getPlugin("ShopGuiPlus") != null;
    }

    /**
     * Registers the spawner provider with ShopGuiPlus
     *
     * @param sparkStacker The plugin instance
     */
    public static void setupSpawners(SparkStacker sparkStacker) {
        if (enabled())
            ShopGuiPlusApi.registerSpawnerProvider(new SparkStackerSpawnerProvider(sparkStacker));
    }

}
