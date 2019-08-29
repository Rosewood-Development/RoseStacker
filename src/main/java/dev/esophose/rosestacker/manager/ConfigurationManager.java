package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class ConfigurationManager extends Manager {

    public enum Setting {
        SERVER_TYPE(SettingType.STRING),
        LOCALE(SettingType.STRING),
        DISABLED_WORLDS(SettingType.STRING_LIST),

        MYSQL_ENABLED(SettingType.BOOLEAN),
        MYSQL_HOSTNAME(SettingType.STRING),
        MYSQL_PORT(SettingType.INT),
        MYSQL_DATABASE_NAME(SettingType.STRING),
        MYSQL_USER_NAME(SettingType.STRING),
        MYSQL_USER_PASSWORD(SettingType.STRING),
        MYSQL_USE_SSL(SettingType.BOOLEAN);

        private SettingType settingType;
        private Object value = null;

        Setting(SettingType settingType) {
            this.settingType = settingType;
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
         * Gets the setting as an int
         *
         * @return The setting as an int
         */
        public int getInt() {
            this.loadValue();
            return (int) this.value;
        }

        /**
         * Gets the setting as a double
         *
         * @return The setting a double
         */
        public double getDouble() {
            this.loadValue();
            return (double) this.value;
        }

        /**
         * Gets the setting as a String
         *
         * @return The setting a String
         */
        public String getString() {
            this.loadValue();
            return (String) this.value;
        }

        /**
         * Gets the setting as a string list
         *
         * @return The setting as a string list
         */
        @SuppressWarnings("unchecked")
        public List<String> getStringList() {
            this.loadValue();
            return (List<String>) this.value;
        }

        /**
         * Resets the cached value
         */
        public void reset() {
            this.value = null;
        }

        /**
         * Loads the value from the config and caches it if it isn't set yet
         */
        private void loadValue() {
            if (this.value != null)
                return;

            FileConfiguration config = RoseStacker.getInstance().getConfigurationManager().getConfig();
            switch (this.settingType) {
                case BOOLEAN:
                    this.value = config.getBoolean(this.getNameAsKey());
                    break;
                case INT:
                    this.value = config.getInt(this.getNameAsKey());
                    break;
                case DOUBLE:
                    this.value = config.getDouble(this.getNameAsKey());
                    break;
                case STRING:
                    this.value = config.getString(this.getNameAsKey());
                    break;
                case STRING_LIST:
                    this.value = config.getStringList(this.getNameAsKey());
                    break;
            }
        }

        /**
         * Gets the name of this Setting as a FileConfiguration-compatible key
         *
         * @return The key for a FileConfiguration
         */
        private String getNameAsKey() {
            return this.name().replace("_", "-").toLowerCase();
        }
    }

    private enum SettingType {
        BOOLEAN,
        INT,
        DOUBLE,
        STRING,
        STRING_LIST
    }

    private YamlConfiguration configuration;

    public ConfigurationManager(RoseStacker roseStacker) {
        super(roseStacker);
    }

    @Override
    public void reload() {
        File configFile = new File(this.roseStacker.getDataFolder() + "/config.yml");

        // Create the new config if it doesn't exist
        if (!configFile.exists())
            this.roseStacker.saveResource("config.yml", false);

        this.configuration = YamlConfiguration.loadConfiguration(configFile);

        for (Setting setting : Setting.values())
            setting.reset();
    }

    @Override
    public void disable() {
        for (Setting setting : Setting.values())
            setting.reset();
    }

    /**
     * Gets the config.yml as a YamlConfiguration
     *
     * @return The YamlConfiguration of the config.yml
     */
    public YamlConfiguration getConfig() {
        return this.configuration;
    }

}
