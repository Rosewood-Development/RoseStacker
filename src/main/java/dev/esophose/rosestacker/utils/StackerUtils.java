package dev.esophose.rosestacker.utils;

import org.apache.commons.lang.WordUtils;

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

}
