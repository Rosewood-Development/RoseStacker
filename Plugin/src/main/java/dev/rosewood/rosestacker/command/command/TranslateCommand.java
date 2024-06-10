package dev.rosewood.rosestacker.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.command.argument.StackerArgumentHandlers;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class TranslateCommand extends BaseRoseCommand {

    private final RosePlugin rosePlugin;

    public TranslateCommand(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.rosePlugin = rosePlugin;
    }

    @RoseExecutable
    public void execute(CommandContext context, String locale, String spawnerFormat) {
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
        StackSettingManager stackSettingManager = this.rosePlugin.getManager(StackSettingManager.class);

        if (spawnerFormat == null) {
            spawnerFormat = "{}";
            localeManager.sendMessage(context.getSender(), "command-translate-spawner-format");
        }

        if (!spawnerFormat.contains("{}")) {
            localeManager.sendMessage(context.getSender(), "command-translate-spawner-format-invalid");
            return;
        }

        localeManager.sendMessage(context.getSender(), "command-translate-loading");

        String finalSpawnerFormat = spawnerFormat;
        localeManager.getMinecraftTranslationValues(locale, response -> {
            if (response.getResult() == LocaleManager.TranslationResponse.Result.FAILURE) {
                localeManager.sendMessage(context.getSender(), "command-translate-failure");
                return;
            }

            if (response.getResult() == LocaleManager.TranslationResponse.Result.INVALID_LOCALE) {
                localeManager.sendMessage(context.getSender(), "command-translate-invalid-locale");
                return;
            }

            File blockSettingsFile = stackSettingManager.getBlockSettingsFile();
            File entitySettingsFile = stackSettingManager.getEntitySettingsFile();
            File itemSettingsFile = stackSettingManager.getItemSettingsFile();
            File spawnerSettingsFile = stackSettingManager.getSpawnerSettingsFile();

            CommentedFileConfiguration blockStackConfig = CommentedFileConfiguration.loadConfiguration(blockSettingsFile);
            CommentedFileConfiguration entityStackConfig = CommentedFileConfiguration.loadConfiguration(entitySettingsFile);
            CommentedFileConfiguration itemStackConfig = CommentedFileConfiguration.loadConfiguration(itemSettingsFile);
            CommentedFileConfiguration spawnerStackConfig = CommentedFileConfiguration.loadConfiguration(spawnerSettingsFile);

            Map<Material, String> materialValues = response.getMaterialValues();
            Map<EntityType, String> entityValues = response.getEntityValues();

            for (Map.Entry<Material, String> entry : materialValues.entrySet()) {
                Material material = entry.getKey();
                String value = entry.getValue();

                if (blockStackConfig.isConfigurationSection(material.name()))
                    blockStackConfig.set(material.name() + ".display-name", value);

                if (itemStackConfig.isConfigurationSection(material.name()))
                    itemStackConfig.set(material.name() + ".display-name", value);
            }

            for (Map.Entry<EntityType, String> entry : entityValues.entrySet()) {
                EntityType entityType = entry.getKey();
                String value = entry.getValue();

                if (entityStackConfig.isConfigurationSection(entityType.name()))
                    entityStackConfig.set(entityType.name() + ".display-name", value);

                if (spawnerStackConfig.isConfigurationSection(entityType.name())) {
                    String name = finalSpawnerFormat.replaceAll(Pattern.quote("{}"), value);
                    spawnerStackConfig.set(entityType.name() + ".display-name", name);
                }
            }

            blockStackConfig.save(blockSettingsFile, true);
            entityStackConfig.save(entitySettingsFile, true);
            itemStackConfig.save(itemSettingsFile, true);
            spawnerStackConfig.save(spawnerSettingsFile, true);

            ThreadUtils.runSync(() -> {
                this.rosePlugin.reload();
                localeManager.sendMessage(context.getSender(), "command-translate-success");
            });
        });
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("translate")
                .descriptionKey("command-translate-description")
                .permission("rosestacker.translate")
                .arguments(ArgumentsDefinition.builder()
                        .required("locale", StackerArgumentHandlers.TRANSLATION_LOCALE)
                        .optional("spawnerFormat", ArgumentHandlers.GREEDY_STRING)
                        .build())
                .build();
    }

}
