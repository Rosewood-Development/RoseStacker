package dev.esophose.rosestacker.listeners;

import dev.esophose.rosestacker.RoseStacker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {

    private RoseStacker roseStacker;

    public ChunkListener(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        this.roseStacker.getStackManager().loadChunk(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        this.roseStacker.getStackManager().unloadChunk(event.getChunk());
    }

}
