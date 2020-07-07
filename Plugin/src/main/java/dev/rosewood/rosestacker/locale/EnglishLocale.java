package dev.rosewood.rosestacker.locale;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConversionManager;
import java.util.Arrays;
import java.util.Collections;
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
    public Map<String, Object> getDefaultLocaleValues() {
        return new LinkedHashMap<String, Object>() {{
            this.put("#0", "Plugin Message Prefix");
            this.put("prefix", "&7[<g:#8A2387:#E94057:#F27121>RoseStacker&7] ");

            this.put("#1", "Stack Display Tags");
            this.put("entity-stack-display", "&c%amount%x &7%name%");
            this.put("entity-stack-display-custom-name", "%name% &7[&c%amount%x&7]");
            this.put("entity-stack-display-spawn-egg", "&c%amount%x &7%name% Spawn Egg");
            this.put("item-stack-display", "&c%amount%x &7%name%");
            this.put("item-stack-display-single", "&7%name%");
            this.put("block-stack-display", "&c%amount%x &7%name%");
            this.put("spawner-stack-display", "&c%amount%x &7%name%");

            this.put("#2", "Base Command Message");
            this.put("base-command-color", "&e");
            this.put("base-command-help", "&eUse &b/rs help &efor command information.");

            this.put("#3", "Help Command");
            this.put("command-help-description", "&8 - &d/rs help &7- Displays the help menu... You have arrived");
            this.put("command-help-title", "&eAvailable Commands:");

            this.put("#4", "Reload Command");
            this.put("command-reload-description", "&8 - &d/rs reload &7- Reloads the plugin");
            this.put("command-reload-reloaded", "&ePlugin data, configuration, and locale files were reloaded.");

            this.put("#5", "Give Command");
            this.put("command-give-description", "&8 - &d/rs give &7- Give pre-stacked items");
            this.put("command-give-usage", "&cUsage: &e/rs give <block|spawner|entity> <player> <type> <amount>");
            this.put("command-give-given", "&eGave &b%player% &e[%display%&e].");
            this.put("command-give-unstackable", "&cThe type that you specified is not stackable.");
            this.put("command-give-too-large", "&cThe amount that you specified exceeds the max stack size for that type.");

            this.put("#6", "Clearall Command");
            this.put("command-clearall-description", "&8 - &d/rs clearall &7- Clears all of a stack type");
            this.put("command-clearall-killed-entities", "&eCleared &b%amount% &eentities.");
            this.put("command-clearall-killed-items", "&eCleared &b%amount% &eitems.");

            this.put("#7", "Stats Command");
            this.put("command-stats-description", "&8 - &d/rs stats &7- Displays stats about the plugin");
            this.put("command-stats-header", "&aCurrent Plugin Stats:");
            this.put("command-stats-threads", "&eActive stacking threads: &b%amount%");
            this.put("command-stats-stacked-entities", "&eLoaded entity stacks: &b%amount%");
            this.put("command-stats-stacked-items", "&eLoaded item stacks: &b%amount%");
            this.put("command-stats-stacked-blocks", "&eLoaded block stacks: &b%amount%");
            this.put("command-stats-stacked-spawners", "&eLoaded spawner stacks: &b%amount%");

            this.put("#8", "Convert Command");
            this.put("command-convert-description", "&8 - &d/rs convert &7- Converts data from another stacking plugin");
            this.put("command-convert-converted", "&eConverted data from &b%plugin% &eto RoseStacker. The converted plugin has been disabled. Make sure to remove the converted plugin from your plugins folder.");
            this.put("command-convert-failed", "&cFailed to convert &b%plugin%&c, plugin is not enabled.");
            this.put("command-convert-aborted", "&cAborted attempting to convert &b%plugin%&c. You have already converted from another stacking plugin.");

            this.put("#9", "Purge Data Command");
            this.put("command-purgedata-description", "&8 - &d/rs purgedata [world] &7- Deletes stack data from a world");
            this.put("command-purgedata-none", "&eNo stack data was found with the given world name.");
            this.put("command-purgedata-purged", "&ePurged &b%amount% &estack data entries from the database.");

            this.put("#10", "Query Data Command");
            this.put("command-querydata-description", "&8 - &d/rs querydata [world] &7- Gets saved stack data info");
            this.put("command-querydata-none", "&eNo data was found with the given world name.");
            this.put("command-querydata-header", "&aQueried Data Results:");
            this.put("command-querydata-entity", "&eSaved entity stacks: &b%amount%");
            this.put("command-querydata-item", "&eSaved item stacks: &b%amount%");
            this.put("command-querydata-block", "&eSaved block stacks: &b%amount%");
            this.put("command-querydata-spawner", "&eSaved spawner stacks: &b%amount%");

            this.put("#11", "Stacked Block GUI");
            this.put("gui-stacked-block-title", "Editing %name% Stack");
            this.put("gui-stacked-block-page-back", Collections.singletonList("&ePrevious Page (" + GuiUtil.PREVIOUS_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-page-forward", Collections.singletonList("&eNext Page (" + GuiUtil.NEXT_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-destroy", Arrays.asList("&cDestroy Stack", "&eDestroys the stack and drops the items"));
            this.put("gui-stacked-block-destroy-title", "Destroy Block Stack?");
            this.put("gui-stacked-block-destroy-confirm", Arrays.asList("&aConfirm", "&eYes, destroy the stack"));
            this.put("gui-stacked-block-destroy-cancel", Arrays.asList("&cCancel", "&eNo, go back to previous screen"));

            this.put("#12", "ACF-Core Messages");
            this.put("acf-core-permission-denied", "&cYou don't have permission for that!");
            this.put("acf-core-permission-denied-parameter", "&cYou don't have permission for that!");
            this.put("acf-core-error-generic-logged", "&cAn error occurred. Please report to the plugin author.");
            this.put("acf-core-error-performing-command", "&cAn error occurred executing the command.");
            this.put("acf-core-unknown-command", "&cUnknown command. Use &b/rs&c for commands.");
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

            this.put("#13", "ACF-Minecraft Messages");
            this.put("acf-minecraft-no-player-found-server", "&cError: Could not find a player by the name: &b{search}");

            this.put("#14", "Convert Lock Messages");
            this.put("convert-lock-conflictions", "&cThere are plugins on your server that are known to conflict with RoseStacker. " +
                    "In order to prevent conflictions and/or data loss, RoseStacker has disabled one or more stack types. " +
                    "A file has been created at plugins/" + RoseStacker.getInstance().getName() + "/" + ConversionManager.FILE_NAME + " where you can configure the disabled stack types. " +
                    "That file will also allow you to acknowledge that you have read this warning and let you to disable this message.");
        }};
    }
}
