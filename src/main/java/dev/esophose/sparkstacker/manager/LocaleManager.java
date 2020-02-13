package dev.esophose.sparkstacker.manager;

import dev.esophose.sparkstacker.SparkStacker;
import dev.esophose.sparkstacker.config.CommentedFileConfiguration;
import dev.esophose.sparkstacker.hook.PlaceholderAPIHook;
import dev.esophose.sparkstacker.locale.EnglishLocale;
import dev.esophose.sparkstacker.locale.Locale;
import dev.esophose.sparkstacker.manager.ConfigurationManager.Setting;
import dev.esophose.sparkstacker.utils.StringPlaceholders;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LocaleManager extends Manager {

    private CommentedFileConfiguration locale;

    public LocaleManager(SparkStacker sparkStacker) {
        super(sparkStacker);
    }

    /**
     * Creates a .lang file if one doesn't exist
     * Cross merges values between files into the .lang file, the .lang values take priority
     *
     * @param locale The Locale to register
     */
    private void registerLocale(Locale locale) {
        File file = new File(this.sparkStacker.getDataFolder() + "/locale", locale.getLocaleName() + ".lang");
        boolean newFile = false;
        if (!file.exists()) {
            try {
                file.createNewFile();
                newFile = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        boolean changed = false;
        CommentedFileConfiguration configuration = CommentedFileConfiguration.loadConfiguration(this.sparkStacker, file);
        if (newFile) {
            configuration.addComments(locale.getLocaleName() + " translation by " + locale.getTranslatorName());
            Map<String, String> defaultLocaleStrings = locale.getDefaultLocaleStrings();
            for (String key : defaultLocaleStrings.keySet()) {
                String value = defaultLocaleStrings.get(key);
                if (key.startsWith("#")) {
                    configuration.addComments(value);
                } else {
                    configuration.set(key, value);
                }
            }
            changed = true;
        } else {
            Map<String, String> defaultLocaleStrings = locale.getDefaultLocaleStrings();
            for (String key : defaultLocaleStrings.keySet()) {
                if (key.startsWith("#"))
                    continue;

                String value = defaultLocaleStrings.get(key);
                if (!configuration.contains(key)) {
                    configuration.set(key, value);
                    changed = true;
                }
            }
        }

        if (changed)
            configuration.save();
    }

    @Override
    public void reload() {
        File localeDirectory = new File(this.sparkStacker.getDataFolder(), "locale");
        if (!localeDirectory.exists())
            localeDirectory.mkdirs();

        this.registerLocale(new EnglishLocale());

        File targetLocaleFile = new File(this.sparkStacker.getDataFolder() + "/locale", Setting.LOCALE.getString() + ".lang");
        if (!targetLocaleFile.exists()) {
            targetLocaleFile = new File(this.sparkStacker.getDataFolder() + "/locale", "en_US.lang");
            this.sparkStacker.getLogger().severe("File " + targetLocaleFile.getName() + " does not exist. Defaulting to en_US.lang");
        }

        this.locale = CommentedFileConfiguration.loadConfiguration(this.sparkStacker, targetLocaleFile);
    }

    @Override
    public void disable() {

    }

    /**
     * @return a map of acf-core messages and their values
     */
    public Map<String, String> getAcfCoreMessages() {
        return this.locale.getKeys(false).stream()
                .filter(x -> x.startsWith("acf-core"))
                .collect(Collectors.toMap(x -> x.replaceFirst("acf-core-", "").replaceAll("-", "_"), x -> this.locale.getString(x)));
    }

    /**
     * @return a map of acf-core minecraft messages and their values
     */
    public Map<String, String> getAcfMinecraftMessages() {
        return this.locale.getKeys(false).stream()
                .filter(x -> x.startsWith("acf-minecraft"))
                .collect(Collectors.toMap(x -> x.replaceFirst("acf-minecraft-", "").replaceAll("-", "_"), x -> this.locale.getString(x)));
    }

    /**
     * Gets a locale message
     *
     * @param messageKey The key of the message to get
     * @return The locale message
     */
    public String getLocaleMessage(String messageKey) {
        return this.getLocaleMessage(messageKey, StringPlaceholders.empty());
    }

    /**
     * Gets a locale message with the given placeholders applied
     *
     * @param messageKey The key of the message to get
     * @param stringPlaceholders The placeholders to apply
     * @return The locale message with the given placeholders applied
     */
    public String getLocaleMessage(String messageKey, StringPlaceholders stringPlaceholders) {
        String message = this.locale.getString(messageKey);
        if (message == null)
            return ChatColor.RED + "Missing message in locale file: " + messageKey;
        return ChatColor.translateAlternateColorCodes('&', stringPlaceholders.apply(message));
    }

    /**
     * Sends a message to a CommandSender with the prefix with placeholders applied
     *
     * @param sender The CommandSender to send to
     * @param messageKey The message key of the Locale to send
     * @param stringPlaceholders The placeholders to apply
     */
    public void sendMessage(CommandSender sender, String messageKey, StringPlaceholders stringPlaceholders) {
        sender.sendMessage(this.parsePlaceholders(sender, this.getLocaleMessage("prefix") + this.getLocaleMessage(messageKey, stringPlaceholders)));
    }

    /**
     * Sends a message to a CommandSender with the prefix
     *
     * @param sender The CommandSender to send to
     * @param messageKey The message key of the Locale to send
     */
    public void sendMessage(CommandSender sender, String messageKey) {
        this.sendMessage(sender, messageKey, StringPlaceholders.empty());
    }

    /**
     * Sends a message to a CommandSender with placeholders applied
     *
     * @param sender The CommandSender to send to
     * @param messageKey The message key of the Locale to send
     * @param stringPlaceholders The placeholders to apply
     */
    public void sendSimpleMessage(CommandSender sender, String messageKey, StringPlaceholders stringPlaceholders) {
        sender.sendMessage(this.parsePlaceholders(sender, this.getLocaleMessage(messageKey, stringPlaceholders)));
    }

    /**
     * Sends a message to a CommandSender
     *
     * @param sender The CommandSender to send to
     * @param messageKey The message key of the Locale to send
     */
    public void sendSimpleMessage(CommandSender sender, String messageKey) {
        this.sendSimpleMessage(sender, messageKey, StringPlaceholders.empty());
    }

    /**
     * Sends a custom message to a CommandSender
     *
     * @param sender The CommandSender to send to
     * @param message The message to send
     */
    public void sendCustomMessage(CommandSender sender, String message) {
        sender.sendMessage(this.parsePlaceholders(sender, ChatColor.translateAlternateColorCodes('&', message)));
    }

    /**
     * Replaces PlaceholderAPI placeholders if PlaceholderAPI is enabled
     *
     * @param sender The potential Player to replace with
     * @param message The message
     * @return A placeholder-replaced message
     */
    private String parsePlaceholders(CommandSender sender, String message) {
        if (sender instanceof Player)
            return PlaceholderAPIHook.applyPlaceholders((Player) sender, message);
        return message;
    }

}
