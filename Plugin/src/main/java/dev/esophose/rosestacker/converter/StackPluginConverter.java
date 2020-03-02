package dev.esophose.rosestacker.converter;

import dev.esophose.rosestacker.RoseStacker;
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

    public StackPluginConverter(RoseStacker roseStacker, String pluginName) {
        this.roseStacker = roseStacker;
        this.plugin = Bukkit.getPluginManager().getPlugin(pluginName);
    }

    public boolean canConvert() {
        return this.plugin != null;
    }

    public void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(this.plugin);
    }

    public abstract void convert() throws Exception;

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
