package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.manager.AbstractConfigurationManager;
import dev.rosewood.rosestacker.RoseStacker;
import java.util.Arrays;
import java.util.Collections;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent;

public class ConfigurationManager extends AbstractConfigurationManager {

    public enum Setting implements RoseSetting {
        DISABLED_WORLDS("disabled-worlds", Collections.singletonList("disabled_world_name"), "A list of worlds that the plugin is disabled in"),
        STACK_FREQUENCY("stack-frequency", 100, "How often should we try to stack nearby entities?", "Higher values mean longer times between checks, but also less lag", "If you are having issues with TPS, increase this value", "Values are in ticks, do not set lower than 1"),
        ITEM_STACK_FREQUENCY("item-stack-frequency", 20, "How often should we try to stack nearby items?", "Values are in ticks, do not set lower than 1"),
        NAMETAG_UPDATE_FREQUENCY("nametag-update-frequency", 30, "How often should we update stacked entity nametags?"),
        AUTOSAVE_FREQUENCY("autosave-frequency", 15, "How often should we autosave all loaded stack data?", "Value is measured in minutes, set to -1 to disable"),
        LEGACY_DATA_MIGRATION("legacy-data-migration", true, "Should data from versions of RoseStacker older than v1.3.0 be updated to the new format?", "Disabling this will also disable any active stack plugin converters that are active", "Disabling this can provide a large performance boost, but it may be needed if your server is still updating data from an old RoseStacker version"),

        GLOBAL_ENTITY_SETTINGS("global-entity-settings", null, "Global entity settings", "Changed values in entity_settings.yml will override these values"),
        ENTITY_STACKING_ENABLED("global-entity-settings.stacking-enabled", true, "Should entity stacking be enabled at all?"),
        ENTITY_INSTANT_STACK("global-entity-settings.instant-stack", true, "Should entities try to be stacked instantly upon spawning?", "Setting this to false may yield better performance at the cost of entities being visible before stacking"),
        ENTITY_MIN_STACK_SIZE("global-entity-settings.min-stack-size", 2, "The minimum number of nearby entities required to form a stack", "Do not set this lower than 2"),
        ENTITY_MAX_STACK_SIZE("global-entity-settings.max-stack-size", 128, "The maximum number of entities that can be in a single stack"),
        ENTITY_MERGE_RADIUS("global-entity-settings.merge-radius", 5, "How close do entities need to be to merge with each other?"),
        ENTITY_MERGE_ENTIRE_CHUNK("global-entity-settings.merge-entire-chunk", false, "Should we merge all similar entities into a single stack per chunk?", "This setting overrides the above"),
        ENTITY_MIN_STACK_COUNT_ONLY_INDIVIDUALS("global-entity-settings.min-stack-count-only-individuals", false, "Should only individual entities be counted for the min-stack-size requirement?", "When false, an existing stack larger than min-stack-size can have other mobs stack into it"),
        ENTITY_MIN_SPLIT_IF_LOWER("global-entity-settings.min-split-if-lower", false, "Should entity stacks split into individual mobs if the stack size goes below the min-stack-size setting?"),
        ENTITY_DISPLAY_TAGS("global-entity-settings.display-tags", true, "Should tags be displayed above stacks to show their amount and type?"),
        ENTITY_DISPLAY_TAGS_SINGLE("global-entity-settings.display-tags-single", false, "Should tags be displayed if the stack only has one entity?"),
        ENTITY_DISPLAY_TAGS_HOVER("global-entity-settings.display-tags-hover", false, "Do stacks need to be hovered over for their tags to be visible?"),
        ENTITY_DISPLAY_TAGS_CUSTOM_NAME("global-entity-settings.display-tags-custom-name", true, "Should the entity custom name be visible with the stack size?"),
        ENTITY_KILL_ENTIRE_STACK_ON_DEATH("global-entity-settings.kill-entire-stack-on-death", false, "Should the entire stack of entities always be killed when the main entity dies?"),
        ENTITY_KILL_ENTIRE_STACK_CONDITIONS("global-entity-settings.kill-entire-stack-on-death-conditions", Collections.singletonList("FALL"), "Under what conditions should the entire stack be killed when the main entity dies?", "If kill-entire-stack-on-death is true, this setting will not be used", "Valid conditions can be found here:", "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html"),
        ENTITY_KILL_TRANSFER_VELOCITY("global-entity-settings.kill-transfer-velocity", true, "Should knockback be transferred to the next entity in the stack?"),
        ENTITY_DISPLAY_CORPSE("global-entity-settings.display-corpse", true, "Should a corpse appear when a mob in the stack dies?", "This is the red death animation that appears when a mob dies"),
        ENTITY_CUMULATIVE_BREEDING("global-entity-settings.cumulative-breeding", true, "Should all animals in a stack be bred together with as much food as they can?", "Please note that this setting is not perfect, it is here to make breeding more simple for players", "For best baby animal support, set dont-stack-if-baby to true for each breedable entity type in entity_settings.yml"),
        ENTITY_SHARE_DAMAGE_CONDITIONS("global-entity-settings.share-damage-conditions", Collections.singletonList(EntityDamageEvent.DamageCause.MAGIC.name()), "Under what conditions will the damage be propagated through the whole stack?", "Valid conditions can be found here:", "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html"),
        ENTITY_DROP_ACCURATE_ITEMS("global-entity-settings.drop-accurate-items", true, "Should items be dropped for all entities when an entire stack is killed at once?"),
        ENTITY_DROP_ACCURATE_EXP("global-entity-settings.drop-accurate-exp", true, "Should exp be dropped for all entities when an entire stack is killed at once?"),
        ENTITY_STACK_TO_BOTTOM("global-entity-settings.stack-to-bottom", false, "Should newly stacked entities be put on the bottom of the stack?"),
        ENTITY_REQUIRE_LINE_OF_SIGHT("global-entity-settings.require-line-of-sight", true, "Do entities need to be able to see each other to be able to stack?", "Setting this to true will prevent entities from stacking through walls"),
        ENTITY_TRANSFORM_ENTIRE_STACK("global-entity-settings.transform-entire-stack", true, "Should the entire stack of entities be transformed when the main entity is transformed?", "This applies to pigs getting struck by lightning, zombies drowning, etc"),
        ENTITY_ONLY_STACK_ON_GROUND("global-entity-settings.only-stack-on-ground", false, "Do entities have to be on the ground in order to stack?", "This does not apply if the mobs can fly or live in the water"),
        ENTITY_DONT_STACK_IF_IN_WATER("global-entity-settings.dont-stack-if-in-water", false, "Should we stack entities if they are in the water?", "This does not apply if the mobs can fly or live in the water"),
        ENTITY_DONT_STACK_IF_LEASHED("global-entity-settings.dont-stack-if-leashed", true, "Should we stack entities if they are leashed?", "You will still be able to leash stacks, it will just prevent them from stacking into other stacks", "This can cause some weird effects if disabled"),
        ENTITY_DONT_STACK_IF_INVULNERABLE("global-entity-settings.dont-stack-if-invulnerable", true, "Should we stack entities if they are invulnerable?"),
        ENTITY_DONT_STACK_CUSTOM_NAMED("global-entity-settings.dont-stack-custom-named", false, "Should we stack entities with custom names?"),
        ENTITY_DONT_STACK_IF_ACTIVE_RAIDER("global-entity-settings.dont-stack-if-active-raider", true, "Should we stack entities that are part of an active raid?", "This may cause issues when loading entities if disabled"),
        ENTITY_STACK_FLYING_DOWNWARDS("global-entity-settings.stack-flying-downwards", false, "Should flying mobs always be stacked downwards?", "This is useful for mob grinders"),
        ENTITY_ONLY_STACK_FROM_SPAWNERS("global-entity-settings.only-stack-from-spawners", false, "Should we only stack entities spawned from spawners?"),
        ENTITY_TRIGGER_DEATH_EVENT_FOR_ENTIRE_STACK_KILL("global-entity-settings.trigger-death-event-for-entire-stack-kill", false, "Should an entity death event be triggered for each mob in a stack?", "If you use custom drops plugins, make sure to enable this", "Note to developers: The death events are asynchronous based on the below setting"),
        ENTITY_DEATH_EVENT_RUN_ASYNC("global-entity-settings.death-event-trigger-async", true, "Should the entity loot be calculated asynchronously?", "If you try enabling this and you get errors that say something like '<SomeEvent> may only be triggered synchronously'", "and has RoseStacker in the stacktrace, you should keep this as false.", "Set this as true for optimal performance if you are not having issues."),
        ENTITY_MULTIPLY_CUSTOM_LOOT("global-entity-settings.multiply-custom-loot", false, "This will cause only a single death event to run which will save resources but", "also make it so rare drops can drop significantly more often since the", "drops will be multiplied by the stack size.", "Note: This only applies to custom loot"),
        ENTITY_INSTANT_KILL_DISABLED_AI("global-entity-settings.instant-kill-disabled-ai", false, "Should entities with disabled AI be killed instantly when receiving damage from a player?"),

        GLOBAL_ITEM_SETTINGS("global-item-settings", null, "Global item settings", "Changed values in item_settings.yml will override these values"),
        ITEM_STACKING_ENABLED("global-item-settings.stacking-enabled", true, "Should item stacking be enabled at all?"),
        ITEM_MAX_STACK_SIZE("global-item-settings.max-stack-size", 1024, "The maximum number of items that can be in a single stack"),
        ITEM_MERGE_RADIUS("global-item-settings.merge-radius", 2.5, "How close do items need to be to merge with each other?"),
        ITEM_DISPLAY_TAGS("global-item-settings.display-tags", true, "Should tags be displayed above stacks to show their amount and type?"),
        ITEM_DISPLAY_TAGS_SINGLE("global-item-settings.display-tags-single", false, "Should tags be displayed if the stack only has one item?"),
        ITEM_DISPLAY_CUSTOM_NAMES("global-item-settings.display-custom-names", true, "Should items with custom names be shown on their tags?"),
        ITEM_DISPLAY_CUSTOM_NAMES_COLOR("global-item-settings.display-custom-names-color", true, "Should the color of custom names be shown on their tags?"),
        ITEM_DISPLAY_CUSTOM_NAMES_ALWAYS("global-item-settings.display-custom-names-always", true, "Should items with a custom name always display their tags?", "This mirrors vanilla behavior"),

        GLOBAL_BLOCK_SETTINGS("global-block-settings", null, "Global block settings", "Changed values in block_settings.yml will override these values"),
        BLOCK_STACKING_ENABLED("global-block-settings.stacking-enabled", true, "Should block stacking be enabled at all?"),
        BLOCK_MAX_STACK_SIZE("global-block-settings.max-stack-size", 2048, "The maximum number of blocks that can be in a single stack"),
        BLOCK_DISPLAY_TAGS("global-block-settings.display-tags", true, "Should tags be displayed above stacks to show their amount and type?"),
        BLOCK_EXPLOSION_PROTECTION("global-block-settings.explosion-protection", true, "Should stacked blocks be protected from explosions?"),
        BLOCK_EXPLOSION_DECREASE_STACK_SIZE_ONLY("global-block-settings.explosion-decrease-stack-size-only", true, "If enabled, only the stack size will decrease from explosions, no items will be dropped"),
        BLOCK_EXPLOSION_DESTROY_CHANCE("global-block-settings.explosion-destroy-chance", 10.0, "The percentage chance of blocks getting destroyed from an explosion (0-100)"),
        BLOCK_EXPLOSION_DESTROY_AMOUNT_PERCENTAGE("global-block-settings.explosion-amount-percentage", 50.0, "The percentage of blocks in the stack that will be destroyed from an explosion", "If a stack of 10 blocks is exploded with a chance of 50.0, 5 blocks will be saved"),
        BLOCK_EXPLOSION_DESTROY_AMOUNT_FIXED("global-block-settings.explosion-amount-fixed", -1, "The fixed amount of blocks in the stack to destroy", "If this is set to 1 or greater, overrides explosion-amount-percentage", "If a stack of 10 blocks is exploded with a fixed amount of 3, 7 blocks will be saved"),
        BLOCK_DROP_TO_INVENTORY("global-block-settings.drop-to-inventory", false, "Should blocks be dropped directly into the player's inventory when broken?"),
        BLOCK_BREAK_ENTIRE_STACK_WHILE_SNEAKING("global-block-settings.break-entire-stack-while-sneaking", true, "Should the entire stack be broken if the player is sneaking?"),
        BLOCK_BREAK_ENTIRE_STACK_INTO_SEPARATE("global-block-settings.break-entire-stack-into-separate", true, "Should the entire stack be broken into individual blocks?"),
        BLOCK_GUI_ENABLED("global-block-settings.gui-enabled", true, "Should a GUI to edit the stack open when the player shift-right-clicks the stack?"),
        BLOCK_GUI_BORDER_MATERIAL("global-block-settings.gui-border-material", Material.BLUE_STAINED_GLASS_PANE.name(), "What material should be used for the border of the GUI?", "If you want no border, set it to AIR"),

        GLOBAL_SPAWNER_SETTINGS("global-spawner-settings", null, "Global spawner settings", "Changed values in spawner_settings.yml will override these values"),
        SPAWNER_STACKING_ENABLED("global-spawner-settings.stacking-enabled", true, "Should RoseStacker handle spawners?", "Disabling this will prevent spawners from stacking and being handled entirely.", "If you don't want spawners to stack but still want them handled", "by RoseStacker, set the max-stack-size to 1 instead."),
        SPAWNER_MAX_STACK_SIZE("global-spawner-settings.max-stack-size", 32, "The maximum number of spawners that can be in a single stack"),
        SPAWNER_DISPLAY_TAGS("global-spawner-settings.display-tags", true, "Should tags be displayed above stacks to show their amount and type?"),
        SPAWNER_DISPLAY_TAGS_SINGLE("global-spawner-settings.display-tags-single", false, "Should tags be displayed if the stack only has one spawner?"),
        SPAWNER_DISPLAY_TAGS_SINGLE_AMOUNT("global-spawner-settings.display-tags-single-amount", false, "Should stacks of size one show the amount on their tags if enabled?"),
        SPAWNER_DISABLE_MOB_AI("global-spawner-settings.disable-mob-ai", false, "Should mob AI be disabled for mobs spawned by spawners?"),
        SPAWNER_DISABLE_MOB_AI_OPTIONS("global-spawner-settings.disable-mob-ai-options", null, "Options to apply to mobs with disabled AI"),
        SPAWNER_DISABLE_MOB_AI_OPTIONS_REMOVE_GOALS("global-spawner-settings.disable-mob-ai-options.remove-goals", true, "Should mob goals be removed? This includes movement and targeting"),
        SPAWNER_DISABLE_MOB_AI_OPTIONS_UNDEAD_BURN_IN_DAYLIGHT("global-spawner-settings.disable-mob-ai-options.undead-burn-in-daylight", false, "Should undead mobs be able to burn in the daylight?"),
        SPAWNER_DISABLE_MOB_AI_OPTIONS_SILENCE("global-spawner-settings.disable-mob-ai-options.silence", true, "Should mobs be silenced so they don't make any sounds?"),
        SPAWNER_DISABLE_MOB_AI_OPTIONS_NO_KNOCKBACK("global-spawner-settings.disable-mob-ai-options.no-knockback", true, "Should knockback be disabled?"),
        SPAWNER_DISABLE_MOB_AI_OPTIONS_DISABLE_BREEDING("global-spawner-settings.disable-mob-ai-options.disable-breeding", true, "Should mobs with disabled AI be able to breed?"),
        SPAWNER_DISABLE_MOB_AI_OPTIONS_KILL_ENTIRE_STACK_ON_DEATH("global-spawner-settings.disable-mob-ai-options.kill-entire-stack-on-death", false, "Should the entire stack of mobs be killed on death?", "This will only apply to mobs with disabled AI and overwrites any other settings if this is set to true"),
        SPAWNER_DISABLE_MOB_AI_ONLY_PLAYER_PLACED("global-spawner-settings.disable-mob-ai-only-player-placed", false, "Should only spawners placed by players spawn mobs with disabled AI?", "disable-mob-ai must be enabled for this to work"),
        SPAWNER_DISABLE_ATTACKING("global-spawner-settings.disable-attacking", false, "Should mobs spawned from spawners be prevented from attacking anything?"),
        SPAWNER_MAX_FAILED_SPAWN_ATTEMPTS("global-spawner-settings.max-failed-spawn-attempts", 50, "How many random blocks should we check to spawn a mob before giving up?"),
        SPAWNER_DEACTIVATE_WHEN_POWERED("global-spawner-settings.deactivate-when-powered", false, "Should spawners turn off when powered by redstone?"),
        SPAWNER_EXPLOSION_PROTECTION("global-spawner-settings.explosion-protection", true, "Should spawners be protected from explosions?"),
        SPAWNER_EXPLOSION_DECREASE_STACK_SIZE_ONLY("global-spawner-settings.explosion-decrease-stack-size-only", true, "If enabled, only the stack size will decrease from explosions, no items will be dropped"),
        SPAWNER_EXPLOSION_DESTROY_CHANCE("global-spawner-settings.explosion-destroy-chance", 10.0, "The percentage chance of spawners getting destroyed from an explosion (0-100)"),
        SPAWNER_EXPLOSION_DESTROY_AMOUNT_PERCENTAGE("global-spawner-settings.explosion-amount-percentage", 50.0, "The percentage of spawners in the stack that will be destroyed from an explosion", "If a stack of 10 spawners is exploded with a chance of 50.0, 5 blocks will be saved"),
        SPAWNER_EXPLOSION_DESTROY_AMOUNT_FIXED("global-spawner-settings.explosion-amount-fixed", -1, "The fixed amount of spawners in the stack to destroy", "If this is set to 0 or greater, overrides explosion-amount-percentage", "If a stack of 10 spawners is exploded with a fixed amount of 3, 7 blocks will be saved"),
        SPAWNER_DROP_TO_INVENTORY("global-spawner-settings.drop-to-inventory", false, "Should spawners be dropped directly into the player's inventory when broken?"),
        SPAWNER_DROP_IN_CREATIVE("global-spawner-settings.drop-in-creative", false, "Should spawners always drop when broken in creative mode?"),
        SPAWNER_BREAK_ENTIRE_STACK_WHILE_SNEAKING("global-spawner-settings.break-entire-stack-while-sneaking", true, "Should the entire stack be broken if the player is sneaking?"),
        SPAWNER_BREAK_ENTIRE_STACK_INTO_SEPARATE("global-spawner-settings.break-entire-stack-into-separate", false, "Should the entire stack be broken into individual spawners?"),
        SPAWNER_ADVANCED_PERMISSIONS("global-spawner-settings.silk-touch-advanced-permissions", false, "Should advanced silk touch permissions be used?", "Requires silk-touch-require-permission to be enabled for the silk touch permissions", "This will enable the following permissions:", "- rosestacker.silktouch.<entityType>", "- rosestacker.nosilk.<entityType>", "- rosestacker.spawnerplace.<entityType>"),
        SPAWNER_SILK_TOUCH_REQUIRED("global-spawner-settings.silk-touch-required", false, "Should silk touch be required to pick up spawners?"),
        SPAWNER_SILK_TOUCH_CHANCE("global-spawner-settings.silk-touch-chance", 100, "The chance that spawners will be picked up with a silk touch tool", "You may use rosestacker.silktouch.chance.<#> to use a custom percent chance through a permission", "The highest number between the config and permissions will be used", "Use a whole number value between 0 and 100"),
        SPAWNER_SILK_TOUCH_ONLY_NATURAL("global-spawner-settings.silk-touch-only-natural", false, "Should only natural spawners have a chance of being picked up with silk touch?", "If enabled, player-placed spawners will always have a 100% chance of being picked up with silk touch"),
        SPAWNER_SILK_TOUCH_GUARANTEE("global-spawner-settings.silk-touch-guarantee", true, "Should silk touch of level II or higher be guaranteed to pick up the spawner?"),
        SPAWNER_SILK_TOUCH_REQUIRE_PERMISSION("global-spawner-settings.silk-touch-require-permission", false, "Should the permission rosestacker.silktouch be required", "to be able to pick up spawners with silk touch?"),
        SPAWNER_SILK_TOUCH_PROTECT("global-spawner-settings.silk-touch-protect", false, "Should spawners be protected from being destroyed without silk touch?", "A message will be sent to the player explaining why it cannot be broken"),
        SPAWNER_AUTO_STACK_RANGE("global-spawner-settings.auto-stack-range", -1, "How close should spawners have to be placed to auto stack together?", "A value of -1 will disable this setting"),
        SPAWNER_AUTO_STACK_PARTICLES("global-spawner-settings.auto-stack-particles", true, "Should particles be displayed when auto stacking spawners together?", "Useful for letting the player know where their spawner just went"),
        SPAWNER_CONVERT_REQUIRE_SAME_AMOUNT("global-spawner-settings.convert-require-same-amount", false, "Should the same number of spawn eggs as the spawner stack be required for conversion?"),
        SPAWNER_SPAWN_COUNT_STACK_SIZE_MULTIPLIER("global-spawner-settings.spawn-count-stack-size-multiplier", 4, "How many mobs should spawn per stacked spawner?", "Will use spawner tile value if set to -1"),
        SPAWNER_SPAWN_COUNT_STACK_SIZE_RANDOMIZED("global-spawner-settings.spawn-count-stack-size-randomized", true, "Should the amount of mobs spawned be randomized between the stack size and the max spawn amount?"),
        SPAWNER_SPAWN_DELAY_MINIMUM("global-spawner-settings.spawn-delay-minimum", 200, "The minimum number of ticks between spawn attempts", "Will use spawner tile value if set to -1"),
        SPAWNER_SPAWN_DELAY_MAXIMUM("global-spawner-settings.spawn-delay-maximum", 800, "The maximum number of ticks between spawn attempts", "Will use spawner tile value if set to -1"),
        SPAWNER_SPAWN_MAX_NEARBY_ENTITIES("global-spawner-settings.spawn-max-nearby-entities", 6, "If more than this number of entities are near the spawner, it will not spawn anything", "This only counts the individual mobs, and not the stack size", "Can be overridden for each spawner type using the max-nearby-entities:# spawn requirement"),
        SPAWNER_SPAWN_ENTITY_SEARCH_RANGE("global-spawner-settings.spawn-entity-search-range", -1, "How far away from the spawner should we search for nearby entities when using max-nearby-entities?", "A value of -1 will make this setting use the same value as the spawn range"),
        SPAWNER_SPAWN_PLAYER_ACTIVATION_RANGE("global-spawner-settings.spawn-player-activation-range", 16, "How close do players need to be to activate the spawner?", "Will use spawner tile value if set to -1"),
        SPAWNER_SPAWN_RANGE("global-spawner-settings.spawn-range", 4, "How far away can entities be spawned from the spawner?", "Will use spawner tile value if set to -1"),
        SPAWNER_SPAWN_INTO_NEARBY_STACKS("global-spawner-settings.spawn-into-nearby-stacks", true, "Should mobs spawned from spawners spawn directly into nearby stacks?"),
        SPAWNER_USE_VERTICAL_SPAWN_RANGE("global-spawner-settings.use-vertical-spawn-range", false, "Should the vertical spawn range use the horizontal spawn range?", "Entities normally only spawn one block above and below the spawner"),
        SPAWNER_DONT_SPAWN_INTO_BLOCKS("global-spawner-settings.dont-spawn-into-blocks", true, "When enabled, this will force the 'air' condition tag onto the", "spawner if it doesn't have 'fluid' or 'air' already.", "Disabling this will allow mobs to spawn into blocks unless", "you explicitly add the 'fluid' or 'air' tags."),
        SPAWNER_NERF_PATROL_LEADERS("global-spawner-settings.nerf-patrol-leaders", false, "Should patrol leaders be prevented when spawning potential raid member mobs?"),
        SPAWNER_MAX_NEARBY_ENTITIES_INCLUDE_STACKS("global-spawner-settings.max-nearby-entities-include-stacks", false, "When enabled, the entire stack size of entities will be taken into account for nearby entiites", "When disabled, only one entity per stack will count towards nearby entities"),
        SPAWNER_GUI_ENABLED("global-spawner-settings.gui-enabled", true, "Should a GUI to view the spawner information open when the player shift-right-clicks the stack?"),
        SPAWNER_GUI_TICK_UPDATE_RATE("global-spawner-settings.gui-tick-update-rate", 2, "How often should the time before next spawn message be updated?", "Value is measured in ticks, do not go below 1"),
        SPAWNER_GUI_BORDER_MATERIAL("global-spawner-settings.gui-border-material", Material.GRAY_STAINED_GLASS_PANE.name(), "What material should be used for the border of the GUI?"),
        SPAWNER_GUI_BORDER_CORNER_MATERIAL("global-spawner-settings.gui-border-corner-material", Material.LIGHT_BLUE_STAINED_GLASS_PANE.name(), "What material should be used for the top right and bottom left corners of the GUI?"),
        SPAWNER_GUI_BORDER_ACCENT_MATERIAL("global-spawner-settings.gui-border-accent-material", Material.BLUE_STAINED_GLASS_PANE.name(), "What material should be used for the corner accents of the GUI?"),
        SPAWNER_GUI_SPAWNER_STATS_MATERIAL("global-spawner-settings.gui-spawner-stats-material", Material.BOOK.name(), "What material should the spawner stats icon be?"),
        SPAWNER_GUI_CENTRAL_MATERIAL("global-spawner-settings.gui-central-material", Material.SPAWNER.name(), "What material should the central icon be?"),
        SPAWNER_GUI_VALID_SPAWN_CONDITIONS_MATERIAL("global-spawner-settings.gui-valid-spawn-conditions-material", Material.EMERALD_BLOCK.name(), "What material should the valid spawn conditions icon be?"),
        SPAWNER_GUI_INVALID_SPAWN_CONDITIONS_MATERIAL("global-spawner-settings.gui-invalid-spawn-conditions-material", Material.REDSTONE_BLOCK.name(), "What material should the invalid spawn conditions icon be?"),

        DYNAMIC_TAG_SETTINGS("dynamic-tag-settings", null, "Settings that apply to the tags above stacks", "These settings require their respective display-tags settings to be set to true to function", "These settings run at the same frequency as the stack-frequency setting", "If you are seeing impacts to server performance, consider disabling these settings"),
        ENTITY_DYNAMIC_TAG_VIEW_RANGE_ENABLED("dynamic-tag-settings.entity-dynamic-tag-view-range-enabled", true, "Should entity tags be hidden when the player is a certain distance away?", "Note: This overrides global-entity-settings.display-tags-hover if enabled"),
        ITEM_DYNAMIC_TAG_VIEW_RANGE_ENABLED("dynamic-tag-settings.item-dynamic-tag-view-range-enabled", true, "Should item tags be hidden when the player is a certain distance away?"),
        BLOCK_DYNAMIC_TAG_VIEW_RANGE_ENABLED("dynamic-tag-settings.block-dynamic-tag-view-range-enabled", true, "Should block/spawner tags be hidden when the player is a certain distance away?"),
        ENTITY_DYNAMIC_TAG_VIEW_RANGE("dynamic-tag-settings.entity-dynamic-tag-view-range", 32, "How far away should a player be able to see entity tags?"),
        ITEM_DYNAMIC_TAG_VIEW_RANGE("dynamic-tag-settings.item-dynamic-tag-view-range", 32, "How far away should a player be able to see item tags?"),
        BLOCK_DYNAMIC_TAG_VIEW_RANGE("dynamic-tag-settings.block-dynamic-tag-view-range", 32, "How far away should a player be able to see block/spawner tags?"),
        ENTITY_DYNAMIC_TAG_VIEW_RANGE_WALL_DETECTION_ENABLED("dynamic-tag-settings.entity-dynamic-tag-view-range-wall-detection-enabled", true, "Should entity tags be hidden if they are out of view?"),
        ITEM_DYNAMIC_TAG_VIEW_RANGE_WALL_DETECTION_ENABLED("dynamic-tag-settings.item-dynamic-tag-view-range-wall-detection-enabled", true, "Should item tags be hidden if they are out of view?"),
        BLOCK_DYNAMIC_TAG_VIEW_RANGE_WALL_DETECTION_ENABLED("dynamic-tag-settings.block-dynamic-tag-view-range-wall-detection-enabled", true, "Should block/spawner tags be hidden if they are out of view?"),

        STACK_TOOL_SETTINGS("stack-tool-settings", null, "Settings that apply to the item given from '/rs stacktool'"),
        STACK_TOOL_MATERIAL("stack-tool-settings.material", Material.STICK.name(), "The material of the stacking tool"),
        STACK_TOOL_NAME("stack-tool-settings.name", "<g:#ed3737:#ffaf3e>Stacking Tool", "The name to display on the stacking tool"),
        STACK_TOOL_LORE("stack-tool-settings.lore", Arrays.asList(
                "&bLeft Click:",
                "&7- &eSelect two mobs to test if they can stack together.",
                "&bShift Left Click:",
                "&7- &eView stack details, works on all stack types.",
                "&bRight Click:",
                "&7- &eToggle if a mob is stackable or not.",
                "&bShift Right Click:",
                "&7- &eMark an entire entity stack as unstackable.",
                "&7- &eThis will cause everything to unstack.",
                "&7- &eIf used on a spawner, will cause it to spawn instantly.",
                "&bWhile Held:",
                "&7- &eA particle will appear above nearby mobs.",
                "&7- &aGreen &emeans the mob can stack.",
                "&7- &cRed &emeans the mob can not stack."
        ), "The lore to display on the stacking tool"),

        MISC_SETTINGS("misc-settings", null, "Miscellaneous other settings for the plugin"),
        MISC_WORLDGUARD_REGION("misc-settings.worldguard-region", false, "Should a custom WorldGuard region named 'rosestacker' be registered and used to test entity stacking?", "Entity stacking within regions will only work if the result is ALLOW", "This setting will only be updated after a restart"),
        MISC_COREPROTECT_LOGGING("misc-settings.coreprotect-logging-enabled", true, "If CoreProtect is installed, should we log stacked block/spawner break/placing?"),
        MISC_LOGBLOCK_LOGGING("misc-settings.logblock-logging-enabled", true, "If LogBlock is installed, should we log stacked block/spawner break/placing?"),
        MISC_CLEARLAG_CLEAR_ENTITIES("misc-settings.clearlag-clear-entities", false, "If Clearlag is installed, should we clear stacked entities?"),
        MISC_CLEARLAG_CLEAR_ITEMS("misc-settings.clearlag-clear-items", false, "If Clearlag is installed, should we clear stacked items?"),
        MISC_CLEARALL_REMOVE_SINGLE("misc-settings.clearall-remove-single", false, "Should single mobs be removed with `/rs clearall`?", "This will also affect the clearlag-clear-entities setting above"),
        MISC_MYTHICMOBS_ALLOW_STACKING("misc-settings.mythicmobs-allow-stacking", false, "Should mobs owned by MythicMobs be allowed to stack?", "This is recommended to keep set to false unless you specifically only change mob attributes");

        private final String key;
        private final Object defaultValue;
        private final String[] comments;
        private Object value = null;

        Setting(String key, Object defaultValue, String... comments) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.comments = comments != null ? comments : new String[0];
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public Object getDefaultValue() {
            return this.defaultValue;
        }

        @Override
        public String[] getComments() {
            return this.comments;
        }

        @Override
        public Object getCachedValue() {
            return this.value;
        }

        @Override
        public void setCachedValue(Object value) {
            this.value = value;
        }

        @Override
        public CommentedFileConfiguration getBaseConfig() {
            return RoseStacker.getInstance().getManager(ConfigurationManager.class).getConfig();
        }
    }

    public ConfigurationManager(RosePlugin rosePlugin) {
        super(rosePlugin, Setting.class);
    }

    @Override
    protected String[] getHeader() {
        return new String[]{
                "     __________                      _________ __                 __                 ",
                "     \\______   \\ ____  ______ ____  /   _____//  |______    ____ |  | __ ___________ ",
                "      |       _//  _ \\/  ___// __ \\ \\_____  \\\\   __\\__  \\ _/ ___\\|  |/ // __ \\_  __ \\",
                "      |    |   (  <_> )___ \\\\  ___/ /        \\|  |  / __ \\\\  \\___|    <\\  ___/|  | \\/",
                "      |____|_  /\\____/____  >\\___  >_______  /|__| (____  /\\___  >__|_ \\\\___  >__|   ",
                "             \\/           \\/     \\/        \\/           \\/     \\/     \\/    \\/       "
        };
    }

}
