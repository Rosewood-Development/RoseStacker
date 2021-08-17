package dev.rosewood.rosestacker.conversion.converter;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.conversion.ConverterType;
import dev.rosewood.rosestacker.conversion.StackPlugin;
import dev.rosewood.rosestacker.stack.StackType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

/**
 * Handles the initial conversion or copying of data between plugins
 */
public abstract class StackPluginConverter {

    protected RosePlugin rosePlugin;
    protected Plugin plugin;
    private final StackPlugin stackPlugin;
    private final Set<ConverterType> converterTypes;

    public StackPluginConverter(RosePlugin rosePlugin, String pluginName, StackPlugin stackPlugin, ConverterType... converterTypes) {
        this.rosePlugin = rosePlugin;
        this.plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        this.stackPlugin = stackPlugin;
        this.converterTypes = new HashSet<>(Arrays.asList(converterTypes));
    }

    /**
     * @return true if this converter can be used, false otherwise
     */
    public boolean canConvert() {
        return this.plugin != null && this.plugin.isEnabled();
    }

    /**
     * Disables the plugin that this converter converts from
     */
    public void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(this.plugin);
    }

    /**
     * @return the types of stacks that this converter can convert from
     */
    public Set<ConverterType> getConverterTypes() {
        return this.converterTypes;
    }

    /**
     * Runs the conversion
     */
    public abstract void convert();

    /**
     * Adds stack types that are blocked by the conflicting convert plugin, if any
     *
     * @param config The config to add values to
     */
    public void configureLockFile(CommentedFileConfiguration config) {
        if (this.plugin == null || config.isConfigurationSection(this.plugin.getName()))
            return;

        CommentedConfigurationSection configurationSection = config.createSection(this.plugin.getName());
        for (StackType stackType : this.stackPlugin.getStackTypes())
            configurationSection.set("lock-" + stackType.name().toLowerCase() + "-stacking", true);
    }

    /**
     * Checks if stacking for a specific type is blocked due to a confliction with this convert plugin
     *
     * @param config The config to read values from
     * @param stackType The stack type to check
     * @return true if stacking for the given type is blocked, false otherwise
     */
    public boolean isStackingLocked(CommentedFileConfiguration config, StackType stackType) {
        if (this.plugin == null || !this.plugin.isEnabled() || !this.stackPlugin.getStackTypes().contains(stackType))
            return false;
        return config.getConfigurationSection(this.plugin.getName()).getBoolean("lock-" + stackType.name().toLowerCase() + "-stacking");
    }

    /**
     * Parses a Location string and character separator into a Location
     *
     * @param locationString The location string
     * @param separator The character separator
     * @return the parsed Location, or null if the location string was invalid
     */
    protected Location parseLocation(String locationString, char separator) {
        String[] pieces = locationString.split(Pattern.quote(String.valueOf(separator)));
        if (pieces.length != 4)
            return null;

        World world = Bukkit.getWorld(pieces[0]);
        if (world == null)
            return null;

        int x = Integer.parseInt(pieces[1]);
        int y = Integer.parseInt(pieces[2]);
        int z = Integer.parseInt(pieces[3]);

        return new Location(world, x, y, z);
    }

}
