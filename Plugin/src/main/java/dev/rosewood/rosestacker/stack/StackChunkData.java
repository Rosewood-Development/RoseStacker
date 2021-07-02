package dev.rosewood.rosestacker.stack;

import java.util.Map;
import org.bukkit.block.Block;

public class StackChunkData {

    private final Map<Block, StackedSpawner> stackedSpawners;
    private final Map<Block, StackedBlock> stackedBlocks;

    private boolean dirtySpawners;
    private boolean dirtyBlocks;

    public StackChunkData(Map<Block, StackedSpawner> stackedSpawners, Map<Block, StackedBlock> stackedBlocks) {
        this.stackedSpawners = stackedSpawners;
        this.stackedBlocks = stackedBlocks;
    }

    public void addSpawner(StackedSpawner stackedSpawner) {
        this.stackedSpawners.put(stackedSpawner.getSpawner().getBlock(), stackedSpawner);
        this.dirtySpawners = true;
    }

    public void addBlock(StackedBlock stackedBlock) {
        this.stackedBlocks.put(stackedBlock.getBlock(), stackedBlock);
        this.dirtyBlocks = true;
    }

    public void removeSpawner(StackedSpawner stackedSpawner) {
        this.stackedSpawners.remove(stackedSpawner.getSpawner().getBlock());
        this.dirtySpawners = true;
    }

    public void removeBlock(StackedBlock stackedBlock) {
        this.stackedBlocks.remove(stackedBlock.getBlock());
        this.dirtyBlocks = true;
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

    public boolean isDirtySpawners() {
        return this.dirtySpawners;
    }

    public boolean isDirtyBlocks() {
        return this.dirtyBlocks;
    }

}
