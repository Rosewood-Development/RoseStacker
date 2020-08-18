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
    private StackPlugin stackPlugin;
    private Set<ConverterType> converterTypes;

    public StackPluginConverter(RosePlugin rosePlugin, String pluginName, StackPlugin stackPlugin, ConverterType... converterTypes) {
        this.rosePlugin = rosePlugin;
        this.plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        this.stackPlugin = stackPlugin;
        this.converterTypes = new HashSet<>(Arrays.asList(converterTypes));
    }

    public boolean canConvert() {
        return this.plugin != null && this.plugin.isEnabled();
    }

    public void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(this.plugin);
    }

    public Set<ConverterType> getConverterTypes() {
        return this.converterTypes;
    }

    public abstract void convert();

    public void configureLockFile(CommentedFileConfiguration config) {
        if (this.plugin == null || config.isConfigurationSection(this.plugin.getName()))
            return;

        CommentedConfigurationSection configurationSection = config.createSection(this.plugin.getName());
        for (StackType stackType : this.stackPlugin.getStackTypes())
            configurationSection.set("lock-" + stackType.name().toLowerCase() + "-stacking", true);
    }

    public boolean isStackingLocked(CommentedFileConfiguration config, StackType stackType) {
        if (this.plugin == null || !this.plugin.isEnabled() || !this.stackPlugin.getStackTypes().contains(stackType))
            return false;
        return config.getConfigurationSection(this.plugin.getName()).getBoolean("lock-" + stackType.name().toLowerCase() + "-stacking");
    }

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
