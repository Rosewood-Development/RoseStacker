package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.conversion.ConverterType;
import dev.rosewood.rosestacker.conversion.StackPlugin;
import dev.rosewood.rosestacker.conversion.converter.StackPluginConverter;
import dev.rosewood.rosestacker.conversion.handler.ConversionHandler;
import dev.rosewood.rosestacker.stack.Stack;
import dev.rosewood.rosestacker.stack.StackType;
import java.io.File;
import java.io.IOException;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ConversionManager extends Manager implements Listener {

    public static final String FILE_NAME = "convert-lock.yml";

    private Map<StackPlugin, StackPluginConverter> converters;
    private Set<ConversionHandler> conversionHandlers;
    private CommentedFileConfiguration convertLockConfig;

    private DataManager dataManager;

    public ConversionManager(RoseStacker roseStacker) {
        super(roseStacker);

        this.converters = new HashMap<>();
        this.conversionHandlers = new HashSet<>();
        this.dataManager = this.roseStacker.getManager(DataManager.class);

        Bukkit.getPluginManager().registerEvents(this, this.roseStacker);
    }

    @Override
    public void reload() {
        for (StackPlugin stackPlugin : StackPlugin.values())
            this.converters.put(stackPlugin, stackPlugin.getConverter());

        for (ConverterType converterType : this.dataManager.getConversionHandlers())
            this.conversionHandlers.add(converterType.getConversionHandler());

        if (!this.getEnabledConverters().isEmpty())
            this.loadConvertLocks();
    }

    @Override
    public void disable() {
        this.converters.clear();
        this.conversionHandlers.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp())
            return;

        if (this.convertLockConfig == null || this.convertLockConfig.getBoolean("acknowledge-reading-warning"))
            return;

        this.roseStacker.getManager(LocaleManager.class).sendMessage(player, "convert-lock-conflictions");
    }

    private void loadConvertLocks() {
        File convertLockFile = new File(this.roseStacker.getDataFolder(), FILE_NAME);
        if (!convertLockFile.exists()) {
            try {
                convertLockFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.convertLockConfig = CommentedFileConfiguration.loadConfiguration(this.roseStacker, convertLockFile);

        if (this.convertLockConfig.get("acknowledge-reading-warning") == null) {
            this.convertLockConfig.addComments("This file is a security measure created to prevent conflictions and/or data loss",
                    "from plugins that conflict with RoseStacker.",
                    "Any sections that you see below will list the conflicting plugins and will allow you",
                    "to re-enable stacking for certain types if you are certain that they will not conflict.");
            this.convertLockConfig.set("acknowledge-reading-warning", false, "If you acknowledge reading the above warning and want to disable", "the message displayed when joining the server, set the following to true");
        }

        this.converters.values().forEach(x -> x.configureLockFile(this.convertLockConfig));

        this.convertLockConfig.save();
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

            // Reload plugin to convert and update data
            Bukkit.getScheduler().runTask(this.roseStacker, this.roseStacker::reload);
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

        Set<Stack<?>> convertedStacks = new HashSet<>();
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

            convertedStacks.addAll(conversionHandler.handleConversion(data));
        }

        // Update nametags synchronously
        if (!convertedStacks.isEmpty())
            Bukkit.getScheduler().runTask(this.roseStacker, () -> convertedStacks.forEach(Stack::updateDisplay));
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

    public boolean isEntityStackingLocked() {
        if (this.convertLockConfig == null)
            return false;
        return this.converters.values()
                .stream()
                .filter(StackPluginConverter::canConvert)
                .anyMatch(x -> x.isStackingLocked(this.convertLockConfig, StackType.ENTITY));
    }

    public boolean isItemStackingLocked() {
        if (this.convertLockConfig == null)
            return false;
        return this.converters.values()
                .stream()
                .filter(StackPluginConverter::canConvert)
                .anyMatch(x -> x.isStackingLocked(this.convertLockConfig, StackType.ITEM));
    }

    public boolean isBlockStackingLocked() {
        if (this.convertLockConfig == null)
            return false;
        return this.converters.values()
                .stream()
                .filter(StackPluginConverter::canConvert)
                .anyMatch(x -> x.isStackingLocked(this.convertLockConfig, StackType.BLOCK));
    }

    public boolean isSpawnerStackingLocked() {
        if (this.convertLockConfig == null)
            return false;
        return this.converters.values()
                .stream()
                .filter(StackPluginConverter::canConvert)
                .anyMatch(x -> x.isStackingLocked(this.convertLockConfig, StackType.SPAWNER));
    }

}
