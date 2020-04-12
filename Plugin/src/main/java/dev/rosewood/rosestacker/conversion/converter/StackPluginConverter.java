package dev.rosewood.rosestacker.conversion.converter;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.ConverterType;
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

    protected RoseStacker roseStacker;
    protected Plugin plugin;
    private Set<ConverterType> converterTypes;

    public StackPluginConverter(RoseStacker roseStacker, String pluginName, ConverterType... converterTypes) {
        this.roseStacker = roseStacker;
        this.plugin = Bukkit.getPluginManager().getPlugin(pluginName);
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
