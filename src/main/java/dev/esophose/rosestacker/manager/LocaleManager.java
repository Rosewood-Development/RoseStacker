package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.utils.StringPlaceholders;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocaleManager extends Manager {

    public enum Locale {
        PREFIX("&7[&cRoseStacker&7] "),

        ENTITY_STACK_DISPLAY("&c%amount%x &7%name%"),
        ITEM_STACK_DISPLAY("&c%amount%x &7%name%"),
        BLOCK_STACK_DISPLAY("&c%amount%x &7%name%"),
        SPAWNER_STACK_DISPLAY("&c%amount%x &7%name%"),

        COMMAND_RELOAD_DESCRIPTION("&8 - &d/rs reload &7- Reloads the plugin"),
        COMMAND_RELOAD_RELOADED("&eConfiguration and locale files were reloaded."),

        COMMAND_GIVE_DESCRIPTION("&8 - &d/rs give &7- Give pre-stacked items"),
        COMMAND_GIVE_USAGE("&cUsage: &e/rs give <block|spawner|entity> <player> <type> <amount>"),
        COMMAND_GIVE_GIVEN("&eGave &b%player% &e[%display%&e]."),

        COMMAND_CLEARALL_DESCRIPTION("&8 - &d/rs clearall &7- Clears all of a stack type"),
        COMMAND_CLEARALL_USAGE("&cUsage: &e/rs clearall <entity|item>"),
        COMMAND_CLEARALL_KILLED_ENTITIES("&eCleared &b%amount% &eentities."),
        COMMAND_CLEARALL_KILLED_ITEMS("&eCleared &b%amount% &eitems."),

        COMMAND_CONVERT_DESCRIPTION("&8 - &d/rs convert &7- Converts data from another stacking plugin"),
        COMMAND_CONVERT_USAGE("&cUsage: &e/rs convert <plugin>"),
        COMMAND_CONVERT_CONVERTED("&eConverted data from &b%plugin% &eto RoseStacker. The converted plugin has been disabled. Make sure to remove the converted plugin from your plugins folder."),
        COMMAND_CONVERT_FAILED("&cFailed to convert &b%plugin%&c, plugin is not enabled."),

        ACF_CORE_PERMISSION_DENIED("&cYou don't have permission for that!"),
        ACF_CORE_PERMISSION_DENIED_PARAMETER("&cYou don't have permission for that!"),
        ACF_CORE_ERROR_GENERIC_LOGGED("&cAn error occurred. Please report to the plugin author."),
        ACF_CORE_UNKNOWN_COMMAND("&cUnknown command. Use &b/rs&c for commands."),
        ACF_CORE_INVALID_SYNTAX("&cUsage: &e{command}&7 {syntax}"),
        ACF_CORE_ERROR_PREFIX("&cError: {message}"),
        ACF_CORE_INFO_MESSAGE("&e{message}"),
        ACF_CORE_PLEASE_SPECIFY_ONE_OF("&cError: An invalid argument was given."),
        ACF_CORE_MUST_BE_A_NUMBER("&cError: &b{num}&c must be a number."),
        ACF_CORE_MUST_BE_MIN_LENGTH("&cError: Must be at least &b{min}&c characters long."),
        ACF_CORE_MUST_BE_MAX_LENGTH("&cError: Must be at most &b{max}&c characters long."),
        ACF_CORE_PLEASE_SPECIFY_AT_MOST("&cError: Please specify a value of at most &b{max}&c."),
        ACF_CORE_PLEASE_SPECIFY_AT_LEAST("&cError: Please specify a value of at least &b{min}&c."),
        ACF_CORE_NOT_ALLOWED_ON_CONSOLE("&cOnly players may execute this command."),
        ACF_CORE_COULD_NOT_FIND_PLAYER("&cError: Could not find a player by the name: &b{search}"),
        ACF_CORE_NO_COMMAND_MATCHED_SEARCH("&cNo command matched &b{search}&c.");

        private final String defaultMessage;
        private String message;

        Locale(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        /**
         * Gets a Locale message
         *
         * @return A message formatted for chat
         */
        public String get() {
            if (this.message == null)
                this.loadMessage();
            return this.message;
        }

        /**
         * Loads the locale message and caches it
         */
        private void loadMessage() {
            String message = RoseStacker.getInstance().getLocaleManager().getLocale().getString(this.getNameAsKey());
            if (message != null)
                this.message = ChatColor.translateAlternateColorCodes('&', message);
        }

        /**
         * Resets the cached message
         */
        private void reset() {
            this.message = null;
        }

        /**
         * Gets the name of this Setting as a FileConfiguration-compatible key
         *
         * @return The key for a FileConfiguration
         */
        private String getNameAsKey() {
            return this.name().replace("_", "-").toLowerCase();
        }
    }

    public LocaleManager(RoseStacker roseStacker) {
        super(roseStacker);
    }

    private FileConfiguration locale;

    @Override
    public void reload() {
        for (Locale value : Locale.values())
            value.reset();

        String targetLocaleName = ConfigurationManager.Setting.LOCALE.getString() + ".lang";
        File targetLocaleFile = new File(this.roseStacker.getDataFolder() + "/locale", targetLocaleName);
        if (!targetLocaleFile.exists()) {
            targetLocaleFile = new File(this.roseStacker.getDataFolder() + "/locale", "en_US.lang");
            if (!targetLocaleFile.exists()) {
                try {
                    this.roseStacker.getDataFolder().mkdir();
                    new File(this.roseStacker.getDataFolder(), "locale").mkdir();
                    targetLocaleFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        this.locale = YamlConfiguration.loadConfiguration(targetLocaleFile);

        // Create defaults
        for (Locale locale : Locale.values())
            if (!this.locale.contains(locale.getNameAsKey()))
                this.locale.set(locale.getNameAsKey(), locale.defaultMessage);

        try {
            this.locale.save(targetLocaleFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disable() {

    }

    /**
     * Gets the FileConfiguration that contains the locale messages
     *
     * @return A FileConfiguration of the messages
     */
    public FileConfiguration getLocale() {
        return this.locale;
    }

    /**
     * @return a map of acf-core messages and their values
     */
    public Map<String, String> getAcfCoreMessages() {
        return Stream.of(Locale.values())
                .filter(x -> x.name().startsWith("ACF_CORE"))
                .collect(Collectors.toMap(x -> x.name().replaceFirst("ACF_CORE_", "").toLowerCase(), Locale::get));
    }

    /**
     * @return a map of acf-core minecraft and their values
     */
    public Map<String, String> getAcfMinecraftMessages() {
        return Stream.of(Locale.values())
                .filter(x -> x.name().startsWith("ACF_MINECRAFT"))
                .collect(Collectors.toMap(x -> x.name().replaceFirst("ACF_MINECRAFT_", "").toLowerCase(), Locale::get));
    }

    /**
     * Sends a message to a CommandSender with the prefix with placeholders applied
     *
     * @param sender The CommandSender to send to
     * @param locale The Locale to send
     * @param stringPlaceholders The placeholders to apply
     */
    public void sendPrefixedMessage(CommandSender sender, Locale locale, StringPlaceholders stringPlaceholders) {
        sender.sendMessage(Locale.PREFIX.get() + stringPlaceholders.apply(locale.get()));
    }

    /**
     * Sends a message to a CommandSender with the prefix
     *
     * @param sender The CommandSender to send to
     * @param locale The Locale to send
     */
    public void sendPrefixedMessage(CommandSender sender, Locale locale) {
        this.sendPrefixedMessage(sender, locale, new StringPlaceholders());
    }

    /**
     * Sends a message to a CommandSender with placeholders applied
     *
     * @param sender The CommandSender to send to
     * @param locale The Locale to send
     * @param stringPlaceholders The placeholders to apply
     */
    public void sendMessage(CommandSender sender, Locale locale, StringPlaceholders stringPlaceholders) {
        sender.sendMessage(stringPlaceholders.apply(locale.get()));
    }

    /**
     * Sends a message to a CommandSender
     *
     * @param sender The CommandSender to send to
     * @param locale The Locale to send
     */
    public void sendMessage(CommandSender sender, Locale locale) {
        this.sendMessage(sender, locale, new StringPlaceholders());
    }

}
