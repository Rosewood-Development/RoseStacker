package dev.rosewood.rosestacker.hook;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class WorldGuardHook {

    private static Boolean enabled;

    public static boolean enabled() {
        if (enabled != null)
            return enabled;
        return enabled = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    public static void registerFlag() {
        if (!enabled())
            return;

        WorldGuardFlagHook.registerFlag();
    }

    public static boolean testLocation(Location location) {
        if (!enabled())
            return true;

        return WorldGuardFlagHook.testLocation(location);
    }

}
