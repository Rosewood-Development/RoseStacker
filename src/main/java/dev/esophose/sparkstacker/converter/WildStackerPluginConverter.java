package dev.esophose.sparkstacker.converter;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import dev.esophose.sparkstacker.SparkStacker;

public class WildStackerPluginConverter extends StackPluginConverter {

    private WildStackerPlugin wildStacker;

    public WildStackerPluginConverter(SparkStacker sparkStacker) {
        super(sparkStacker, "WildStacker");

        this.wildStacker = (WildStackerPlugin) this.plugin;
    }

    @Override
    public void convert() {

    }

}
