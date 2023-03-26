package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.conversion.ConverterType;
import dev.rosewood.rosestacker.conversion.StackPlugin;
import dev.rosewood.rosestacker.conversion.converter.StackPluginConverter;
import dev.rosewood.rosestacker.conversion.handler.ConversionHandler;
import dev.rosewood.rosestacker.stack.Stack;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ConversionManager extends Manager implements Listener {

    public static final String FILE_NAME = "convert-lock.yml";

    private final Map<StackPlugin, StackPluginConverter> converters;
    private final Set<ConversionHandler> conversionHandlers;
    private CommentedFileConfiguration convertLockConfig;

    private final DataManager dataManager;

    public ConversionManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.converters = new HashMap<>();
        this.conversionHandlers = new HashSet<>();
        this.dataManager = this.rosePlugin.getManager(DataManager.class);

        Bukkit.getPluginManager().registerEvents(this, this.rosePlugin);
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

        this.rosePlugin.getManager(LocaleManager.class).sendMessage(player, "convert-lock-conflictions");
    }

    /**
     * Loads any convert locks for conflicting stacker plugins and writes them to the lock file
     */
    private void loadConvertLocks() {
        File convertLockFile = new File(this.rosePlugin.getDataFolder(), FILE_NAME);
        if (!convertLockFile.exists()) {
            try {
                convertLockFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.convertLockConfig = CommentedFileConfiguration.loadConfiguration(convertLockFile);

        if (this.convertLockConfig.get("acknowledge-reading-warning") == null) {
            this.convertLockConfig.addComments("This file is a security measure created to prevent conflictions and/or data loss",
                    "from plugins that conflict with RoseStacker.",
                    "Any sections that you see below will list the conflicting plugins and will allow you",
                    "to re-enable stacking for certain types if you are certain that they will not conflict.");
            this.convertLockConfig.set("acknowledge-reading-warning", false, "If you acknowledge reading the above warning and want to disable", "the message displayed when joining the server, set the following to true");
        }

        this.converters.values().forEach(x -> x.configureLockFile(this.convertLockConfig));

        this.convertLockConfig.save(convertLockFile);
    }

    /**
     * Attempts to run the conversion for the given StackPlugin
     *
     * @param stackPlugin The StackPlugin to attempt to convert
     * @return true if the conversion was successful, false otherwise
     */
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
            ThreadUtils.runSync(this.rosePlugin::reload);
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    /**
     * @return true if any conversion handlers are loaded
     */
    public boolean hasConversions() {
        return !this.conversionHandlers.isEmpty();
    }

    /**
     * Runs the chunk data conversion for any conversion handlers
     *
     * @param chunkEntities The entities in the chunk to convert
     */
    public void convertChunkEntities(List<Entity> chunkEntities) {
        Set<StackType> requiredStackTypes = new HashSet<>();
        for (ConversionHandler conversionHandler : this.conversionHandlers)
            requiredStackTypes.add(conversionHandler.getRequiredDataStackType());

        Map<StackType, Set<ConversionData>> conversionData = this.dataManager.getConversionData(chunkEntities, requiredStackTypes);

        Set<Stack<?>> convertedStacks = new HashSet<>();
        for (ConversionHandler conversionHandler : this.conversionHandlers) {
            Set<ConversionData> data;
            if (conversionHandler.shouldUseChunkEntities()) {
                data = new HashSet<>();
                switch (conversionHandler.getRequiredDataStackType()) {
                    case ENTITY -> {
                        for (Entity entity : chunkEntities)
                            if (entity.getType() != EntityType.DROPPED_ITEM)
                                data.add(new ConversionData(entity));
                    }
                    case ITEM -> {
                        for (Entity entity : chunkEntities)
                            if (entity.getType() == EntityType.DROPPED_ITEM)
                                data.add(new ConversionData(entity));
                    }
                }
            } else {
                data = conversionData.get(conversionHandler.getRequiredDataStackType());
            }

            if (data.isEmpty())
                continue;

            convertedStacks.addAll(conversionHandler.handleConversion(data));
        }

        // Update nametags synchronously
        if (!convertedStacks.isEmpty())
            ThreadUtils.runSync(() -> convertedStacks.forEach(Stack::updateDisplay));
    }

    /**
     * @return a set of StackPlugins that have enabled converters
     */
    public Set<StackPlugin> getEnabledConverters() {
        return this.converters.entrySet().stream()
                .filter(x -> x.getValue().canConvert())
                .map(Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * @return true if entity stacking is prevented by a convert lock, false otherwise
     */
    public boolean isEntityStackingLocked() {
        if (this.convertLockConfig == null)
            return false;
        return this.converters.values()
                .stream()
                .filter(StackPluginConverter::canConvert)
                .anyMatch(x -> x.isStackingLocked(this.convertLockConfig, StackType.ENTITY));
    }

    /**
     * @return true if item stacking is prevented by a convert lock, false otherwise
     */
    public boolean isItemStackingLocked() {
        if (this.convertLockConfig == null)
            return false;
        return this.converters.values()
                .stream()
                .filter(StackPluginConverter::canConvert)
                .anyMatch(x -> x.isStackingLocked(this.convertLockConfig, StackType.ITEM));
    }

    /**
     * @return true if block stacking is prevented by a convert lock, false otherwise
     */
    public boolean isBlockStackingLocked() {
        if (this.convertLockConfig == null)
            return false;
        return this.converters.values()
                .stream()
                .filter(StackPluginConverter::canConvert)
                .anyMatch(x -> x.isStackingLocked(this.convertLockConfig, StackType.BLOCK));
    }

    /**
     * @return true if spawner stacking is prevented by a convert lock, false otherwise
     */
    public boolean isSpawnerStackingLocked() {
        if (this.convertLockConfig == null)
            return false;
        return this.converters.values()
                .stream()
                .filter(StackPluginConverter::canConvert)
                .anyMatch(x -> x.isStackingLocked(this.convertLockConfig, StackType.SPAWNER));
    }

}
