package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.utils.NMSUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;

@SuppressWarnings("deprecation")
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
    }

    private static Enchantment findEnchantmentLegacy(String... names) {
        for (String name : names) {
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(name));
            if (enchantment != null)
                return enchantment;
        }
        return null;
    }

}
