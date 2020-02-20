package dev.esophose.rosestacker.converter;

import dev.esophose.rosestacker.RoseStacker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

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

    public abstract void convert();

}
