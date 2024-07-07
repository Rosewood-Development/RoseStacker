package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import java.util.Arrays;
import org.bukkit.Chunk;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldListener implements Listener {

    private final StackManager stackManager;

    public WorldListener(RosePlugin rosePlugin) {
        this.stackManager = rosePlugin.getManager(StackManager.class);
    }

    /**
     * 1.17 loads entities async and is handled in {@link EntitiesLoadListener} instead
     *
     * @param event The ChunkLoadEvent
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (this.stackManager.isWorldDisabled(event.getWorld()))
            return;

        Chunk chunk = event.getChunk();
        if (event.isNewChunk()) {
            // Stack new entities
            if (NMSUtil.getVersionNumber() < 17 && this.stackManager.isEntityStackingEnabled())
                for (Entity entity : chunk.getEntities())
                    if (entity instanceof LivingEntity)
                        this.stackManager.createEntityStack((LivingEntity) entity, true);

            // Stack new spawners
            if (this.stackManager.isSpawnerStackingEnabled())
                for (BlockState tileEntity : chunk.getTileEntities())
                    if (tileEntity instanceof CreatureSpawner)
                        this.stackManager.createSpawnerStack(tileEntity.getBlock(), 1, false);
        } else {
            if (NMSUtil.getVersionNumber() < 17) {
                // Make sure AI is disabled if it's marked
                Entity[] entities = chunk.getEntities();
                for (Entity entity : entities)
                    if (entity instanceof LivingEntity)
                        PersistentDataUtils.applyDisabledAi((LivingEntity) entity);

                this.stackManager.loadChunkEntities(Arrays.asList(entities));
            }

            this.stackManager.loadChunkBlocks(chunk);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        this.stackManager.saveChunkBlocks(event.getChunk(), true);

        if (NMSUtil.getVersionNumber() < 17)
            this.stackManager.saveChunkEntities(Arrays.asList(event.getChunk().getEntities()), true);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        this.stackManager.loadWorld(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        this.stackManager.unloadWorld(event.getWorld());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ThreadUtils.runAsync(this.stackManager::processNametags);
    }

}
