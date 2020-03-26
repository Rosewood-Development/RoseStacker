package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.conversion.ConversionData;
import dev.esophose.rosestacker.conversion.ConverterType;
import dev.esophose.rosestacker.conversion.StackPlugin;
import dev.esophose.rosestacker.conversion.converter.StackPluginConverter;
import dev.esophose.rosestacker.conversion.handler.ConversionHandler;
import dev.esophose.rosestacker.stack.StackType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class ConversionManager extends Manager {

    private Map<StackPlugin, StackPluginConverter> converters;
    private Set<ConversionHandler> conversionHandlers;
    private DataManager dataManager;

    public ConversionManager(RoseStacker roseStacker) {
        super(roseStacker);

        this.converters = new HashMap<>();
        this.conversionHandlers = new HashSet<>();
        this.dataManager = this.roseStacker.getManager(DataManager.class);
    }

    @Override
    public void reload() {
        this.converters.clear();
        this.conversionHandlers.clear();

        for (StackPlugin stackPlugin : StackPlugin.values())
            this.converters.put(stackPlugin, stackPlugin.getConverter());

        for (ConverterType converterType : this.dataManager.getConversionHandlers())
            this.conversionHandlers.add(converterType.getConversionHandler());

        if (this.conversionHandlers.size() > 0 || this.getEnabledConverters().size() > 0)
            this.loadConvertLocks();
    }

    @Override
    public void disable() {
        this.converters.clear();
    }

    private void loadConvertLocks() {
        // TODO: See discord for feature suggestion implementation
        // TODO: Need to manage a way to disable certain stack types in the StackManager
    }

    public boolean convert(StackPlugin stackPlugin) {
        StackPluginConverter converter = this.converters.get(stackPlugin);
        if (!converter.canConvert())
            return false;

        try {
            // Convert, then disable the converted plugin
            converter.convert();
            converter.disablePlugin();

            // Make sure we set the conversion handlers
            this.dataManager.setConversionHandlers(converter.getConverterTypes());

            // Convert data for all loaded chunks
            Set<Chunk> loadedChunks = new HashSet<>();
            for (World world : Bukkit.getWorlds())
                loadedChunks.addAll(Arrays.asList(world.getLoadedChunks()));

            Bukkit.getScheduler().runTaskAsynchronously(this.roseStacker, () -> this.convertChunks(loadedChunks));
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    public void convertChunks(Set<Chunk> chunks) {
        if (this.conversionHandlers.isEmpty())
            return;

        Set<StackType> requiredStackTypes = new HashSet<>();
        for (ConversionHandler conversionHandler : this.conversionHandlers)
            requiredStackTypes.add(conversionHandler.getRequiredDataStackType());

        Set<Entity> entities = new HashSet<>();
        for (Chunk chunk : chunks)
            entities.addAll(Arrays.asList(chunk.getEntities()));

        Map<StackType, Set<ConversionData>> conversionData = this.dataManager.getConversionData(entities, requiredStackTypes);

        for (ConversionHandler conversionHandler : this.conversionHandlers) {
            Set<ConversionData> data;
            if (conversionHandler.isDataAlwaysRequired()) {
                data = new HashSet<>();
                for (Entity entity : entities)
                    data.add(new ConversionData(entity));
            } else {
                data = conversionData.get(conversionHandler.getRequiredDataStackType());
            }

            if (data.isEmpty())
                continue;

            conversionHandler.handleConversion(data);
        }
    }

    public Set<StackPlugin> getEnabledConverters() {
        return this.converters.entrySet().stream()
                .filter(x -> x.getValue().canConvert())
                .map(Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Set<ConversionHandler> getEnabledHandlers() {
        return Collections.unmodifiableSet(this.conversionHandlers);
    }

}
