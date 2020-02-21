package dev.esophose.rosestacker.manager;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import co.aikar.locales.MessageKey;
import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.command.RoseCommand;
import dev.esophose.rosestacker.command.RoseCommand.ClearallType;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import dev.esophose.rosestacker.utils.StackerUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.entity.EntityType;

public class CommandManager extends Manager {

    private boolean loaded;

    public CommandManager(RoseStacker roseStacker) {
        super(roseStacker);
        this.loaded = false;
    }

    @Override
    public void reload() {
        if (!this.loaded) {
            LocaleManager localeManager = this.roseStacker.getManager(LocaleManager.class);
            ConversionManager conversionManager = this.roseStacker.getManager(ConversionManager.class);
            StackSettingManager stackSettingManager = this.roseStacker.getManager(StackSettingManager.class);
            PaperCommandManager commandManager = new PaperCommandManager(this.roseStacker);
            commandManager.registerCommand(new RoseCommand(this.roseStacker));

            // Load custom message strings
            Map<String, String> acfCoreMessages = localeManager.getAcfCoreMessages();
            Map<String, String> acfMinecraftMessages = localeManager.getAcfMinecraftMessages();
            for (String key : acfCoreMessages.keySet())
                commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKey.of("acf-core." + key), localeManager.getLocaleMessage("prefix") + acfCoreMessages.get(key));
            for (String key : acfMinecraftMessages.keySet())
                commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKey.of("acf-minecraft." + key), localeManager.getLocaleMessage("prefix") + acfMinecraftMessages.get(key));

            CommandCompletions<BukkitCommandCompletionContext> completions = commandManager.getCommandCompletions();
            completions.registerAsyncCompletion("amount", ctx -> Arrays.asList("5", "16", "64", "256", "<amount>"));
            completions.registerAsyncCompletion("stackableBlockMaterial", ctx -> stackSettingManager.getStackableBlockTypes().stream().map(Enum::name).map(String::toLowerCase).collect(Collectors.toSet()));
            completions.registerAsyncCompletion("spawnableEntityType", ctx -> StackerUtils.getStackableEntityTypes().stream().map(Enum::name).map(String::toLowerCase).collect(Collectors.toSet()));
            completions.registerAsyncCompletion("blockStackAmounts", ctx -> {
                int maxStackAmount = Setting.BLOCK_MAX_STACK_SIZE.getInt();
                return Arrays.asList(String.valueOf(maxStackAmount), String.valueOf(maxStackAmount / 2), String.valueOf(maxStackAmount / 4), "<amount>");
            });
            completions.registerAsyncCompletion("spawnerStackAmounts", ctx -> {
                int maxStackAmount = Setting.SPAWNER_MAX_STACK_SIZE.getInt();
                return Arrays.asList(String.valueOf(maxStackAmount), String.valueOf(maxStackAmount / 2), String.valueOf(maxStackAmount / 4), "<amount>");
            });
            completions.registerAsyncCompletion("entityStackAmounts", ctx -> {
                EntityType entityType = ctx.getContextValue(EntityType.class);
                if (entityType != null) {
                    EntityStackSettings entityStackSettings = stackSettingManager.getEntityStackSettings(entityType);
                    int maxStackAmount = entityStackSettings.getMaxStackSize();
                    return Arrays.asList(String.valueOf(maxStackAmount), String.valueOf(maxStackAmount / 2), String.valueOf(maxStackAmount / 4), "<amount>");
                }
                return Collections.emptySet();
            });
            completions.registerAsyncCompletion("clearallType", ctx -> Stream.of(ClearallType.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toSet()));
            completions.registerAsyncCompletion("conversionType", ctx -> conversionManager.getEnabledConverters().stream().map(Enum::name).collect(Collectors.toSet()));

            this.loaded = true;
        }
    }

    @Override
    public void disable() {

    }

}
