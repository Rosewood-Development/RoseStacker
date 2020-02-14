package dev.esophose.sparkstacker.hook;

import dev.esophose.sparkstacker.SparkStacker;
import net.brcdev.shopgui.ShopGuiPlusApi;

public final class ShopGuiPlusHook {

    /**
     * Registers the spawner provider with ShopGuiPlus
     *
     * @param sparkStacker The plugin instance
     */
    public static void setupSpawners(SparkStacker sparkStacker) {
        ShopGuiPlusApi.registerSpawnerProvider(new SparkStackerSpawnerProvider(sparkStacker));
    }

}
