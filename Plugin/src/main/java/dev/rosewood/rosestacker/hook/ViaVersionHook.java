package dev.rosewood.rosestacker.hook;

import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;

public class ViaVersionHook {

    public static void suppressMetadataErrors() {
        ConfigurationProvider config = ViaVersionPlugin.getInstance().getConfigurationProvider();
        // If we don't set this, the console will get spammed due to the dynamic tags
        config.set("suppress-metadata-errors", true);
        config.saveConfig();
        config.reloadConfig();
    }

}
