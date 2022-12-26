package dev.rosewood.rosestacker.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.Optional;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.command.framework.types.GreedyString;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.command.type.TranslationLocale;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import java.util.Map;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class TranslateCommand extends RoseCommand {

    public TranslateCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context, TranslationLocale locale, @Optional GreedyString spawnerFormat) {
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
        StackSettingManager stackSettingManager = this.rosePlugin.getManager(StackSettingManager.class);

        String format = spawnerFormat == null ? null : spawnerFormat.get();
        if (format == null) {
            format = "{}";
            localeManager.sendMessage(context.getSender(), "command-translate-spawner-format");
        }

        if (!format.contains("{}")) {
            localeManager.sendMessage(context.getSender(), "command-translate-spawner-format-invalid");
            return;
        }

        localeManager.sendMessage(context.getSender(), "command-translate-loading");

        String finalSpawnerFormat = format;
        localeManager.getMinecraftTranslationValues(locale.get(), response -> {
            if (response.getResult() == LocaleManager.TranslationResponse.Result.FAILURE) {
                localeManager.sendMessage(context.getSender(), "command-translate-failure");
                return;
            }

            if (response.getResult() == LocaleManager.TranslationResponse.Result.INVALID_LOCALE) {
                localeManager.sendMessage(context.getSender(), "command-translate-invalid-locale");
                return;
            }

            CommentedFileConfiguration blockStackConfig = CommentedFileConfiguration.loadConfiguration(stackSettingManager.getBlockSettingsFile());
            CommentedFileConfiguration entityStackConfig = CommentedFileConfiguration.loadConfiguration(stackSettingManager.getEntitySettingsFile());
            CommentedFileConfiguration itemStackConfig = CommentedFileConfiguration.loadConfiguration(stackSettingManager.getItemSettingsFile());
            CommentedFileConfiguration spawnerStackConfig = CommentedFileConfiguration.loadConfiguration(stackSettingManager.getSpawnerSettingsFile());

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

            blockStackConfig.save();
            entityStackConfig.save();
            itemStackConfig.save();
            spawnerStackConfig.save();

            ThreadUtils.runSync(() -> {
                this.rosePlugin.reload();
                localeManager.sendMessage(context.getSender(), "command-translate-success");
            });
        });
    }

    @Override
    protected String getDefaultName() {
        return "translate";
    }

    @Override
    public String getDescriptionKey() {
        return "command-translate-description";
    }

    @Override
    public String getRequiredPermission() {
        return "rosestacker.translate";
    }

}
