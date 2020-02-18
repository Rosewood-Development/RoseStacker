package dev.esophose.sparkstacker.hook;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CoreProtectHook {

    private static Boolean enabled;
    private static CoreProtectAPI coreProtectAPI;

    public static boolean enabled() {
        if (enabled != null)
            return enabled;

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CoreProtect");
        if (plugin != null) {
            coreProtectAPI = ((CoreProtect) plugin).getAPI();
            return enabled = coreProtectAPI.isEnabled();
        } else {
            return enabled = false;
        }
    }

    public static void recordBlockPlace(Player player, Block block) {
        if (!enabled())
            return;

        Material type = block.getType();
        BlockData blockData = null;

        if (type == Material.SPAWNER)
            blockData = block.getBlockData();

        coreProtectAPI.logPlacement(player.getName(), block.getLocation(), type, blockData);
    }

    public static void recordBlockBreak(Player player, Block block) {
        if (!enabled())
            return;

        Material type = block.getType();
        BlockData blockData = null;

        if (type == Material.SPAWNER)
            blockData = block.getBlockData();

        coreProtectAPI.logRemoval(player.getName(), block.getLocation(), type, blockData);
    }

}
