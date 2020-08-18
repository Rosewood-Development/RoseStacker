package dev.rosewood.rosestacker.hook;

import dev.rosewood.rosegarden.RosePlugin;
import net.brcdev.shopgui.ShopGuiPlusApi;

public final class ShopGuiPlusHook {

    /**
     * Registers the spawner provider with ShopGuiPlus
     *
     * @param rosePlugin The plugin instance
     */
    public static void setupSpawners(RosePlugin rosePlugin) {
        ShopGuiPlusApi.registerSpawnerProvider(new RoseStackerSpawnerProvider(rosePlugin));
    }

}
