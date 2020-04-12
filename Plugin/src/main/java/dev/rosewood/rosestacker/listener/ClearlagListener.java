package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackingThread;
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
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        StackingThread stackingThread = stackManager.getStackingThread(event.getWorld());
        if (stackingThread == null)
            return;

        if (Setting.MISC_CLEARLAG_CLEAR_ENTITIES.getBoolean() && stackManager.isEntityStackingEnabled())
            stackingThread.removeAllEntityStacks();

        if (Setting.MISC_CLEARLAG_CLEAR_ITEMS.getBoolean() && stackManager.isItemStackingEnabled())
            stackingThread.removeAllItemStacks();
    }

}
