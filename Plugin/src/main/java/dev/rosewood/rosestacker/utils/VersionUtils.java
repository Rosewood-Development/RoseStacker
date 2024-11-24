package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.utils.NMSUtil;
import java.lang.reflect.Method;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;

@SuppressWarnings({"deprecation", "removal"})
public class VersionUtils {

    public static final EntityType ITEM;
    public static final EntityType MOOSHROOM;
    public static final EntityType SNOW_GOLEM;
    public static final Particle DUST;
    public static final Particle POOF;
    public static final Particle SMOKE;
    public static final Enchantment INFINITY;
    public static final Enchantment SWEEPING_EDGE;
    public static final ItemFlag HIDE_ADDITIONAL_TOOLTIP;
    public static final Attribute MAX_HEALTH;
    public static final Attribute KNOCKBACK_RESISTANCE;
    public static final Attribute LUCK;
    static {
        if (NMSUtil.getVersionNumber() > 20 || (NMSUtil.getVersionNumber() == 20 && NMSUtil.getMinorVersionNumber() >= 5)) {
            ITEM = EntityType.ITEM;
            MOOSHROOM = EntityType.MOOSHROOM;
            SNOW_GOLEM = EntityType.SNOW_GOLEM;
            POOF = Particle.POOF;
            SMOKE = Particle.SMOKE;
            DUST = Particle.DUST;
            INFINITY = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("infinity"));
            SWEEPING_EDGE = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("sweeping_edge"));
            HIDE_ADDITIONAL_TOOLTIP = ItemFlag.HIDE_ADDITIONAL_TOOLTIP;
        } else {
            ITEM = EntityType.valueOf("DROPPED_ITEM");
            MOOSHROOM = EntityType.valueOf("MUSHROOM_COW");
            SNOW_GOLEM = EntityType.valueOf("SNOWMAN");
            POOF = Particle.valueOf("EXPLOSION_NORMAL");
            SMOKE = Particle.valueOf("SMOKE_NORMAL");
            DUST = Particle.valueOf("REDSTONE");
            INFINITY = findEnchantmentLegacy("infinity", "arrow_infinite");
            SWEEPING_EDGE = findEnchantmentLegacy("sweeping", "sweeping_edge");
            HIDE_ADDITIONAL_TOOLTIP = ItemFlag.valueOf("HIDE_POTION_EFFECTS");
        }

        if (NMSUtil.getVersionNumber() > 21 || NMSUtil.getVersionNumber() == 21 && NMSUtil.getMinorVersionNumber() >= 3) {
            MAX_HEALTH = Attribute.MAX_HEALTH;
            KNOCKBACK_RESISTANCE = Attribute.KNOCKBACK_RESISTANCE;
            LUCK = Attribute.LUCK;
        } else {
            MAX_HEALTH = findAttributeLegacy("GENERIC_MAX_HEALTH");
            KNOCKBACK_RESISTANCE = findAttributeLegacy("GENERIC_KNOCKBACK_RESISTANCE");
            LUCK = findAttributeLegacy("GENERIC_LUCK");
        }
    }

    private static Enchantment findEnchantmentLegacy(String... names) {
        for (String name : names) {
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(name));
            if (enchantment != null)
                return enchantment;
        }
        return null;
    }

    private static Method attributeValueOf;
    private static Attribute findAttributeLegacy(String name) {
        try {
            if (attributeValueOf == null)
                attributeValueOf = Attribute.class.getMethod("valueOf", String.class);
            return (Attribute) attributeValueOf.invoke(null, name);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Method biomeValueOf;
    public static Biome getBiome(String name) {
        if (NMSUtil.getVersionNumber() > 21 || NMSUtil.getVersionNumber() == 21 && NMSUtil.getMinorVersionNumber() >= 3) {
            return Registry.BIOME.match(name);
        } else {
            try {
                if (biomeValueOf == null)
                    biomeValueOf = Biome.class.getMethod("valueOf", String.class);
                return (Biome) biomeValueOf.invoke(null, name.toUpperCase());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
