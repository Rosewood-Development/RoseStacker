package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.utils.EntitySpawnUtil;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.manager.LocaleManager;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;

public final class StackerUtils {

    public static final String MIN_SUPPORTED_VERSION = "1.16.5";
    public static final String MAX_SUPPORTED_VERSION = "1.21.7";
    public static final String MAX_SUPPORTED_LOCALE_VERSION = "1.21.7";

    public static final int ASSUMED_ENTITY_VISIBILITY_RANGE = 75 * 75;

    public static final DustOptions STACKABLE_DUST_OPTIONS = new DustOptions(Color.fromRGB(0x00FF00), 1.5F);
    public static final DustOptions UNSTACKABLE_DUST_OPTIONS = new DustOptions(Color.fromRGB(0xFF0000), 1.5F);

    private static final Random RANDOM = new Random();
    private static Set<EntityType> cachedStackableEntityTypes;

    private static NumberFormat formatter = NumberFormat.getInstance();

    /**
     * Formats a string from THIS_FORMAT to This Format
     *
     * @param name The name to format
     * @return the reformatted string
     */
    public static String formatName(String name) {
        return Arrays.stream(name.replace('_', ' ').split("\\s+"))
                .map(x -> x.substring(0, 1).toUpperCase() + x.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /**
     * Formats a Material name from THIS_FORMAT to This Format
     *
     * @param material The material to format the name of
     * @return the reformatted string
     */
    public static String formatMaterialName(Material material) {
        if (material == Material.TNT)
            return "TNT"; // The one exception
        return formatName(material.name());
    }

    /**
     * Gets a random value between the given range, inclusively
     *
     * @param min The minimum value
     * @param max The maximum value
     * @return A value between the min and max, inclusively
     */
    public static int randomInRange(int min, int max) {
        if (min == max)
            return min;

        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return RANDOM.nextInt(max - min + 1) + min;
    }

    public static Set<EntityType> getStackableEntityTypes() {
        if (cachedStackableEntityTypes != null)
            return cachedStackableEntityTypes;

        return cachedStackableEntityTypes = Stream.of(EntityType.values())
                .filter(EntityType::isAlive)
                .filter(EntityType::isSpawnable)
                .filter(x -> x != EntityType.PLAYER && x != EntityType.ARMOR_STAND)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EntityType.class)));
    }

    public static boolean passesChance(double chance) {
        return RANDOM.nextDouble() <= chance;
    }

    /**
     * Calculates the number of times a chance passes against a certain number of attempts
     *
     * @param chance The percent chance, 0-1, that the attempt will pass
     * @param attempts The number of times to attempt the chance
     * @return The number of times that the chance passed in the number of attempts
     */
    public static int countPassedChances(double chance, int attempts) {
        if (chance == 0) return 0;
        if (chance == 1) return attempts;

        if (attempts < 128) {
            int passes = 0;
            for (int i = 0; i < attempts; i++)
                if (passesChance(chance))
                    passes++;
            return passes;
        }

        double mean = attempts * chance;
        double stdDev = Math.sqrt(attempts * chance) * (1 - chance);
        return (int) Math.round(RANDOM.nextGaussian(mean, stdDev));
    }

    /**
     * Drops experience at a given location
     *
     * @param location to spawn experience
     * @param lowerBound minimum amount to drop
     * @param upperBound maximum amount to drop
     * @param step the max size an orb can be, will drop multiple orbs if this is exceeded
     */
    public static void dropExperience(Location location, long lowerBound, long upperBound, int step) {
        World world = location.getWorld();
        if (world == null)
            return;

        long experience = RANDOM.nextLong(upperBound - lowerBound + 1) + lowerBound;

        int chunkAmount = Math.max(2, step); // Prevent infinite loops and always use at minimum a step of 2
        while (experience > chunkAmount) {
            EntitySpawnUtil.spawn(location.clone().add(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5), ExperienceOrb.class, x -> x.setExperience(chunkAmount));
            experience -= chunkAmount;
        }

        if (experience > 0) {
            int fExperience = (int) experience;
            EntitySpawnUtil.spawn(location.clone().add(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5), ExperienceOrb.class, x -> x.setExperience(fExperience));
        }
    }

    /**
     * @return a stream of all block materials that can be considered to be used for stacked blocks
     */
    public static List<Material> getPossibleStackableBlockMaterials() {
        return Arrays.stream(Material.values())
                .filter(x -> !x.isLegacy())
                .filter(Material::isBlock)
                .filter(Material::isItem)
                .filter(Material::isSolid)
                .filter(x -> !isInteractable(x))
                .filter(x -> !Tag.CORAL_PLANTS.isTagged(x))
                .filter(x -> !Tag.SLABS.isTagged(x))
                .filter(x -> !Tag.BANNERS.isTagged(x))
                .filter(x -> !x.name().endsWith("_WALL")) // Tags for these don't exist in older versions
                .filter(x -> !x.name().endsWith("_PRESSURE_PLATE"))
                .sorted(Comparator.comparing(Enum::name)).toList();
    }

    public static boolean isInteractable(Material material) {
        return switch (material.getKey().getKey()) {
            case "redstone_ore",
                 "deepslate_redstone_ore",
                 "beacon",
                 "tnt",
                 "dragon_egg" -> false;
            default -> material.isInteractable();
        };
    }

    /**
     * Checks if a Material can not be passed through
     *
     * @param material The Material to check
     * @return true if the Material can be passed through, false otherwise
     */
    public static boolean isOccluding(Material material) {
        if (material.name().contains("GLASS")
                || material.name().endsWith("_STAINED_GLASS_PANE")
                || material.name().contains("FENCE")
                || material.name().endsWith("SLAB")
                || material.name().endsWith("WALL"))
            return true;

        return switch (material) {
            case CHEST, ENDER_CHEST, TRAPPED_CHEST, ICE -> true;
            default -> material.isOccluding();
        };
    }

    /**
     * Checks if a Material is air
     *
     * @param material The Material to check
     * @return true if the Material is a type of air
     */
    public static boolean isAir(Material material) {
        return switch (material) {
            case AIR, CAVE_AIR, VOID_AIR -> true;
            default -> false;
        };
    }

    /**
     * Formats a number to a string with a number grouping separator
     *
     * @param value The numerical value to format
     * @return The formatted string
     */
    public static String formatNumber(long value) {
        return formatter.format(value);
    }

    public static String formatTicksAsTime(long value) {
        long seconds = value / 20;
        long minutes = seconds / 60;

        if (minutes > 0) {
            seconds -= minutes * 60;
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Gets an integer value from a Permissible's permissions, picking the highest value out of the ones available.
     * The lowerbound will be the lowest the value can possibly be. To use, pass in a permission such as
     * "example.permission" and the integer value will be located at "example.permission.<#>"
     *
     * @param permissible The Permissible
     * @param permission The permission prefix
     * @param lowerBound The lowerbound of the value
     * @return the highest value found within the Permissible's permissions
     */
    public static int getPermissionDefinableValue(Permissible permissible, String permission, int lowerBound) {
        int amount = lowerBound;
        for (PermissionAttachmentInfo info : permissible.getEffectivePermissions()) {
            String target = info.getPermission().toLowerCase();
            if (target.startsWith(permission) && info.getValue()) {
                try {
                    amount = Math.max(amount, Integer.parseInt(target.substring(target.lastIndexOf('.') + 1)));
                } catch (NumberFormatException ignored) { }
            }
        }
        return amount;
    }

    public static void clearCache() {
        cachedStackableEntityTypes = null;
        EntityUtils.clearCache();
        ItemUtils.clearCache();

        String separator = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("number-separator");
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator(!separator.isEmpty() ? separator.charAt(0) : ',');
        formatter = new DecimalFormat("#,##0", symbols);
    }

    public static int getLuckLevel(Player player) {
        double luck = 0;
        AttributeInstance attribute = player.getAttribute(VersionUtils.LUCK);
        if (attribute != null)
            luck += attribute.getValue();
        return (int) Math.floor(luck);
    }

    public static double getSilkTouchChanceRaw(Player player) {
        double chance = StackerUtils.getPermissionDefinableValue(player, "rosestacker.silktouch.chance", SettingKey.SPAWNER_SILK_TOUCH_CHANCE.get());
        chance += SettingKey.SPAWNER_SILK_TOUCH_LUCK_CHANCE_INCREASE.get() * StackerUtils.getLuckLevel(player);
        return chance;
    }

    public static boolean isVanished(Player player) {
        return player.getMetadata("vanished").stream().anyMatch(MetadataValue::asBoolean);
    }

}
