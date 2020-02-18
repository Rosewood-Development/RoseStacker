package dev.esophose.sparkstacker.listener;

import dev.esophose.sparkstacker.SparkStacker;
import dev.esophose.sparkstacker.manager.StackManager;
import dev.esophose.sparkstacker.stack.StackedEntity;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEnterBlockEvent;

public class BeeListener implements Listener {

    private SparkStacker sparkStacker;

    public BeeListener(SparkStacker sparkStacker) {
        this.sparkStacker = sparkStacker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBeeEnterHive(EntityEnterBlockEvent event) {
        if (event.getEntityType() != EntityType.BEE)
            return;

        StackManager stackManager = this.sparkStacker.getStackManager();
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
