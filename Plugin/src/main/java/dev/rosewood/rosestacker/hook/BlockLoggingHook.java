package dev.rosewood.rosestacker.hook;

import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.storage.ChunkStorage;
import dev.frankheijden.insights.api.concurrent.storage.WorldStorage;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.utils.ChunkUtils;
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

    private static Boolean insightsEnabled;
    private static InsightsPlugin insightsPlugin;

    /**
     * @return true if CoreProtect is enabled, false otherwise
     */
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

    /**
     * @return true if LogBlock is enabled, false otherwise
     */
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

    /**
     * @return true if Insights is enabled, false otherwise
     */
    public static boolean insightsEnabled() {
        if (!Setting.MISC_INSIGHTS_LOGGING.getBoolean())
            return false;

        if (insightsEnabled != null)
            return insightsEnabled;

        Plugin plugin = Bukkit.getPluginManager().getPlugin("Insights");
        if (plugin != null) {
            insightsPlugin = Insights.getInstance();
            return insightsEnabled = true;
        } else {
            return insightsEnabled = false;
        }
    }

    /**
     * Records a block place
     *
     * @param player The Player that placed the block
     * @param block The Block that was placed
     */
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

        if (insightsEnabled()) {
            ChunkStorage chunkStorage = insightsPlugin.getWorldStorage().getWorld(block.getWorld().getUID());
            long chunkKey = ChunkUtils.getKey(block.getLocation());
            chunkStorage.get(chunkKey).ifPresent(chunk -> chunk.modify(ScanObject.of(block.getType()), 1));
        }
    }

    /**
     * Records a block break
     *
     * @param player The Player that broke the block
     * @param block The Block that was broken
     */
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

        if (insightsEnabled()) {
            ChunkStorage chunkStorage = insightsPlugin.getWorldStorage().getWorld(block.getWorld().getUID());
            long chunkKey = ChunkUtils.getKey(block.getLocation());
            chunkStorage.get(chunkKey).ifPresent(chunk -> chunk.modify(ScanObject.of(block.getType()), -1));
        }
    }

}
