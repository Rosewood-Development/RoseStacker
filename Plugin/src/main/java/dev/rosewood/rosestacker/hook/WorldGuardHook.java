package dev.rosewood.rosestacker.hook;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHook {

    private static Boolean enabled;

    /**
     * @return true if WorldGuard is enabled, false otherwise
     */
    public static boolean enabled() {
        if (enabled != null)
            return enabled;
        return enabled = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    /**
     * Registers the WorldGuard flag
     */
    public static void registerFlag() {
        if (!enabled())
            return;

        WorldGuardFlagHook.registerFlag();
    }

    /**
     * Tests if the given Location is within a WorldGuard region that has our flag
     *
     * @param location The Location to test
     * @return true if the Location is flagged with our flag, false otherwise
     */
    public static boolean testLocation(Location location) {
        if (!enabled())
            return true;

        return WorldGuardFlagHook.testLocation(location);
    }

    /**
     * Tests if the given Location allows dropping experience
     *
     * @param player The player that is causing the experience to drop, nullable
     * @param location The location the experience is dropping at
     * @return true if the Location is flagged to allow dropping experience, false otherwise
     */
    public static boolean testCanDropExperience(Player player, Location location) {
        if (!enabled())
            return true;

        return WorldGuardFlagHook.testCanDropExperience(player, location);
    }

}
