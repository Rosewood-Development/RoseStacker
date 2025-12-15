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
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getWorld()))
            return;

        for (Entity entity : event.getEntities())
            if (entity instanceof LivingEntity livingEntity && !stackManager.isAreaDisabled(livingEntity.getLocation()))
                PersistentDataUtils.applyDisabledAi(livingEntity);

        this.rosePlugin.getManager(StackManager.class).loadChunkEntities(event.getEntities());
    }

    @EventHandler
    public void onEntitiesUnload(EntitiesUnloadEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getWorld()))
            return;

        this.rosePlugin.getManager(StackManager.class).saveChunkEntities(event.getEntities(), true);
    }

}
