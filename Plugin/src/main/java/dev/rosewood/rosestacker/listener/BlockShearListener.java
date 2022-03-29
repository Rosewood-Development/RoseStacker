package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockShearEntityEvent;

public class BlockShearListener implements Listener {

    private final RosePlugin rosePlugin;

    public BlockShearListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockShearSheep(BlockShearEntityEvent event) {
        EntityListener.handleSheepShear(this.rosePlugin, event.getEntity());
    }

}
