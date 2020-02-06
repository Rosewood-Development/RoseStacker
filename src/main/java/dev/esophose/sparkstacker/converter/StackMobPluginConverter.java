package dev.esophose.sparkstacker.converter;

import dev.esophose.sparkstacker.SparkStacker;
import uk.antiperson.stackmob.StackMob;

public class StackMobPluginConverter extends StackPluginConverter {

    private StackMob stackMob;

    public StackMobPluginConverter(SparkStacker sparkStacker) {
        super(sparkStacker, "StackMob");

        this.stackMob = (StackMob) this.plugin;
    }

    @Override
    public void convert() {

    }

}
