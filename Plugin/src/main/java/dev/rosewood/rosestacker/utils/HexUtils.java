package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosestacker.nms.NMSUtil;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

public final class HexUtils {

    private static final Pattern RAINBOW_PATTERN = Pattern.compile("<rainbow(:\\d*\\.?\\d+){0,2}>(.*)</rainbow>");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient(:#([A-Fa-f0-9]){6})*>(.*)</gradient>");
    private static final List<Pattern> HEX_PATTERNS = Arrays.asList(
            Pattern.compile("<#([A-Fa-f0-9]){6}>"), // <#FFFFFF>
            Pattern.compile("&#([A-Fa-f0-9]){6}"),  // &#FFFFFF
            Pattern.compile("#([A-Fa-f0-9]){6}")    // #FFFFFF
    );

    private HexUtils() {

    }

    /**
     * Sends a CommandSender a colored message
     *
     * @param sender The CommandSender to send to
     * @param message The message to send
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorify(message));
    }

    /**
     * Parses gradients, hex colors, and legacy color codes
     *
     * @param message The message
     * @return A color-replaced message
     */
    public static String colorify(String message) {
        String parsed = message;
        parsed = parseRainbow(parsed);
        parsed = parseGradients(parsed);
        parsed = parseHex(parsed);
        parsed = parseLegacy(parsed);
        return parsed;
    }

    private static String parseRainbow(String message) {
        String parsed = message;

        int startTagLength = 8;
        int endTagLength = 10;

        Matcher matcher = RAINBOW_PATTERN.matcher(parsed);
        while (matcher.find()) {
            StringBuilder parsedRainbow = new StringBuilder();

            String match = matcher.group();
            int indexOfClose = match.indexOf(">");
            String extraDataContent = match.substring(startTagLength, indexOfClose);

            double[] extraData;
            if (!extraDataContent.isEmpty()) {
                extraDataContent = extraDataContent.substring(1);
                extraData = Arrays.stream(extraDataContent.split(":")).mapToDouble(Double::parseDouble).toArray();
            } else {
                extraData = new double[0];
            }

            float hue = 0;
            float saturation = extraData.length > 0 ? (float) extraData[0] : 1.0F;
            float brightness = extraData.length > 1 ? (float) extraData[1] : 1.0F;

            String content = match.substring(indexOfClose + 1, match.length() - endTagLength);
            float hueStep = 1.0F / content.length();

            int i = 0;
            while (i < content.length()) {
                char c = content.charAt(i);
                Color color = Color.getHSBColor(hue, saturation, brightness);

                parsedRainbow.append(translateHex(color)).append(c);

                i++;
                hue += hueStep;
            }

            String before = parsed.substring(0, matcher.start());
            String after = parsed.substring(matcher.end());
            parsed = before + parsedRainbow + after;
            matcher = RAINBOW_PATTERN.matcher(parsed);
        }

        return parsed;
    }

    private static String parseGradients(String message) {
        String parsed = message;

        int startTagLength = 10;
        int endTagLength = 11;

        Matcher matcher = GRADIENT_PATTERN.matcher(parsed);
        while (matcher.find()) {
            StringBuilder parsedGradient = new StringBuilder();

            String match = matcher.group();
            int indexOfClose = match.indexOf(">");
            String hexContent = match.substring(startTagLength, indexOfClose);
            List<Color> hexSteps = Arrays.stream(hexContent.split(":")).map(Color::decode).collect(Collectors.toList());

            String content = match.substring(indexOfClose + 1, match.length() - endTagLength);
            int stepSize = content.length() / (hexSteps.size() - 1);

            int i = 0, step = 0, stepIndex = 0;
            while (i < content.length()) {
                char c = content.charAt(i);

                Color color;
                if (stepIndex + 1 < hexSteps.size()) {
                    Color start = hexSteps.get(stepIndex);
                    Color end = hexSteps.get(stepIndex + 1);
                    float interval = (float) step / stepSize;
                    color = getGradientInterval(start, end, interval);
                } else {
                    color = hexSteps.get(hexSteps.size() - 1);
                }

                parsedGradient.append(translateHex(color)).append(c);

                i++;
                step += 1;
                if (step >= stepSize) {
                    step = 0;
                    stepIndex++;
                }
            }

            String before = parsed.substring(0, matcher.start());
            String after = parsed.substring(matcher.end());
            parsed = before + parsedGradient + after;
            matcher = GRADIENT_PATTERN.matcher(parsed);
        }

        return parsed;
    }

    private static String parseHex(String message) {
        String parsed = message;

        for (Pattern pattern : HEX_PATTERNS) {
            Matcher matcher = pattern.matcher(parsed);
            while (matcher.find()) {
                String color = translateHex(cleanHex(matcher.group()));
                String before = parsed.substring(0, matcher.start());
                String after = parsed.substring(matcher.end());
                parsed = before + color + after;
                matcher = pattern.matcher(parsed);
            }
        }

        return parsed;
    }

    private static String parseLegacy(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private static String cleanHex(String hex) {
        if (hex.startsWith("<")) {
            return hex.substring(1, hex.length() - 1);
        } else if (hex.startsWith("&")) {
            return hex.substring(1);
        } else {
            return hex;
        }
    }

    /**
     * Gets a color along a linear gradient between two colors
     *
     * @param start The start color
     * @param end The end color
     * @param interval The interval to get, between 0 and 1 inclusively
     * @return A Color at the interval between the start and end colors
     */
    public static Color getGradientInterval(Color start, Color end, float interval) {
        if (0 > interval || interval > 1)
            throw new IllegalArgumentException("Interval must be between 0 and 1 inclusively.");

        int r = (int) (end.getRed() * interval + start.getRed() * (1 - interval));
        int g = (int) (end.getGreen() * interval + start.getGreen() * (1 - interval));
        int b = (int) (end.getBlue() * interval + start.getBlue() * (1 - interval));

        return new Color(r, g, b);
    }

    /**
     * Finds the closest hex or ChatColor value as the hex string
     *
     * @param hex The hex color
     * @return The closest ChatColor value
     */
    private static String translateHex(String hex) {
        if (NMSUtil.getVersionNumber() >= 16)
            return ChatColor.of(hex).toString();
        return translateHex(Color.decode(hex));
    }

    private static String translateHex(Color color) {
        if (NMSUtil.getVersionNumber() >= 16)
            return ChatColor.of(color).toString();

        int minDist = Integer.MAX_VALUE;
        ChatColor legacy = ChatColor.WHITE;
        for (ChatColorHexMapping mapping : ChatColorHexMapping.values()) {
            int r = mapping.getRed() - color.getRed();
            int g = mapping.getGreen() - color.getGreen();
            int b = mapping.getBlue() - color.getBlue();
            int dist = r * r + g * g + b * b;
            if (dist < minDist) {
                minDist = dist;
                legacy = mapping.getChatColor();
            }
        }

        return legacy.toString();
    }

    private enum ChatColorHexMapping {
        BLACK(0x000000, ChatColor.BLACK),
        DARK_BLUE(0x0000AA, ChatColor.DARK_BLUE),
        DARK_GREEN(0x00AA00, ChatColor.DARK_GREEN),
        DARK_AQUA(0x00AAAA, ChatColor.DARK_AQUA),
        DARK_RED(0xAA0000, ChatColor.DARK_RED),
        DARK_PURPLE(0xAA00AA, ChatColor.DARK_PURPLE),
        GOLD(0xFFAA00, ChatColor.GOLD),
        GRAY(0xAAAAAA, ChatColor.GRAY),
        DARK_GRAY(0x555555, ChatColor.DARK_GRAY),
        BLUE(0x5555FF, ChatColor.BLUE),
        GREEN(0x55FF55, ChatColor.GREEN),
        AQUA(0x55FFFF, ChatColor.AQUA),
        RED(0xFF5555, ChatColor.RED),
        LIGHT_PURPLE(0xFF55FF, ChatColor.LIGHT_PURPLE),
        YELLOW(0xFFFF55, ChatColor.YELLOW),
        WHITE(0xFFFFFF, ChatColor.WHITE);

        private final int r, g, b;
        private final ChatColor chatColor;

        ChatColorHexMapping(int hex, ChatColor chatColor) {
            this.r = (hex >> 16) & 0xFF;
            this.g = (hex >> 8) & 0xFF;
            this.b = hex & 0xFF;
            this.chatColor = chatColor;
        }

        public int getRed() {
            return this.r;
        }

        public int getGreen() {
            return this.g;
        }

        public int getBlue() {
            return this.b;
        }

        public ChatColor getChatColor() {
            return this.chatColor;
        }
    }

}
