package dev.rosewood.rosestacker.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.rosewood.rosestacker.manager.ConfigurationManager;
import org.bukkit.Location;

/**
 * Wrapper class to fix the Flag class trying to load without WorldGuard installed
 */
public class WorldGuardFlagHook {

    private static StateFlag flag;

    /**
     * UNCHECKED! Call {@link WorldGuardHook#registerFlag}
     */
    public static void registerFlag() {
        flag = new StateFlag("rosestacker", true);
        WorldGuard.getInstance().getFlagRegistry().register(flag);
    }

    /**
     * UNCHECKED! Call {@link WorldGuardHook#testLocation}}
     */
    public static boolean testLocation(Location location) {
        if (!ConfigurationManager.Setting.MISC_WORLDGUARD_REGION.getBoolean())
            return true;

        RegionQuery regionQuery = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return regionQuery.testState(BukkitAdapter.adapt(location), null, flag);
    }

}
