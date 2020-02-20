package dev.esophose.rosestacker.converter;

import com.songoda.ultimatestacker.UltimateStacker;
import dev.esophose.rosestacker.RoseStacker;

public class UltimateStackerPluginConverter extends StackPluginConverter {

    private UltimateStacker ultimateStacker;

    public UltimateStackerPluginConverter(RoseStacker roseStacker) {
        super(roseStacker, "UltimateStacker");

        this.ultimateStacker = (UltimateStacker) this.plugin;
    }

    @Override
    public void convert() {

    }

}
