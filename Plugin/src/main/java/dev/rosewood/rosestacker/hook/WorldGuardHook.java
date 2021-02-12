package dev.rosewood.rosestacker.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class WorldGuardHook {

    private static Boolean enabled;
    private static StateFlag flag;

    public static boolean enabled() {
        if (enabled != null)
            return enabled;
        return enabled = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    public static void registerFlag() {
        if (!enabled())
            return;

        flag = new StateFlag("rosestacker", true);
        WorldGuard.getInstance().getFlagRegistry().register(flag);
    }

    public static boolean testLocation(Location location) {
        if (!enabled() || !Setting.MISC_WORLDGUARD_REGION.getBoolean())
            return true;

        RegionQuery regionQuery = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return regionQuery.testState(BukkitAdapter.adapt(location), null, flag);
    }

}
