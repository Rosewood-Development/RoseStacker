package dev.esophose.sparkstacker.listener;

import dev.esophose.sparkstacker.SparkStacker;
import dev.esophose.sparkstacker.manager.StackManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldListener implements Listener {

    private SparkStacker sparkStacker;

    public WorldListener(SparkStacker sparkStacker) {
        this.sparkStacker = sparkStacker;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        StackManager stackManager = this.sparkStacker.getStackManager();

        if (event.isNewChunk()) {
            for (Entity entity : event.getChunk().getEntities())
                if (entity instanceof LivingEntity)
                    stackManager.createEntityStack((LivingEntity) entity, true);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(this.sparkStacker, () -> stackManager.loadChunk(event.getChunk()));
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this.sparkStacker, () -> this.sparkStacker.getStackManager().unloadChunk(event.getChunk()));
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        System.out.println("LOADED WORLD: " + event.getWorld().getName());
        this.sparkStacker.getStackManager().loadWorld(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        this.sparkStacker.getStackManager().unloadWorld(event.getWorld());
    }

}
