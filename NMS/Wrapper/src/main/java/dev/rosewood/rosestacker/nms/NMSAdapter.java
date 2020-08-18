package dev.rosewood.rosestacker.nms;

import org.bukkit.Bukkit;

public final class NMSAdapter {

    private static NMSHandler nmsHandler;

    static {
        try {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            String version = name.substring(name.lastIndexOf('.') + 1);
            nmsHandler = (NMSHandler) Class.forName("dev.rosewood.rosestacker.nms." + version + ".NMSHandlerImpl").getConstructor().newInstance();
        } catch (Exception ignored) { }
    }

    public static boolean isValidVersion() {
        return nmsHandler != null;
    }

    public static NMSHandler getHandler() {
        return nmsHandler;
    }

}
