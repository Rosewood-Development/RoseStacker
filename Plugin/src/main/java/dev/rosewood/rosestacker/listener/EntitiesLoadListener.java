package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;

public class EntitiesLoadListener implements Listener {

    private final RosePlugin rosePlugin;

    public EntitiesLoadListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities())
            if (entity instanceof LivingEntity)
                PersistentDataUtils.applyDisabledAi((LivingEntity) entity);

        this.rosePlugin.getManager(StackManager.class).loadChunkEntities(event.getEntities());
    }

    @EventHandler
    public void onEntitiesUnload(EntitiesUnloadEvent event) {
        this.rosePlugin.getManager(StackManager.class).saveChunkEntities(event.getEntities(), true);
    }

}
