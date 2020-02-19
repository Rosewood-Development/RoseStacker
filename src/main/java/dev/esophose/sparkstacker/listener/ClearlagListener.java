package dev.esophose.sparkstacker.listener;

import dev.esophose.sparkstacker.SparkStacker;
import dev.esophose.sparkstacker.manager.ConfigurationManager.Setting;
import dev.esophose.sparkstacker.manager.StackManager;
import dev.esophose.sparkstacker.stack.StackingThread;
import me.minebuilders.clearlag.events.EntityRemoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ClearlagListener implements Listener {

    private SparkStacker sparkStacker;

    public ClearlagListener(SparkStacker sparkStacker) {
        this.sparkStacker = sparkStacker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClear(EntityRemoveEvent event) {
        StackManager stackManager = this.sparkStacker.getStackManager();
        StackingThread stackingThread = stackManager.getStackingThread(event.getWorld());
        if (stackingThread == null)
            return;

        if (Setting.MISC_CLEARLAG_CLEAR_ENTITIES.getBoolean())
            stackingThread.removeAllEntityStacks();

        if (Setting.MISC_CLEARLAG_CLEAR_ITEMS.getBoolean())
            stackingThread.removeAllItemStacks();
    }

}
