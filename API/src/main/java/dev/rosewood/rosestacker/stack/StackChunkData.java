package dev.rosewood.rosestacker.stack;

import java.util.Map;
import org.bukkit.block.Block;

/**
 * Tracks a Chunk's StackedSpawners and StackedBlocks
 */
public interface StackChunkData {

    void addSpawner(StackedSpawner stackedSpawner);

    void addBlock(StackedBlock stackedBlock);

    void removeSpawner(StackedSpawner stackedSpawner);

    void removeBlock(StackedBlock stackedBlock);

    StackedSpawner getSpawner(Block block);

    StackedBlock getBlock(Block block);

    Map<Block, StackedSpawner> getSpawners();

    Map<Block, StackedBlock> getBlocks();

}
