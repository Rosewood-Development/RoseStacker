package dev.rosewood.rosestacker.nms;

import org.bukkit.Bukkit;

public final class NMSUtil {

    private static NMSHandler nmsHandler;
    private static String cachedVersion = null;
    private static int cachedVersionNumber = -1;

    static {
        try {
            nmsHandler = (NMSHandler) Class.forName("dev.rosewood.rosestacker.nms." + getVersion() + ".NMSHandlerImpl").getConstructor().newInstance();
        } catch (Exception ignored) { }
    }

    public static boolean isValidVersion() {
        return nmsHandler != null;
    }

    public static NMSHandler getHandler() {
        return nmsHandler;
    }

    /**
     * @return The server version
     */
    public static String getVersion() {
        if (cachedVersion == null) {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            cachedVersion = name.substring(name.lastIndexOf('.') + 1);
        }
        return cachedVersion;
    }

    /**
     * @return the server version major release number
     */
    public static int getVersionNumber() {
        if (cachedVersionNumber == -1) {
            String name = getVersion().substring(3);
            cachedVersionNumber = Integer.parseInt(name.substring(0, name.length() - 3));
        }
        return cachedVersionNumber;
    }

}
