package dev.esophose.sparkstacker.converter;

import com.songoda.ultimatestacker.UltimateStacker;
import dev.esophose.sparkstacker.SparkStacker;

public class UltimateStackerPluginConverter extends StackPluginConverter {

    private UltimateStacker ultimateStacker;

    public UltimateStackerPluginConverter(SparkStacker sparkStacker) {
        super(sparkStacker, "UltimateStacker");

        this.ultimateStacker = (UltimateStacker) this.plugin;
    }

    @Override
    public void convert() {

    }

}
