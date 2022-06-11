package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.utils.DataUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEnterBlockEvent;

public class BeeListener implements Listener {

    private final RosePlugin rosePlugin;

    public BeeListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBeeEnterHive(EntityEnterBlockEvent event) {
        if (event.getEntityType() != EntityType.BEE)
            return;

        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (stackManager.isWorldDisabled(event.getBlock().getWorld()))
            return;

        if (!stackManager.isEntityStackingEnabled())
            return;

        Bee beeEntity = (Bee) event.getEntity();

        StackedEntity stackedEntity = stackManager.getStackedEntity(beeEntity);
        if (stackedEntity == null)
            return;

        if (stackedEntity.getStackSize() == 1) {
            stackManager.removeEntityStack(stackedEntity);
            return;
        }

        stackedEntity.decreaseStackSize();
    }

}
