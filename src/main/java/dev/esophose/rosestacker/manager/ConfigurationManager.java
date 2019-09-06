package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.config.CommentedFileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigurationManager extends Manager {

    private static final String[] HEADER = new String[] {
            "  __________                      _________ __                 __                 ",
            "  \\______   \\ ____  ______ ____  /   _____//  |______    ____ |  | __ ___________ ",
            "   |       _//  _ \\/  ___// __ \\ \\_____  \\\\   __\\__  \\ _/ ___\\|  |/ // __ \\_  __ \\",
            "   |    |   (  <_> )___ \\\\  ___/ /        \\|  |  / __ \\\\  \\___|    <\\  ___/|  | \\/",
            "   |____|_  /\\____/____  >\\___  >_______  /|__| (____  /\\___  >__|_ \\\\___  >__|   ",
            "          \\/           \\/     \\/        \\/           \\/     \\/     \\/    \\/       "
    };

    public enum Setting {
        LOCALE("locale", "en_US", "The locale to use in the /locale folder"),
        DISABLED_WORLDS("disabled-worlds", Collections.singletonList("disabled_world_name"), "A list of worlds that the plugin is disabled in"),

        STACKABLE_BLOCKS("stackable-blocks", Arrays.asList("DIAMOND_BLOCK", "GOLD_BLOCK", "IRON_BLOCK", "EMERALD_BLOCK", "LAPIS_BLOCK"), "Which blocks should be stackable?"),

        GLOBAL_ENTITY_SETTINGS("global-entity-settings", null, "Global entity settings", "Changed values in entity_settings.yml will override these values"),
        ENTITY_STACKING_ENABLED("global-entity-settings.stacking-enabled", true, "Should entity stacking be enabled at all?"),
        ENTITY_MIN_STACK_SIZE("global-entity-settings.min-stack-size", 2, "The minimum number of nearby entities required to form a stack", "Do not set this lower than 2"),
        ENTITY_MAX_STACK_SIZE("global-entity-settings.max-stack-size", 128, "The maximum number of entities that can be in a single stack"),
        ENTITY_KILL_ENTIRE_STACK_ON_DEATH("global-entity-settings.kill-entire-stack-on-death", false, "Should the entire stack of entities be killed when the main entity dies?"),

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
         * Gets the setting as a boolean
         *
         * @return The setting as a boolean
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
            return (int) this.value;
        }

        /**
         * @return the setting as a long
         */
        public long getLong() {
            this.loadValue();
            return (long) this.value;
        }

        /**
         * @return the setting a double
         */
        public double getDouble() {
            this.loadValue();
            return (double) this.value;
        }

        /**
         * @return the setting a String
         */
        public String getString() {
            this.loadValue();
            return (String) this.value;
        }

        /**
         * @return the setting as a string list
         */
        @SuppressWarnings("unchecked")
        public List<String> getStringList() {
            this.loadValue();
            return (List<String>) this.value;
        }

        public void setIfNotExists(CommentedFileConfiguration fileConfiguration) {
            this.loadValue();

            if (fileConfiguration.get(this.key) == null) {
                List<String> comments = Stream.of(this.comments).collect(Collectors.toList());
                if (!(this.defaultValue instanceof List) && this.defaultValue != null) {
                    String defaultComment = "Default: ";
                    if (this.defaultValue instanceof String) {
                        defaultComment += "'" + this.defaultValue + "'";
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
            }
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

            this.value = RoseStacker.getInstance().getConfigurationManager().getConfig().get(this.key);
        }
    }

    private CommentedFileConfiguration configuration;

    public ConfigurationManager(RoseStacker roseStacker) {
        super(roseStacker);
    }

    @Override
    public void reload() {
        File configFile = new File(this.roseStacker.getDataFolder(), "config.yml");
        boolean setHeader = !configFile.exists();

        this.configuration = CommentedFileConfiguration.loadConfiguration(this.roseStacker, configFile);

        if (setHeader)
            this.configuration.addComments(HEADER);

        for (Setting setting : Setting.values()) {
            setting.reset();
            setting.setIfNotExists(this.configuration);
        }

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
