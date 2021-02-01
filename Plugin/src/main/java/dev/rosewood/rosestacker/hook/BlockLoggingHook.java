package dev.rosewood.rosestacker.hook;

import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class BlockLoggingHook {

    private static Boolean coreProtectEnabled;
    private static CoreProtectAPI coreProtectAPI;

    private static Boolean logBlockEnabled;
    private static Consumer logBlockConsumer;

    public static boolean coreProtectEnabled() {
        if (!Setting.MISC_COREPROTECT_LOGGING.getBoolean())
            return false;

        if (coreProtectEnabled != null)
            return coreProtectEnabled;

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CoreProtect");
        if (plugin != null) {
            coreProtectAPI = ((CoreProtect) plugin).getAPI();
            return coreProtectEnabled = coreProtectAPI.isEnabled();
        } else {
            return coreProtectEnabled = false;
        }
    }

    public static boolean logBlockEnabled() {
        if (!Setting.MISC_LOGBLOCK_LOGGING.getBoolean())
            return false;

        if (logBlockEnabled != null)
            return logBlockEnabled;

        Plugin plugin = Bukkit.getPluginManager().getPlugin("LogBlock");
        if (plugin != null) {
            logBlockConsumer = ((LogBlock) plugin).getConsumer();
            return logBlockEnabled = true;
        } else {
            return logBlockEnabled = false;
        }
    }

    public static void recordBlockPlace(Player player, Block block) {
        if (coreProtectEnabled()) {
            Material type = block.getType();
            BlockData blockData = null;

            if (type == Material.SPAWNER)
                blockData = block.getBlockData();

            coreProtectAPI.logPlacement(player.getName(), block.getLocation(), type, blockData);
        }

        if (logBlockEnabled())
            logBlockConsumer.queueBlockPlace(new Actor(player.getName(), player.getUniqueId()), block.getState());
    }

    public static void recordBlockBreak(Player player, Block block) {
        if (coreProtectEnabled()) {
            Material type = block.getType();
            BlockData blockData = null;

            if (type == Material.SPAWNER)
                blockData = block.getBlockData();

            coreProtectAPI.logRemoval(player.getName(), block.getLocation(), type, blockData);
        }

        if (logBlockEnabled())
            logBlockConsumer.queueBlockBreak(new Actor(player.getName(), player.getUniqueId()), block.getState());
    }

}
