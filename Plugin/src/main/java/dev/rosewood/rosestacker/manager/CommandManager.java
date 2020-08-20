package dev.rosewood.rosestacker.manager;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.PaperCommandManager;
import co.aikar.locales.MessageKey;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosestacker.command.RoseCommand;
import dev.rosewood.rosestacker.command.RoseCommand.ClearallType;
import dev.rosewood.rosestacker.conversion.handler.ConversionHandler;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class CommandManager extends Manager {

    private final PaperCommandManager commandManager;

    public CommandManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.commandManager = new PaperCommandManager(this.rosePlugin);
        this.commandManager.registerCommand(new RoseCommand(this.rosePlugin), true);
    }

    @Override
    public void reload() {
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
        ConversionManager conversionManager = this.rosePlugin.getManager(ConversionManager.class);
        StackSettingManager stackSettingManager = this.rosePlugin.getManager(StackSettingManager.class);

        // Load custom message strings
        Map<String, String> acfCoreMessages = localeManager.getAcfCoreMessages();
        Map<String, String> acfMinecraftMessages = localeManager.getAcfMinecraftMessages();
        for (String key : acfCoreMessages.keySet())
            this.commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKey.of("acf-core." + key), HexUtils.colorify(localeManager.getLocaleMessage("prefix") + acfCoreMessages.get(key)));
        for (String key : acfMinecraftMessages.keySet())
            this.commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKey.of("acf-minecraft." + key), HexUtils.colorify(localeManager.getLocaleMessage("prefix") + acfMinecraftMessages.get(key)));

        CommandCompletions<BukkitCommandCompletionContext> completions = this.commandManager.getCommandCompletions();
        completions.registerStaticCompletion("stackableBlockMaterial", () -> stackSettingManager.getStackableBlockTypes().stream().map(Enum::name).map(String::toLowerCase).collect(Collectors.toSet()));
        completions.registerStaticCompletion("spawnableSpawnerEntityType", () -> stackSettingManager.getStackableSpawnerTypes().stream().map(Enum::name).map(String::toLowerCase).collect(Collectors.toSet()));
        completions.registerStaticCompletion("spawnableEggEntityType", () -> stackSettingManager.getStackableEntityTypes().stream().filter(x -> {
            EntityStackSettings stackSettings = stackSettingManager.getEntityStackSettings(x);
            return stackSettings.getSpawnEggMaterial() != null;
        }).map(Enum::name).map(String::toLowerCase).collect(Collectors.toSet()));
        completions.registerAsyncCompletion("blockStackAmounts", ctx -> {
            Material blockType = ctx.getContextValue(Material.class);
            if (blockType == null)
                return Collections.emptyList();

            BlockStackSettings blockStackSettings = stackSettingManager.getBlockStackSettings(blockType);
            int maxStackAmount = blockStackSettings.getMaxStackSize();
            return Arrays.asList(String.valueOf(maxStackAmount), String.valueOf(maxStackAmount / 2), String.valueOf(maxStackAmount / 4), "<amount>");
        });
        completions.registerAsyncCompletion("spawnerStackAmounts", ctx -> {
            EntityType entityType = ctx.getContextValue(EntityType.class);
            if (entityType == null)
                return Collections.emptySet();

            SpawnerStackSettings spawnerStackSettings = stackSettingManager.getSpawnerStackSettings(entityType);
            int maxStackAmount = spawnerStackSettings.getMaxStackSize();
            return Arrays.asList(String.valueOf(maxStackAmount), String.valueOf(maxStackAmount / 2), String.valueOf(maxStackAmount / 4), "<amount>");
        });
        completions.registerAsyncCompletion("entityStackAmounts", ctx -> {
            EntityType entityType = ctx.getContextValue(EntityType.class);
            if (entityType == null)
                return Collections.emptySet();

            EntityStackSettings entityStackSettings = stackSettingManager.getEntityStackSettings(entityType);
            int maxStackAmount = entityStackSettings.getMaxStackSize();
            return Arrays.asList(String.valueOf(maxStackAmount), String.valueOf(maxStackAmount / 2), String.valueOf(maxStackAmount / 4), "<amount>");
        });
        completions.registerStaticCompletion("clearallType", () -> Stream.of(ClearallType.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toSet()));
        completions.registerStaticCompletion("stackType", () -> Stream.of(StackType.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toSet()));
        completions.registerAsyncCompletion("conversionType", ctx -> conversionManager.getEnabledConverters().stream().map(Enum::name).collect(Collectors.toSet()));
        completions.registerAsyncCompletion("conversionEnabledType", ctx -> conversionManager.getEnabledHandlers().stream().map(ConversionHandler::getRequiredDataStackType).map(Enum::name).map(String::toLowerCase).collect(Collectors.toSet()));

        completions.registerAsyncCompletion("translationLocales", ctx -> localeManager.getPossibleTranslationLocales());

        this.commandManager.getCommandConditions().addCondition(int.class, "limits", (c, exec, value) -> {
            if (value == null)
                return;

            if (c.hasConfig("min") && c.getConfigValue("min", 0) > value)
                throw new ConditionFailedException(MessageKeys.PLEASE_SPECIFY_AT_LEAST, "{min}", String.valueOf(c.getConfigValue("min", 0)));

            if (c.hasConfig("max") && c.getConfigValue("max", Integer.MAX_VALUE) < value)
                throw new ConditionFailedException(MessageKeys.PLEASE_SPECIFY_AT_MOST, "{max}", String.valueOf(c.getConfigValue("max", Integer.MAX_VALUE)));
        });
    }

    @Override
    public void disable() {

    }

}
