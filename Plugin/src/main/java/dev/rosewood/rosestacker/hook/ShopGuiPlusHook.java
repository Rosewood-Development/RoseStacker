package dev.rosewood.rosestacker.hook;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.manager.StackManager;
import net.brcdev.shopgui.ShopGuiPlusApi;

public final class ShopGuiPlusHook {

    /**
     * Registers the spawner provider with ShopGuiPlus
     *
     * @param rosePlugin The plugin instance
     */
    public static void setupSpawners(RosePlugin rosePlugin) {
        if (rosePlugin.getManager(StackManager.class).isSpawnerStackingEnabled())
            ShopGuiPlusApi.registerSpawnerProvider(new RoseStackerSpawnerProvider(rosePlugin));
    }

}
