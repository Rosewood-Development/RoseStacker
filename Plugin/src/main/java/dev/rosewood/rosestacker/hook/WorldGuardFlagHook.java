package dev.rosewood.rosestacker.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.rosewood.rosestacker.config.SettingKey;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Wrapper class to fix the Flag class trying to load without WorldGuard installed
 */
public class WorldGuardFlagHook {

    private static StateFlag flag;

    /**
     * UNCHECKED! Call {@link WorldGuardHook#registerFlag}
     */
    public static void registerFlag() {
        if (!SettingKey.MISC_WORLDGUARD_REGION.get())
            return;

        flag = new StateFlag("rosestacker", true);
        WorldGuard.getInstance().getFlagRegistry().register(flag);
    }

    /**
     * UNCHECKED! Call {@link WorldGuardHook#testLocation}
     *
     * @param location The Location to test
     */
    public static boolean testLocation(Location location) {
        if (!SettingKey.MISC_WORLDGUARD_REGION.get())
            return true;

        RegionQuery regionQuery = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return regionQuery.testState(BukkitAdapter.adapt(location), null, flag);
    }

    /**
     * UNCHECKED! Call {@link WorldGuardHook#testCanDropExperience}
     *
     * @param player The player that is causing the experience to drop, nullable
     * @param location The location the experience is dropping at
     * @return true if the Location is flagged to allow dropping experience, false otherwise
     */
    public static boolean testCanDropExperience(Player player, Location location) {
        return testFlag(player, location, Flags.EXP_DROPS);
    }

    private static boolean testFlag(Player player, Location location, StateFlag flag) {
        if (!SettingKey.MISC_WORLDGUARD_OBEY_FLAGS.get())
            return true;

        LocalPlayer localPlayer = player != null ? WorldGuardPlugin.inst().wrapPlayer(player) : null;
        if (localPlayer != null && WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(location.getWorld())))
            return true;

        RegionQuery regionQuery = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return regionQuery.testState(BukkitAdapter.adapt(location), localPlayer, flag);
    }

}
