package dev.esophose.rosestacker.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;

import java.io.File;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class CommentedFileConfiguration {

    private int comments;
    private CommentedFileConfigurationHelper helper;
    private File file;
    private FileConfiguration config;

    public CommentedFileConfiguration(Reader configStream, File configFile, int comments, JavaPlugin plugin) {
        this.comments = comments;
        this.helper = new CommentedFileConfigurationHelper(plugin);

        this.file = configFile;
        this.config = YamlConfiguration.loadConfiguration(configStream);
    }

    public static CommentedFileConfiguration loadConfiguration(JavaPlugin plugin, File file) {
        return new CommentedFileConfigurationHelper(plugin).getNewConfig(file);
    }

    public Object get(String path) {
        return this.config.get(path);
    }

    public Object get(String path, Object def) {
        return this.config.get(path, def);
    }

    public String getString(String path) {
        return this.config.getString(path);
    }

    public String getString(String path, String def) {
        return this.config.getString(path, def);
    }

    public List<String> getStringList(String path) {
        return this.config.getStringList(path);
    }

    public int getInt(String path) {
        return this.config.getInt(path);
    }

    public int getInt(String path, int def) {
        return this.config.getInt(path, def);
    }

    public boolean getBoolean(String path) {
        return this.config.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean def) {
        return this.config.getBoolean(path, def);
    }

    public ConfigurationSection createSection(String path) {
        return this.config.createSection(path);
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return this.config.getConfigurationSection(path);
    }

    public double getDouble(String path) {
        return this.config.getDouble(path);
    }

    public double getDouble(String path, double def) {
        return this.config.getDouble(path, def);
    }

    public List<?> getList(String path) {
        return this.config.getList(path);
    }

    public List<?> getList(String path, List<?> def) {
        return this.config.getList(path, def);
    }

    public boolean contains(String path) {
        return this.config.contains(path);
    }

    public void removeKey(String path) {
        this.config.set(path, null);
    }

    public void set(String path, Object value) {
        this.config.set(path, value);
    }

    public void set(String path, Object value, String... comments) {
        if (!this.config.contains(path)) {
            int subpathIndex = path.lastIndexOf('.');
            String subpath = subpathIndex == -1 ? "" : path.substring(0, subpathIndex) + '.';

            for (String comment : comments) {
                this.config.set(subpath + this.helper.getPluginName() + "_COMMENT_" + this.comments, " " + comment);
                this.comments++;
            }
        }

        this.config.set(path, value);
    }

    public void addComments(String... comments) {
        for (String comment : comments) {
            this.config.set(this.helper.getPluginName() + "_COMMENT_" + this.comments, " " + comment);
            this.comments++;
        }
    }

    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(this.helper.getConfigContent(this.file));
    }

    public void save() {
        this.save(false);
    }

    public void save(boolean compactLines) {
        String config = this.getConfigAsString();
        this.helper.saveConfig(config, this.file, compactLines);
    }

    public void save(File file) {
        this.save(file, false);
    }

    public void save(File file, boolean compactLines) {
        String config = this.getConfigAsString();
        this.helper.saveConfig(config, file, compactLines);
    }

    private String getConfigAsString() {
        // Edit the configuration to how we want it
        YamlConfiguration yamlConfiguration = (YamlConfiguration) this.config;
        try {
            Field field_yamlOptions = YamlConfiguration.class.getDeclaredField("yamlOptions");
            field_yamlOptions.setAccessible(true);
            DumperOptions yamlOptions = (DumperOptions) field_yamlOptions.get(yamlConfiguration);
            yamlOptions.setWidth(Integer.MAX_VALUE);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return this.config.saveToString();
    }

    public Set<String> getKeys(boolean deep) {
        return this.config.getKeys(deep);
    }

}
