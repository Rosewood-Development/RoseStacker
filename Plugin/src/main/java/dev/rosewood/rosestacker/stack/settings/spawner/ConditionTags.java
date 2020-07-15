package dev.rosewood.rosestacker.stack.settings.spawner;

import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.AboveSeaLevelConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.AboveYAxisConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.AirConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.BelowSeaLevelConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.BelowYAxisConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.BiomeConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.BlockConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.BlockExceptionConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.DarknessConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.FluidConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.LightnessConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.MaxNearbyEntityConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.NoSkylightAccessConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.NoneConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.OnGroundConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.SkylightAccessConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.TallAirConditionTag;
import dev.rosewood.rosestacker.utils.StringPlaceholders;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConditionTags {

    private static final Map<String, Constructor<? extends ConditionTag>> tagPrefixMap = new HashMap<>();
    private static final Map<Class<? extends ConditionTag>, String> classToPrefixMap = new HashMap<>();

    public static final List<String> ANIMAL_TAGS = Collections.unmodifiableList(Arrays.asList("block:grass_block", "lightness"));
    public static final List<String> MONSTER_TAGS = Collections.unmodifiableList(Collections.singletonList("darkness"));

    static {
        registerTag("above-sea-level", AboveSeaLevelConditionTag.class);
        registerTag("above-y-axis", AboveYAxisConditionTag.class);
        registerTag("air", AirConditionTag.class);
        registerTag("below-sea-level", BelowSeaLevelConditionTag.class);
        registerTag("below-y-axis", BelowYAxisConditionTag.class);
        registerTag("biome", BiomeConditionTag.class);
        registerTag("block", BlockConditionTag.class);
        registerTag("block-exception", BlockExceptionConditionTag.class);
        registerTag("darkness", DarknessConditionTag.class);
        registerTag("fluid", FluidConditionTag.class);
        registerTag("lightness", LightnessConditionTag.class);
        registerTag("max-nearby-entities", MaxNearbyEntityConditionTag.class);
        registerTag("no-skylight-access", NoSkylightAccessConditionTag.class);
        registerTag("on-ground", OnGroundConditionTag.class);
        registerTag("skylight-access", SkylightAccessConditionTag.class);
        registerTag("tall-air", TallAirConditionTag.class);

        // Tag for when all conditions were met, but no entities were able to spawn
        registerTag("none", NoneConditionTag.class);
    }

    private static <T extends ConditionTag> void registerTag(String prefix, Class<T> tagClass) {
        try {
            tagPrefixMap.put(prefix, tagClass.getConstructor(String.class));
            classToPrefixMap.put(tagClass, prefix);
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

    public static ConditionTag parse(String tag) {
        try {
            int index = tag.indexOf(":");
            if (index == -1) {
                return tagPrefixMap.get(tag).newInstance(tag);
            } else {
                return tagPrefixMap.get(tag.substring(0, index)).newInstance(tag);
            }
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String getPrefix(Class<? extends ConditionTag> tagClass) {
        return classToPrefixMap.get(tagClass);
    }

    public static String getErrorMessage(Class<? extends ConditionTag> tagClass, LocaleManager localeManager) {
        String prefix = ConditionTags.getPrefix(tagClass);
        return localeManager.getLocaleMessage("spawner-condition-invalid",
                StringPlaceholders.single("message", localeManager.getLocaleMessage("spawner-condition-" + prefix + "-invalid")));
    }

}
