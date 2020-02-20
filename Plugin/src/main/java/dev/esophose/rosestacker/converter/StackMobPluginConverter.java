package dev.esophose.rosestacker.converter;

import dev.esophose.rosestacker.RoseStacker;
import uk.antiperson.stackmob.StackMob;

public class StackMobPluginConverter extends StackPluginConverter {

    private StackMob stackMob;

    public StackMobPluginConverter(RoseStacker roseStacker) {
        super(roseStacker, "StackMob");

        this.stackMob = (StackMob) this.plugin;
    }

    @Override
    public void convert() {

    }

}
