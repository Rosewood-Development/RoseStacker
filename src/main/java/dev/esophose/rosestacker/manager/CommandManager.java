package dev.esophose.rosestacker.manager;

import co.aikar.commands.PaperCommandManager;
import co.aikar.locales.MessageKey;
import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.command.RoseCommand;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandManager extends Manager {

    private boolean loaded;

    public CommandManager(RoseStacker roseStacker) {
        super(roseStacker);
        this.loaded = false;
    }

    @Override
    public void reload() {
        if (!this.loaded) {
            PaperCommandManager commandManager = new PaperCommandManager(this.roseStacker);
            commandManager.registerCommand(new RoseCommand(this.roseStacker));

            // Load custom message strings
            Map<String, String> acfCoreMessages = this.roseStacker.getLocaleManager().getAcfCoreMessages();
            for (String key : acfCoreMessages.keySet())
                commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKey.of("acf-core." + key), LocaleManager.Locale.PREFIX.get() + acfCoreMessages.get(key));

            commandManager.getCommandCompletions().registerAsyncCompletion("amount", (ctx) -> Arrays.asList("5", "16", "64", "256", "<amount>"));
            commandManager.getCommandCompletions().registerAsyncCompletion("stackableBlockMaterial", (ctx) -> this.roseStacker.getStackSettingManager().getStackableBlockTypes().stream().map(x -> x.name().toLowerCase()).collect(Collectors.toSet()));
            commandManager.getCommandCompletions().registerAsyncCompletion("spawnableEntityType", (ctx) -> StackerUtils.getStackableEntityTypes().stream().map(x -> x.name().toLowerCase()).collect(Collectors.toSet()));

            this.loaded = true;
        }
    }

    @Override
    public void disable() {

    }

    private String parse(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}
