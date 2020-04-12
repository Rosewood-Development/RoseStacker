package dev.rosewood.rosestacker.conversion;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.converter.EpicSpawnersPluginConverter;
import dev.rosewood.rosestacker.conversion.converter.StackMobPluginConverter;
import dev.rosewood.rosestacker.conversion.converter.StackPluginConverter;
import dev.rosewood.rosestacker.conversion.converter.UltimateStackerPluginConverter;
import dev.rosewood.rosestacker.conversion.converter.WildStackerPluginConverter;

public enum StackPlugin {

    WildStacker(WildStackerPluginConverter.class),
    UltimateStacker(UltimateStackerPluginConverter.class),
    EpicSpawners(EpicSpawnersPluginConverter.class),
    StackMob(StackMobPluginConverter.class);

    private final Class<? extends StackPluginConverter> converterClass;

    StackPlugin(Class<? extends StackPluginConverter> conveterClass) {
        this.converterClass = conveterClass;
    }

    public StackPluginConverter getConverter() {
        try {
            return this.converterClass.getConstructor(RoseStacker.class).newInstance(RoseStacker.getInstance());
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
            return null;
        }
    }

}