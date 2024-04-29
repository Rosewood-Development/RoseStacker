package dev.rosewood.rosestacker.nms;

import dev.rosewood.rosegarden.utils.NMSUtil;
import org.bukkit.Bukkit;

public final class NMSAdapter {

    private static NMSHandler nmsHandler;

    static {
        try {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            if (name.contains("R")) { // Contains NMS package version, use that
                name = name.substring(name.lastIndexOf('.') + 1);
            } else { // We started not having these identifiers with Paper starting 1.20.5
                int major = NMSUtil.getVersionNumber();
                int minor = NMSUtil.getMinorVersionNumber();
                if (major == 20 && minor == 5) {
                    name = "v1_20_R4";
                }
            }

            nmsHandler = (NMSHandler) Class.forName("dev.rosewood.rosestacker.nms." + name + ".NMSHandlerImpl").getConstructor().newInstance();
        } catch (Exception ignored) { }
    }

    /**
     * @return true if this server version is supported, false otherwise
     */
    public static boolean isValidVersion() {
        return nmsHandler != null;
    }

    /**
     * @return the instance of the NMSHandler, or null if this server version is not supported
     */
    public static NMSHandler getHandler() {
        return nmsHandler;
    }

    /**
     * @return true if the server is running Paper or a fork of Paper, false otherwise
     */
    public static boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
