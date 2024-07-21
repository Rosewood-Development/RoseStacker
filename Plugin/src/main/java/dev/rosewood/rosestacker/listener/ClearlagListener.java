package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackingThread;
import me.minebuilders.clearlag.events.EntityRemoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ClearlagListener implements Listener {

    private final RosePlugin rosePlugin;

    public ClearlagListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClear(EntityRemoveEvent event) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        StackingThread stackingThread = stackManager.getStackingThread(event.getWorld());
        if (stackingThread == null)
            return;

        if (SettingKey.MISC_CLEARLAG_CLEAR_ENTITIES.get() && stackManager.isEntityStackingEnabled())
            stackingThread.removeAllEntityStacks();

        if (SettingKey.MISC_CLEARLAG_CLEAR_ITEMS.get() && stackManager.isItemStackingEnabled())
            stackingThread.removeAllItemStacks();
    }

}
