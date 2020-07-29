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
import dev.rosewood.rosestacker.utils.StringPlaceholders;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ConditionTags {

    private static final Map<String, Constructor<? extends ConditionTag>> tagPrefixMap = new HashMap<>();
    private static final Map<Class<? extends ConditionTag>, String> classToPrefixMap = new HashMap<>();
    private static final Map<String, String> tagDescriptionMap = new LinkedHashMap<>();

    public static final List<String> ANIMAL_TAGS = Collections.unmodifiableList(Arrays.asList("block:grass_block", "lightness"));
    public static final List<String> MONSTER_TAGS = Collections.unmodifiableList(Collections.singletonList("darkness"));

    static {
        registerTag("above-sea-level", AboveSeaLevelConditionTag.class, "Spawn area must be above sea level");
        registerTag("above-y-axis", AboveYAxisConditionTag.class, "Spawn area must be above the given y-axis. Example: `above-y-axis:30`");
        registerTag("air", AirConditionTag.class, "Spawn area must have air blocks available");
        registerTag("below-sea-level", BelowSeaLevelConditionTag.class, "Spawn area must be below the world sea level");
        registerTag("below-y-axis", BelowYAxisConditionTag.class, "Spawn area must be below the given y-axis. Example: `below-y-axis:30`");
        registerTag("biome", BiomeConditionTag.class, "Spawn area must be in one of the given biomes. Example: `biome:plains,desert`");
        registerTag("block", BlockConditionTag.class, "Spawn area must be on one of the given blocks. Example: `block:grass_block,sand`");
        registerTag("block-exception", BlockExceptionConditionTag.class, "Spawn area must not be on one of the given blocks. Example: `block-exeption:bedrock,barrier`");
        registerTag("darkness", DarknessConditionTag.class, "Spawn area must be below light level 8");
        registerTag("fluid", FluidConditionTag.class, "Spawn area must be inside a fluid. Valid values are `water` and `lava`. Example: `fluid:water`");
        registerTag("lightness", LightnessConditionTag.class, "Spawn area must be above light level 8");
        registerTag("max-nearby-entities", MaxNearbyEntityConditionTag.class, "Spawn area must have below a certain number of entities. Example: `max-nearby-entities:6`");
        registerTag("no-skylight-access", NoSkylightAccessConditionTag.class, "Spawn area must have no skylight access");
        registerTag("on-ground", OnGroundConditionTag.class, "Spawn area must have a ground block to spawn on");
        registerTag("skylight-access", SkylightAccessConditionTag.class, "Spawn area must have skylight access");

        // Tag for when all conditions were met, but no entities were able to spawn
        registerTag("none", NoneConditionTag.class, null);
    }

    public static Map<String, String> getTagDescriptionMap() {
        return Collections.unmodifiableMap(tagDescriptionMap);
    }

    private static <T extends ConditionTag> void registerTag(String prefix, Class<T> tagClass, String description) {
        try {
            tagPrefixMap.put(prefix, tagClass.getConstructor(String.class));
            classToPrefixMap.put(tagClass, prefix);
            if (description != null)
                tagDescriptionMap.put(prefix, description);
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

    /**
     * Gets the error message to be displayed in the spawner GUI
     *
     * @param localeManager The LocaleManager instance
     * @return A lore line to be displayed in the spawner GUI
     */
    public static String getErrorMessage(Class<? extends ConditionTag> tagClass, LocaleManager localeManager) {
        String prefix = ConditionTags.getPrefix(tagClass);
        return localeManager.getLocaleMessage("spawner-condition-invalid",
                StringPlaceholders.single("message", localeManager.getLocaleMessage("spawner-condition-" + prefix + "-invalid")));
    }

}
