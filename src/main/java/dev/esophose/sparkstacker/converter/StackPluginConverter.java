package dev.esophose.sparkstacker.converter;

import dev.esophose.sparkstacker.SparkStacker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class StackPluginConverter {

    protected SparkStacker sparkStacker;
    protected Plugin plugin;

    public StackPluginConverter(SparkStacker sparkStacker, String pluginName) {
        this.sparkStacker = sparkStacker;
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
