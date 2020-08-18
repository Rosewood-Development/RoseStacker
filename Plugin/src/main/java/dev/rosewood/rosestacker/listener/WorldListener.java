package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.manager.StackManager;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldListener implements Listener {

    private RosePlugin rosePlugin;
    private StackManager stackManager;

    public WorldListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
        this.stackManager = this.rosePlugin.getManager(StackManager.class);;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) {
            // Stack new entities
            if (this.stackManager.isEntityStackingEnabled())
                for (Entity entity : event.getChunk().getEntities())
                    if (entity instanceof LivingEntity)
                        this.stackManager.createEntityStack((LivingEntity) entity, true);

            // Stack new spawners
            if (this.stackManager.isSpawnerStackingEnabled())
                for (BlockState tileEntity : event.getChunk().getTileEntities())
                    if (tileEntity instanceof CreatureSpawner)
                        this.stackManager.createSpawnerStack(tileEntity.getBlock(), 1);
        } else {
            this.stackManager.loadChunk(event.getChunk());
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        this.stackManager.unloadChunk(event.getChunk());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        this.stackManager.loadWorld(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        this.stackManager.unloadWorld(event.getWorld());
    }

}
