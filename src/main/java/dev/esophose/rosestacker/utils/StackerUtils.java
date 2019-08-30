package dev.esophose.rosestacker.utils;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;

public class StackerUtils {

    /**
     * Formats a string from THIS_FORMAT to This Format
     *
     * @param name The name to format
     * @return the reformatted string
     */
    public static String formatName(String name) {
        return WordUtils.capitalizeFully(name.toLowerCase().replace('_', ' '));
    }

    /**
     * Gets a location as a string key
     *
     * @param location The location
     * @return the location as a string key
     */
    public static String locationAsKey(Location location) {
        return String.format("%s-%.2f-%.2f-%.2f", location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

}
