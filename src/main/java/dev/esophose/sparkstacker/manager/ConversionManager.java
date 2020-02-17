package dev.esophose.sparkstacker.manager;

import dev.esophose.sparkstacker.SparkStacker;
import dev.esophose.sparkstacker.converter.StackMobPluginConverter;
import dev.esophose.sparkstacker.converter.StackPluginConverter;
import dev.esophose.sparkstacker.converter.UltimateStackerPluginConverter;
import dev.esophose.sparkstacker.converter.WildStackerPluginConverter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class ConversionManager extends Manager {

    private Map<StackPlugin, StackPluginConverter> converters;

    public ConversionManager(SparkStacker sparkStacker) {
        super(sparkStacker);

        this.converters = new HashMap<>();
    }

    @Override
    public void reload() {
        this.converters.clear();

        for (StackPlugin stackPlugin : StackPlugin.values())
            this.converters.put(stackPlugin, stackPlugin.getConverter());
    }

    @Override
    public void disable() {

    }

    public boolean convert(StackPlugin stackPlugin) {
        StackPluginConverter converter = this.converters.get(stackPlugin);
        if (!converter.canConvert())
            return false;

        converter.convert();
        converter.disablePlugin();
        return true;
    }

    public Set<StackPlugin> getEnabledConverters() {
        return this.converters.entrySet().stream()
                .filter(x -> x.getValue().canConvert())
                .map(Entry::getKey)
                .collect(Collectors.toSet());
    }

    public enum StackPlugin {
        WildStacker(WildStackerPluginConverter.class),
        UltimateStacker(UltimateStackerPluginConverter.class),
        StackMob(StackMobPluginConverter.class);

        private final Class<? extends StackPluginConverter> converterClass;

        StackPlugin(Class<? extends StackPluginConverter> conveterClass) {
            this.converterClass = conveterClass;
        }

        public StackPluginConverter getConverter() {
            try {
                return this.converterClass.getConstructor(SparkStacker.class).newInstance(SparkStacker.getInstance());
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

}
