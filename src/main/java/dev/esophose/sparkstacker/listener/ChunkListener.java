package dev.esophose.sparkstacker.listener;

import dev.esophose.sparkstacker.SparkStacker;
import dev.esophose.sparkstacker.manager.StackManager;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {

    private SparkStacker sparkStacker;

    public ChunkListener(SparkStacker sparkStacker) {
        this.sparkStacker = sparkStacker;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        StackManager stackManager = this.sparkStacker.getStackManager();

        if (event.isNewChunk()) {
            for (Entity entity : event.getChunk().getEntities())
                stackManager.createStackFromEntity(entity, true);
        } else {
            stackManager.loadChunk(event.getChunk());
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        this.sparkStacker.getStackManager().unloadChunk(event.getChunk());
    }

}
