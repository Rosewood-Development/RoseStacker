package dev.rosewood.rosestacker.conversion;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.converter.EpicSpawnersPluginConverter;
import dev.rosewood.rosestacker.conversion.converter.StackMobPluginConverter;
import dev.rosewood.rosestacker.conversion.converter.StackPluginConverter;
import dev.rosewood.rosestacker.conversion.converter.UltimateStackerPluginConverter;
import dev.rosewood.rosestacker.conversion.converter.WildStackerPluginConverter;
import dev.rosewood.rosestacker.stack.StackType;
import java.util.Collections;
import java.util.List;

public enum StackPlugin {

    WildStacker(WildStackerPluginConverter.class, StackType.ENTITY, StackType.ITEM, StackType.BLOCK, StackType.SPAWNER),
    UltimateStacker(UltimateStackerPluginConverter.class, StackType.ENTITY, StackType.ITEM, StackType.BLOCK, StackType.SPAWNER),
    EpicSpawners(EpicSpawnersPluginConverter.class, StackType.SPAWNER),
    StackMob(StackMobPluginConverter.class, StackType.ENTITY);

    private final Class<? extends StackPluginConverter> converterClass;
    private final List<StackType> stackTypes;

    StackPlugin(Class<? extends StackPluginConverter> conveterClass, StackType... stackTypes) {
        this.converterClass = conveterClass;
        this.stackTypes = List.of(stackTypes);
    }

    /**
     * @return a newly created StackPluginConverter for this StackPlugin
     */
    public StackPluginConverter getConverter() {
        try {
            return this.converterClass.getConstructor(RosePlugin.class).newInstance(RoseStacker.getInstance());
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * @return the StackTypes that this StackPlugin is capable of converting
     */
    public List<StackType> getStackTypes() {
        return Collections.unmodifiableList(this.stackTypes);
    }

}
