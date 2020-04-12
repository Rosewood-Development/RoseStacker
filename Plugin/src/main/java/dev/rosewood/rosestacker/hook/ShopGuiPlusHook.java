package dev.rosewood.rosestacker.hook;

import dev.rosewood.rosestacker.RoseStacker;
import net.brcdev.shopgui.ShopGuiPlusApi;

public final class ShopGuiPlusHook {

    /**
     * Registers the spawner provider with ShopGuiPlus
     *
     * @param roseStacker The plugin instance
     */
    public static void setupSpawners(RoseStacker roseStacker) {
        ShopGuiPlusApi.registerSpawnerProvider(new RoseStackerSpawnerProvider(roseStacker));
    }

}
