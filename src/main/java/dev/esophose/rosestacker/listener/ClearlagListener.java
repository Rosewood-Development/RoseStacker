package dev.esophose.rosestacker.listener;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.stack.StackingThread;
import me.minebuilders.clearlag.events.EntityRemoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ClearlagListener implements Listener {

    private RoseStacker roseStacker;

    public ClearlagListener(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClear(EntityRemoveEvent event) {
        StackManager stackManager = this.roseStacker.getStackManager();
        StackingThread stackingThread = stackManager.getStackingThread(event.getWorld());
        if (stackingThread == null)
            return;

        if (Setting.MISC_CLEARLAG_CLEAR_ENTITIES.getBoolean())
            stackingThread.removeAllEntityStacks();

        if (Setting.MISC_CLEARLAG_CLEAR_ITEMS.getBoolean())
            stackingThread.removeAllItemStacks();
    }

}
