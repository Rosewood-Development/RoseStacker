package dev.rosewood.rosestacker.hook;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.manager.StackManager;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.exception.api.ExternalSpawnerProviderNameConflictException;

public final class ShopGuiPlusHook {

    /**
     * Registers the spawner provider with ShopGuiPlus
     *
     * @param rosePlugin The plugin instance
     */
    public static void setupSpawners(RosePlugin rosePlugin) {
        if (rosePlugin.getManager(StackManager.class).isSpawnerStackingEnabled()) {
            try {
                ShopGuiPlusApi.registerSpawnerProvider(new RoseStackerSpawnerProvider(rosePlugin));
            } catch (ExternalSpawnerProviderNameConflictException e) {
                e.printStackTrace();
            }
        }
    }

}
