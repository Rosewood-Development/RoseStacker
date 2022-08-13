package dev.rosewood.rosestacker.hook;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import dev.rosewood.rosegarden.RosePlugin;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.event.ShopGUIPlusPostEnableEvent;
import net.brcdev.shopgui.exception.api.ExternalSpawnerProviderNameConflictException;

public final class ShopGuiPlusHook implements Listener {
    private final RosePlugin plugin;

    public ShopGuiPlusHook(RosePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPostEnable(ShopGUIPlusPostEnableEvent e) {
        setupSpawners();
    }

    private RosePlugin getPlugin() {
        return this.plugin;
    }

    private Logger getLogger() {
        RosePlugin plugin = getPlugin();
        return plugin.getLogger();
    }

    /**
     * Registers the spawner provider with ShopGuiPlus
     */
    private void setupSpawners() {
        try {
            RosePlugin plugin = getPlugin();
            ShopGuiPlusApi.registerSpawnerProvider(new RoseStackerSpawnerProvider(plugin));
        } catch(ExternalSpawnerProviderNameConflictException ex) {
            Logger logger = getLogger();
            logger.log(Level.WARNING, "Another plugin already registered a RoseStacker hook:", ex);
        }
    }
}
