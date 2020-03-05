package dev.esophose.rosestacker.conversion.converter;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.conversion.ConverterType;
import uk.antiperson.stackmob.StackMob;

public class StackMobPluginConverter extends StackPluginConverter {

    private StackMob stackMob;

    public StackMobPluginConverter(RoseStacker roseStacker) {
        super(roseStacker, "StackMob", ConverterType.STACKMOB);

        this.stackMob = (StackMob) this.plugin;
    }

    @Override
    public void convert() {
        // There's actually nothing to do here since everything is stored in PersistentDataContainers
    }

}
