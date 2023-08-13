package dev.rosewood.rosestacker.hook;

import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.api.concurrent.storage.ChunkStorage;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.block.Block;

/**
 * Wrapper class to fix classes trying to load without Insights installed
 */
public class InsightsHook {

    public static void modifyBlockAmount(Block block, int amount) {
        ChunkStorage chunkStorage = Insights.getInstance().getWorldStorage().getWorld(block.getWorld().getUID());
        long chunkKey = ChunkUtils.getKey(block.getLocation());
        chunkStorage.get(chunkKey).ifPresent(chunk -> chunk.modify(ScanObject.of(block.getType()), amount));
    }

}
