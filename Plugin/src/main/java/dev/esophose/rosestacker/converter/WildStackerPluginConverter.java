package dev.esophose.rosestacker.converter;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import dev.esophose.rosestacker.RoseStacker;

public class WildStackerPluginConverter extends StackPluginConverter {

    private WildStackerPlugin wildStacker;

    public WildStackerPluginConverter(RoseStacker roseStacker) {
        super(roseStacker, "WildStacker");

        this.wildStacker = (WildStackerPlugin) this.plugin;
    }

    @Override
    public void convert() {

    }

}
