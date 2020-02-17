package dev.esophose.sparkstacker.locale;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnglishLocale implements Locale {

    @Override
    public String getLocaleName() {
        return "en_US";
    }

    @Override
    public String getTranslatorName() {
        return "Esophose";
    }

    @Override
    public Map<String, String> getDefaultLocaleStrings() {
        return new LinkedHashMap<String, String>() {{
            this.put("#0", "Plugin Message Prefix");
            this.put("prefix", "&7[&bSparkStacker&7] ");

            this.put("#1", "Stack Display Tags");
            this.put("entity-stack-display", "&b%amount%x &7%name%");
            this.put("entity-stack-display-custom-name", "%name% &7[&b%amount%x&7]");
            this.put("item-stack-display", "&b%amount%x &7%name%");
            this.put("block-stack-display", "&b%amount%x &7%name%");
            this.put("spawner-stack-display", "&b%amount%x &7%name%");

            this.put("#2", "Reload Command");
            this.put("command-reload-description", "&8 - &d/ss reload &7- Reloads the plugin");
            this.put("command-reload-reloaded", "&ePlugin data, configuration, and locale files were reloaded.");

            this.put("#3", "Give Command");
            this.put("command-give-description", "&8 - &d/ss give &7- Give pre-stacked items");
            this.put("command-give-usage", "&cUsage: &e/ss give <block|spawner|entity> <player> <type> <amount>");
            this.put("command-give-given", "&eGave &b%player% &e[%display%&e].");

            this.put("#4", "Clearall Command");
            this.put("command-clearall-description", "&8 - &d/ss clearall &7- Clears all of a stack type");
            this.put("command-clearall-usage", "&cUsage: &e/ss clearall <entity|item>");
            this.put("command-clearall-killed-entities", "&eCleared &b%amount% &eentities.");
            this.put("command-clearall-killed-items", "&eCleared &b%amount% &eitems.");

            this.put("#5", "Convert Command");
            this.put("command-convert-description", "&8 - &d/ss convert &7- Converts data from another stacking plugin");
            this.put("command-convert-usage", "&cUsage: &e/ss convert <plugin>");
            this.put("command-convert-converted", "&eConverted data from &b%plugin% &eto SparkStacker. The converted plugin has been disabled. Make sure to remove the converted plugin from your plugins folder.");
            this.put("command-convert-failed", "&cFailed to convert &b%plugin%&c, plugin is not enabled.");

            this.put("#6", "ACF-Core Messages");
            this.put("acf-core-permission-denied", "&cYou don't have permission for that!");
            this.put("acf-core-permission-denied-parameter", "&cYou don't have permission for that!");
            this.put("acf-core-error-generic-logged", "&cAn error occurred. Please report to the plugin author.");
            this.put("acf-core-unknown-command", "&cUnknown command. Use &b/ss&c for commands.");
            this.put("acf-core-invalid-syntax", "&cUsage: &e{command}&7 {syntax}");
            this.put("acf-core-error-prefix", "&cError: {message}");
            this.put("acf-core-info-message", "&e{message}");
            this.put("acf-core-please-specify-one-of", "&cError: An invalid argument was given.");
            this.put("acf-core-must-be-a-number", "&cError: &b{num}&c must be a number.");
            this.put("acf-core-must-be-min-length", "&cError: Must be at least &b{min}&c characters long.");
            this.put("acf-core-must-be-max-length", "&cError: Must be at most &b{max}&c characters long.");
            this.put("acf-core-please-specify-at-most", "&cError: Please specify a value of at most &b{max}&c.");
            this.put("acf-core-please-specify-at-least", "&cError: Please specify a value of at least &b{min}&c.");
            this.put("acf-core-not-allowed-on-console", "&cOnly players may execute this command.");
            this.put("acf-core-could-not-find-player", "&cError: Could not find a player by the name: &b{search}");
            this.put("acf-core-no-command-matched-search", "&cError: No command matched &b{search}&c.");

            this.put("#7", "ACF-Minecraft Messages");
            this.put("acf-minecraft-no-player-found-server", "&cError: Could not find a player by the name: &b{search}");
        }};
    }
}
