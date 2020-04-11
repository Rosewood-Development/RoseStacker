package dev.esophose.rosestacker.listener;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.stack.StackedEntity;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEnterBlockEvent;

public class BeeListener implements Listener {

    private RoseStacker roseStacker;

    public BeeListener(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBeeEnterHive(EntityEnterBlockEvent event) {
        if (event.getEntityType() != EntityType.BEE)
            return;

        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
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

        stackedEntity.split();
    }

}
