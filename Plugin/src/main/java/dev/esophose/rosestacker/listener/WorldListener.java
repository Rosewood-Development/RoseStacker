package dev.esophose.rosestacker.listener;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.StackManager;
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

    private RoseStacker roseStacker;

    public WorldListener(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);

        if (event.isNewChunk()) {
            for (Entity entity : event.getChunk().getEntities())
                if (entity instanceof LivingEntity)
                    stackManager.createEntityStack((LivingEntity) entity, true);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(this.roseStacker, () -> stackManager.loadChunk(event.getChunk()));
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this.roseStacker, () -> this.roseStacker.getManager(StackManager.class).unloadChunk(event.getChunk()));
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        this.roseStacker.getManager(StackManager.class).loadWorld(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        this.roseStacker.getManager(StackManager.class).unloadWorld(event.getWorld());
    }

}
