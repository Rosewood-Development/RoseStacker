package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.utils.StackerUtils;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigurationManager extends Manager {

    private static final String[] HEADER = new String[] {
            "     __________                      _________ __                 __                 ",
            "     \\______   \\ ____  ______ ____  /   _____//  |______    ____ |  | __ ___________ ",
            "      |       _//  _ \\/  ___// __ \\ \\_____  \\\\   __\\__  \\ _/ ___\\|  |/ // __ \\_  __ \\",
            "      |    |   (  <_> )___ \\\\  ___/ /        \\|  |  / __ \\\\  \\___|    <\\  ___/|  | \\/",
            "      |____|_  /\\____/____  >\\___  >_______  /|__| (____  /\\___  >__|_ \\\\___  >__|   ",
            "             \\/           \\/     \\/        \\/           \\/     \\/     \\/    \\/       "
    };

    private static final String[] FOOTER = new String[] {
            "That's everything! You reached the end of the configuration.",
            "Enjoy the plugin!"
    };

    public enum Setting {
        LOCALE("locale", "en_US", "The locale to use in the /locale folder"),
        DISABLED_WORLDS("disabled-worlds", Collections.singletonList("disabled_world_name"), "A list of worlds that the plugin is disabled in"),
        STACK_FREQUENCY("stack-frequency", 5, "How often should we try to stack nearby entities?", "Higher values mean longer times between checks", "Values are in ticks, do not set lower than 1"),

        GLOBAL_ENTITY_SETTINGS("global-entity-settings", null, "Global entity settings", "Changed values in entity_settings.yml will override these values"),
        ENTITY_STACKING_ENABLED("global-entity-settings.stacking-enabled", true, "Should entity stacking be enabled at all?"),
        ENTITY_MIN_STACK_SIZE("global-entity-settings.min-stack-size", 2, "The minimum number of nearby entities required to form a stack", "Do not set this lower than 2"),
        ENTITY_MAX_STACK_SIZE("global-entity-settings.max-stack-size", 128, "The maximum number of entities that can be in a single stack"),
        ENTITY_MERGE_RADIUS("global-entity-settings.merge-radius", 5, "How close to entities need to be to merge with each other?"),
        ENTITY_MERGE_ENTIRE_CHUNK("global-entity-settings.merge-entire-chunk", false, "Should we merge all similar entities into a single stack per chunk?", "This setting overrides the above"),
        ENTITY_DISPLAY_TAGS("global-entity-settings.display-tags", true, "Should tags be displayed above stacks to show their amount and type?"),
        ENTITY_DISPLAY_TAGS_SINGLE("global-entity-settings.display-tags-single", false, "Should tags be displayed if the stack only has one entity?"),
        ENTITY_DISPLAY_TAGS_HOVER("global-entity-settings.display-tags-hover", false, "Do stacks need to be hovered over for their tags to be visible?"),
        ENTITY_DISPLAY_TAGS_CUSTOM_NAME("global-entity-settings.display-tags-custom-name", true, "Should the entity custom name be visible with the stack size?"),
        ENTITY_KILL_ENTIRE_STACK_ON_DEATH("global-entity-settings.kill-entire-stack-on-death", false, "Should the entire stack of entities always be killed when the main entity dies?"),
        ENTITY_KILL_ENTIRE_STACK_CONDITIONS("global-entity-settings.kill-entire-stack-on-death-conditions", Collections.singletonList("FALL"), "Under what conditions should the entire stack be killed when the main entity dies?", "If kill-entire-stack-on-death is true, this setting will not be used", "Valid conditions can be found here:", "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html"),
        ENTITY_DROP_ACCURATE_ITEMS("global-entity-settings.drop-accurate-items", true, "Should items be dropped for all entities when an entire stack is killed at once?"),
        ENTITY_DROP_ACCURATE_EXP("global-entity-settings.drop-accurate-exp", true, "Should exp be dropped for all entities when an entire stack is killed at once?"),
        ENTITY_STACK_TO_BOTTOM("global-entity-settings.stack-to-bottom", false, "Should newly stacked entities be put on the bottom of the stack?"),
        ENTITY_REQUIRE_LINE_OF_SIGHT("global-entity-settings.require-line-of-sight", true, "Do entities need to be able to see each other to be able to stack?", "Setting this to true will prevent entities from stacking through walls"),
        ENTITY_TRANSFORM_ENTIRE_STACK("global-entity-settings.transform-entire-stack", true, "Should the entire stack of entities be transformed when the main entity is transformed?", "This applies to pigs getting struck by lightning, zombies drowning, etc"),
        ENTITY_ONLY_STACK_ON_GROUND("global-entity-settings.only-stack-on-ground", false, "Do entities have to be on the ground in order to stack?", "This does not apply if the mobs can fly or live in the water"),
        ENTITY_DONT_STACK_IN_WATER("global-entity-settings.dont-stack-in-water", false, "Should we stack entities if they are in the water?", "This does not apply if the mobs can fly or live in the water"),
        ENTITY_DONT_STACK_CUSTOM_NAMED("global-entity-settings.dont-stack-custom-named", false, "Should we avoid stacking entities with custom names?"),
        ENTITY_STACK_FLYING_DOWNWARDS("global-entity-settings.stack-flying-downwards", false, "Should flying mobs always be stacked downwards?", "This is useful for mob grinders"),
        ENTITY_ONLY_STACK_FROM_SPAWNERS("global-entity-settings.only-stack-from-spawners", false, "Should we only stack entities spawned from spawners?"),
        ENTITY_TRIGGER_DEATH_EVENT_FOR_ENTIRE_STACK_KILL("global-entity-settings.trigger-death-event-for-entire-stack-kill", false, "Should an entity death event be triggered for each mob in a stack?", "If you use custom drops plugins, make sure to enable this", "Note to developers: The death events are asynchronous"),

        GLOBAL_ITEM_SETTINGS("global-item-settings", null, "Global item settings", "Changed values in item_settings.yml will override these values"),
        ITEM_STACKING_ENABLED("global-item-settings.stacking-enabled", true, "Should item stacking be enabled at all?"),
        ITEM_MAX_STACK_SIZE("global-item-settings.max-stack-size", 1024, "The maximum number of items that can be in a single stack"),
        ITEM_MERGE_RADIUS("global-item-settings.merge-radius", 2.5, "How close do items need to be to merge with each other?"),
        ITEM_DISPLAY_TAGS("global-item-settings.display-tags", true, "Should tags be displayed above stacks to show their amount and type?"),
        ITEM_DISPLAY_TAGS_SINGLE("global-item-settings.display-tags-single", false, "Should tags be displayed if the stack only has one item?"),
        ITEM_DISPLAY_CUSTOM_NAMES("global-item-settings.display-custom-names", true, "Should items with custom names be shown on their tags?"),
        ITEM_DISPLAY_CUSTOM_NAMES_COLOR("global-item-settings.display-custom-names-color", false, "Should the color of custom names be shown on their tags?"),

        GLOBAL_BLOCK_SETTINGS("global-block-settings", null, "Global block settings", "Changed values in block_settings.yml will override these values"),
        BLOCK_STACKING_ENABLED("global-block-settings.stacking-enabled", true, "Should block stacking be enabled at all?"),
        BLOCK_MAX_STACK_SIZE("global-block-settings.max-stack-size", 2048, "The maximum number of blocks that can be in a single stack"),
        BLOCK_DISPLAY_TAGS("global-block-settings.display-tags", true, "Should tags be displayed above stacks to show their amount and type?"),
        BLOCK_BREAK_ENTIRE_STACK_WHILE_SNEAKING("global-block-settings.break-entire-stack-while-sneaking", true, "Should the entire stack be broken if the player is sneaking?"),
        BLOCK_GUI_ENABLED("global-block-settings.gui-enabled", true, "Should a GUI to edit the stack open when the player shift-right-clicks the stack?"),
        BLOCK_GUI_BORDER_MATERIAL("global-block-settings.gui-border-material", "BLUE_STAINED_GLASS_PANE", "What material should be used for the border of the GUI?", "If you want no border, set it to AIR"),

        GLOBAL_SPAWNER_SETTINGS("global-spawner-settings", null, "Global spawner settings", "Changed values in spawner_settings.yml will override these values"),
        SPAWNER_STACKING_ENABLED("global-spawner-settings.stacking-enabled", true, "Should spawner stacking be enabled at all?"),
        SPAWNER_MAX_STACK_SIZE("global-spawner-settings.max-stack-size", 32, "The maximum number of spawners that can be in a single stack"),
        SPAWNER_DISPLAY_TAGS("global-spawner-settings.display-tags", true, "Should tags be displayed above stacks to show their amount and type?"),
        SPAWNER_DISPLAY_TAGS_SINGLE("global-spawner-settings.display-tags-single", false, "Should tags be displayed if the stack only has one spawner?"),
        SPAWNER_DROP_TO_INVENTORY("global-spawner-settings.drop-to-inventory", false, "Should spawners be dropped directly into the player's inventory when broken?"),
        SPAWNER_BREAK_ENTIRE_STACK_WHILE_SNEAKING("global-spawner-settings.break-entire-stack-while-sneaking", true, "Should the entire stack be broken if the player is sneaking?"),
        SPAWNER_SILK_TOUCH_REQUIRED("global-spawner-settings.silk-touch-required", false, "Should silk touch be required to pick up spawners?"),
        SPAWNER_SILK_TOUCH_CHANCE("global-spawner-settings.silk-touch-chance", 100, "The chance that spawners will be picked up with a silk touch tool"),
        SPAWNER_SILK_TOUCH_GUARANTEE("global-spawner-settings.silk-touch-guarantee", true, "Should silk touch of level II or higher be guaranteed to pick up the spawner?"),
        SPAWNER_SILK_TOUCH_REQUIRE_PERMISSION("global-spawner-settings.silk-touch-require-permission", false, "Should the permission rosestacker.silktouch be required", "to be able to pick up spawners with silk touch?"),
        SPAWNER_SPAWN_COUNT_STACK_SIZE_MULTIPLIER("global-spawner-settings.spawn-count-stack-size-multiplier", 4, "How many mobs should spawn per stacked spawner?"),
        SPAWNER_SPAWN_COUNT_STACK_SIZE_RANDOMIZED("global-spawner-settings.spawn-count-stack-size-randomized", true, "Should the amount of mobs spawned be randomized between the stack size and the max spawn amount?"),
        SPAWNER_SPAWN_DELAY_MINIMUM("global-spawner-settings.spawn-delay-minimum", 200, "The minimum number of ticks between spawn attempts"),
        SPAWNER_SPAWN_DELAY_MAXIMUM("global-spawner-settings.spawn-delay-maximum", 800, "The maximum number of ticks between spawn attempts"),
        SPAWNER_SPAWN_MAX_NEARBY_ENTITIES("global-spawner-settings.spawn-max-nearby-entities", 6, "If more than this number of entities are near the spawner, it will not spawn anything", "This only counts the individual mobs, and not the stack size"),
        SPAWNER_SPAWN_PLAYER_ACTIVATION_RANGE("global-spawner-settings.spawn-player-activation-range", 16, "How close do players need to be to activate the spawner?"),
        SPAWNER_SPAWN_RANGE("global-spawner-settings.spawn-range", 4, "How far away can entities be spawned from the spawner?"),

        DYNAMIC_TAG_SETTINGS("dynamic-tag-settings", null, "Settings that apply to the tags above stacks", "These settings require their respective display-tags settings to be set to true to function", "These settings run at the same frequency as the stack-frequency setting", "If you are seeing impacts to server performance, consider disabling these settings"),
        ENTITY_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE_ENABLED("dynamic-tag-settings.entity-display-tags-dynamic-view-range-enabled", true, "Should entity tags be hidden when the player is a certain distance away?", "Note: This overrides global-entity-settings.display-tags-hover if enabled"),
        ITEM_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE_ENABLED("dynamic-tag-settings.item-display-tags-dynamic-view-range-enabled", true, "Should item tags be hidden when the player is a certain distance away?"),
        BLOCK_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE_ENABLED("dynamic-tag-settings.block-display-tags-dynamic-view-range-enabled", true, "Should block tags be hidden when the player is a certain distance away?"),
        SPAWNER_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE_ENABLED("dynamic-tag-settings.spawner-display-tags-dynamic-view-range-enabled", true, "Should spawner tags be hidden when the player is a certain distance away?"),
        ENTITY_ITEM_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE("dynamic-tag-settings.entity-item-display-tags-dynamic-view-range", 32, "How far away should a player be able to see entity and item tags?"),
        BLOCK_SPAWNER_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE("dynamic-tag-settings.block-spawner-display-tags-dynamic-view-range", 32, "How far away should a player be able to see block and spawner tags?"),
        ENTITY_ITEM_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE_WALL_DETECTION_ENABLED("dynamic-tag-settings.entity-item-display-tags-dynamic-view-range-wall-detection-enabled", true, "Should entity and item tags be hidden if they are out of view?"),
        BLOCK_SPAWNER_DISPLAY_TAGS_DYNAMIC_VIEW_RANGE_WALL_DETECTION_ENABLED("dynamic-tag-settings.block-spawner-display-tags-dynamic-view-range-wall-detection-enabled", true, "Should block and spawner tags be hidden if they are out of view?"),

        MISC_SETTINGS("misc-settings", null, "Miscellaneous other settings for the plugin"),
        MISC_COREPROTECT_LOGGING("misc-settings.coreprotect-logging-enabled", true, "If CoreProtect is installed, should we log stacked block/spawner break/placing?"),
        MISC_CLEARLAG_CLEAR_ENTITIES("misc-settings.clearlag-clear-entities", true, "If Clearlag is installed, should we clear stacked entities?"),
        MISC_CLEARLAG_CLEAR_ITEMS("misc-settings.clearlag-clear-items", true, "If Clearlag is installed, should we clear stacked items?"),

        MYSQL_SETTINGS("mysql-settings", null, "Settings for if you want to use MySQL for data management"),
        MYSQL_ENABLED("mysql-settings.enabled", false, "Enable MySQL", "If false, SQLite will be used instead"),
        MYSQL_HOSTNAME("mysql-settings.hostname", "", "MySQL Database Hostname"),
        MYSQL_PORT("mysql-settings.port", 3306, "MySQL Database Port"),
        MYSQL_DATABASE_NAME("mysql-settings.database-name", "", "MySQL Database Name"),
        MYSQL_USER_NAME("mysql-settings.user-name", "", "MySQL Database User Name"),
        MYSQL_USER_PASSWORD("mysql-settings.user-password", "", "MySQL Database User Password"),
        MYSQL_USE_SSL("mysql-settings.use-ssl", false, "If the database connection should use SSL", "You should enable this if your database supports SSL");

        private final String key;
        private final Object defaultValue;
        private final String[] comments;
        private Object value = null;

        Setting(String key, Object defaultValue, String... comments) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.comments = comments != null ? comments : new String[0];
        }

        /**
         * @return the setting as a boolean
         */
        public boolean getBoolean() {
            this.loadValue();
            return (boolean) this.value;
        }

        /**
         * @return the setting as an int
         */
        public int getInt() {
            this.loadValue();
            return (int) this.getNumber();
        }

        /**
         * @return the setting as a long
         */
        public long getLong() {
            this.loadValue();
            return (long) this.getNumber();
        }

        /**
         * @return the setting as a double
         */
        public double getDouble() {
            this.loadValue();
            return this.getNumber();
        }

        /**
         * @return the setting as a float
         */
        public float getFloat() {
            this.loadValue();
            return (float) this.getNumber();
        }

        /**
         * @return the setting as a String
         */
        public String getString() {
            this.loadValue();
            return (String) this.value;
        }

        private double getNumber() {
            if (this.value instanceof Integer) {
                return (int) this.value;
            } else if (this.value instanceof Short) {
                return (short) this.value;
            } else if (this.value instanceof Byte) {
                return (byte) this.value;
            } else if (this.value instanceof Float) {
                return (float) this.value;
            }

            return (double) this.value;
        }

        /**
         * @return the setting as a string list
         */
        @SuppressWarnings("unchecked")
        public List<String> getStringList() {
            this.loadValue();
            return (List<String>) this.value;
        }

        public boolean setIfNotExists(CommentedFileConfiguration fileConfiguration) {
            this.loadValue();

            if (fileConfiguration.get(this.key) == null) {
                List<String> comments = Stream.of(this.comments).collect(Collectors.toList());
                if (!(this.defaultValue instanceof List) && this.defaultValue != null) {
                    String defaultComment = "Default: ";
                    if (this.defaultValue instanceof String) {
                        if (StackerUtils.containsConfigSpecialCharacters((String) this.defaultValue)) {
                            defaultComment += "'" + this.defaultValue + "'";
                        } else {
                            defaultComment += this.defaultValue;
                        }
                    } else {
                        defaultComment += this.defaultValue;
                    }
                    comments.add(defaultComment);
                }

                if (this.defaultValue != null) {
                    fileConfiguration.set(this.key, this.defaultValue, comments.toArray(new String[0]));
                } else {
                    fileConfiguration.addComments(comments.toArray(new String[0]));
                }

                return true;
            }

            return false;
        }

        /**
         * Resets the cached value
         */
        public void reset() {
            this.value = null;
        }

        /**
         * @return true if this setting is only a section and doesn't contain an actual value
         */
        public boolean isSection() {
            return this.defaultValue == null;
        }

        /**
         * Loads the value from the config and caches it if it isn't set yet
         */
        private void loadValue() {
            if (this.value != null)
                return;

            this.value = RoseStacker.getInstance().getManager(ConfigurationManager.class).getConfig().get(this.key);
        }
    }

    private CommentedFileConfiguration configuration;

    public ConfigurationManager(RoseStacker roseStacker) {
        super(roseStacker);
    }

    @Override
    public void reload() {
        File configFile = new File(this.roseStacker.getDataFolder(), "config.yml");
        boolean setHeaderFooter = !configFile.exists();
        boolean changed = setHeaderFooter;

        this.configuration = CommentedFileConfiguration.loadConfiguration(this.roseStacker, configFile);

        if (setHeaderFooter)
            this.configuration.addComments(HEADER);

        for (Setting setting : Setting.values()) {
            setting.reset();
            changed |= setting.setIfNotExists(this.configuration);
        }

        if (setHeaderFooter)
            this.configuration.addComments(FOOTER);

        if (changed)
            this.configuration.save();
    }

    @Override
    public void disable() {
        for (Setting setting : Setting.values())
            setting.reset();
    }

    /**
     * @return the config.yml as a CommentedFileConfiguration
     */
    public CommentedFileConfiguration getConfig() {
        return this.configuration;
    }

}
