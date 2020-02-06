package dev.esophose.sparkstacker.utils.reflection;

import org.bukkit.Bukkit;

public final class ReflectionUtils {

    public static final String packageVersion;
    public static final int versionNumber;

    static {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        packageVersion = version.substring(version.lastIndexOf('.') + 1);

        String name = (packageVersion + ".").substring(3);
        versionNumber = Integer.parseInt(name.substring(0, name.length() - 4));
    }

    public static Class<?> getNMSClass(String clazz) {
        try {
            return Class.forName("net.minecraft.server." + packageVersion + "." + clazz);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Class<?> getCraftClass(String clazz) {
        try {

            return Class.forName("org.bukkit.craftbukkit." + packageVersion + "." + clazz);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

}
