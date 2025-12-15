package dev.rosewood.rosestacker.stack.settings.conditions.spawner;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.AboveSeaLevelConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.AboveYAxisConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.AirConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.BelowSeaLevelConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.BelowYAxisConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.BiomeConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.BlockConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.BlockExceptionConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.DarknessConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.FluidConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.LightnessConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.MaxNearbyEntityConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.NoSkylightAccessConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.NoneConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.NotPlayerPlacedConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.OnGroundConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.SkylightAccessConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.TotalDarknessConditionTag;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class ConditionTags {

    private static final Map<String, Function<String, ? extends ConditionTag>> TAG_PREFIX_MAP = new HashMap<>();
    private static final Map<Class<? extends ConditionTag>, String> CLASS_TO_PREFIX_MAP = new HashMap<>();
    private static final Map<String, String> tagDescriptionMap = new LinkedHashMap<>();

    static {
        registerTag("above-sea-level", AboveSeaLevelConditionTag.class, AboveSeaLevelConditionTag::new, "Spawn area must be above sea level");
        registerTag("above-y-axis", AboveYAxisConditionTag.class, AboveYAxisConditionTag::new, "Spawn area must be above the given y-axis. Example: `above-y-axis:30`");
        registerTag("air", AirConditionTag.class, AirConditionTag::new, "Spawn area must have air blocks available");
        registerTag("below-sea-level", BelowSeaLevelConditionTag.class, BelowSeaLevelConditionTag::new, "Spawn area must be below the world sea level");
        registerTag("below-y-axis", BelowYAxisConditionTag.class, BelowYAxisConditionTag::new, "Spawn area must be below the given y-axis. Example: `below-y-axis:30`");
        registerTag("biome", BiomeConditionTag.class, BiomeConditionTag::new, "Spawn area must be in one of the given biomes. Example: `biome:plains,desert`");
        registerTag("block", BlockConditionTag.class, BlockConditionTag::new, "Spawn area must be on one of the given blocks. Example: `block:grass_block,sand`");
        registerTag("block-exception", BlockExceptionConditionTag.class, BlockExceptionConditionTag::new, "Spawn area must not be on one of the given blocks. Example: `block-exeption:bedrock,barrier`");
        registerTag("darkness", DarknessConditionTag.class, DarknessConditionTag::new, "Spawn area must be below light level 8");
        registerTag("total-darkness", TotalDarknessConditionTag.class, TotalDarknessConditionTag::new, "Spawn area must have no light at all");
        registerTag("fluid", FluidConditionTag.class, FluidConditionTag::new, "Spawn area must be inside a fluid. Valid values are `water` and `lava`. Example: `fluid:water`");
        registerTag("lightness", LightnessConditionTag.class, LightnessConditionTag::new, "Spawn area must be above light level 8");
        registerTag("max-nearby-entities", MaxNearbyEntityConditionTag.class, MaxNearbyEntityConditionTag::new, "Spawn area must have below a certain number of entities. Example: `max-nearby-entities:6`");
        registerTag("no-skylight-access", NoSkylightAccessConditionTag.class, NoSkylightAccessConditionTag::new, "Spawn area must have no skylight access");
        registerTag("on-ground", OnGroundConditionTag.class, OnGroundConditionTag::new, "Spawn area must have a ground block to spawn on");
        registerTag("skylight-access", SkylightAccessConditionTag.class, SkylightAccessConditionTag::new, "Spawn area must have skylight access");

        // Tag for when all conditions were met, but no entities were able to spawn
        registerTag("none", NoneConditionTag.class, NoneConditionTag::new, null);

        // Tag for when only spawners placed by players can spawn mobs
        registerTag("not-player-placed", NotPlayerPlacedConditionTag.class, NotPlayerPlacedConditionTag::new, null);
    }

    public static Map<String, String> getTagDescriptionMap() {
        return Collections.unmodifiableMap(tagDescriptionMap);
    }

    private static <T extends ConditionTag> void registerTag(String prefix, Class<T> tagClass, Function<String, T> tagConstructor, String description) {
        TAG_PREFIX_MAP.put(prefix, tagConstructor);
        CLASS_TO_PREFIX_MAP.put(tagClass, prefix);
        if (description != null)
            tagDescriptionMap.put(prefix, description);
    }

    public static ConditionTag parse(String tag) {
        int index = tag.indexOf(":");
        if (index == -1) {
            return TAG_PREFIX_MAP.get(tag).apply(tag);
        } else {
            return TAG_PREFIX_MAP.get(tag.substring(0, index)).apply(tag);
        }
    }

    public static String getPrefix(Class<? extends ConditionTag> tagClass) {
        return CLASS_TO_PREFIX_MAP.get(tagClass);
    }

    /**
     * Gets the error message to be displayed in the spawner GUI
     *
     * @param localeManager The LocaleManager instance
     * @return A lore line to be displayed in the spawner GUI
     */
    public static String getErrorMessage(Class<? extends ConditionTag> tagClass, LocaleManager localeManager) {
        String prefix = ConditionTags.getPrefix(tagClass);
        return localeManager.getLocaleMessage("spawner-condition-invalid",
                StringPlaceholders.of("message", localeManager.getLocaleMessage("spawner-condition-" + prefix + "-invalid")));
    }

}
