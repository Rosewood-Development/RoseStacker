package dev.rosewood.rosestacker.hook;

public class ViaVersionHook {

    public static void suppressMetadataErrors() {
        // ViaVersion has changed its package name in the newer dev builds
        boolean oldPackage;
        try {
            Class.forName("us.myles.ViaVersion.ViaVersionPlugin");
            oldPackage = true;
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.viaversion.viaversion.ViaVersionPlugin");
                oldPackage = false;
            } catch (ClassNotFoundException e2) {
                return;
            }
        }

        // Try to suppress metadata errors so the dynamic tags don't spam the console
        if (oldPackage) {
            us.myles.ViaVersion.api.configuration.ConfigurationProvider config = us.myles.ViaVersion.ViaVersionPlugin.getInstance().getConfigurationProvider();
            config.set("suppress-metadata-errors", true);
            config.saveConfig();
            config.reloadConfig();
        } else {
            com.viaversion.viaversion.api.configuration.ConfigurationProvider config = com.viaversion.viaversion.ViaVersionPlugin.getInstance().getConfigurationProvider();
            config.set("suppress-metadata-errors", true);
            config.saveConfig();
            config.reloadConfig();
        }
    }

}
