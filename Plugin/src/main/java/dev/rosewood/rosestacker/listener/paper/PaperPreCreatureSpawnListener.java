package dev.rosewood.rosestacker.listener.paper;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class PaperPreCreatureSpawnListener implements Listener {

    private final EntityCacheManager entityCacheManager;

    public PaperPreCreatureSpawnListener(RosePlugin rosePlugin) {
        this.entityCacheManager = rosePlugin.getManager(EntityCacheManager.class);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPreCreatureSpawn(PreCreatureSpawnEvent event) {

    }

}
