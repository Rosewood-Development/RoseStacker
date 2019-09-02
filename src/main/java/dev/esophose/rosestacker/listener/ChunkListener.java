package dev.esophose.rosestacker.listener;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.StackManager;
import org.bukkit.entity.Entity;
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
        StackManager stackManager = this.roseStacker.getStackManager();

        if (event.isNewChunk()) {
            for (Entity entity : event.getChunk().getEntities())
                stackManager.createStackFromEntity(entity);
        } else {
            stackManager.loadChunk(event.getChunk());
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        this.roseStacker.getStackManager().unloadChunk(event.getChunk());
    }

}
