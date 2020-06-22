package dev.rosewood.rosestacker.conversion.converter;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.ConverterType;
import dev.rosewood.rosestacker.conversion.StackPlugin;
import uk.antiperson.stackmob.StackMob;

public class StackMobPluginConverter extends StackPluginConverter {

    private StackMob stackMob;

    public StackMobPluginConverter(RoseStacker roseStacker) {
        super(roseStacker, "StackMob", StackPlugin.StackMob, ConverterType.STACKMOB);

        this.stackMob = (StackMob) this.plugin;
    }

    @Override
    public void convert() {
        // There's actually nothing to do here since everything is stored in PersistentDataContainers
    }

}
