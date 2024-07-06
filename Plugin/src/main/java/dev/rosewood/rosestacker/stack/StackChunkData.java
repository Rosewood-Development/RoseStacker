package dev.rosewood.rosestacker.stack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.block.Block;

/**
 * Tracks a Chunk's StackedSpawners and StackedBlocks
 */
public class StackChunkData {

    private final Map<Block, StackedSpawner> stackedSpawners;
    private final Map<Block, StackedBlock> stackedBlocks;

    public StackChunkData() {
        this.stackedSpawners = new ConcurrentHashMap<>();
        this.stackedBlocks = new ConcurrentHashMap<>();
    }

    public StackChunkData(Map<Block, StackedSpawner> stackedSpawners, Map<Block, StackedBlock> stackedBlocks) {
        this.stackedSpawners = stackedSpawners;
        this.stackedBlocks = stackedBlocks;
    }

    public void addSpawner(StackedSpawner stackedSpawner) {
        this.stackedSpawners.put(stackedSpawner.getBlock(), stackedSpawner);
    }

    public void addBlock(StackedBlock stackedBlock) {
        this.stackedBlocks.put(stackedBlock.getBlock(), stackedBlock);
    }

    public void removeSpawner(StackedSpawner stackedSpawner) {
        this.stackedSpawners.remove(stackedSpawner.getBlock());
    }

    public void removeBlock(StackedBlock stackedBlock) {
        this.stackedBlocks.remove(stackedBlock.getBlock());
    }

    public StackedSpawner getSpawner(Block block) {
        return this.stackedSpawners.get(block);
    }

    public StackedBlock getBlock(Block block) {
        return this.stackedBlocks.get(block);
    }

    public Map<Block, StackedSpawner> getSpawners() {
        return this.stackedSpawners;
    }

    public Map<Block, StackedBlock> getBlocks() {
        return this.stackedBlocks;
    }

}
