package dev.rosewood.rosestacker.locale;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.locale.Locale;
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
            this.put("command-give-usage", "&cUsage: &e/rs give <block|spawner|entity> <player> <type> <stackSize> [amount]");
            this.put("command-give-given", "&eGave &b%player% &e[%display%&e].");
            this.put("command-give-given-multiple", "&eGave &b%player% &e%amount%x [%display%&e].");
            this.put("command-give-unstackable", "&cThe type that you specified is not stackable.");
            this.put("command-give-too-large", "&cThe amount that you specified exceeds the max stack size for that type.");

            this.put("#6", "Clearall Command");
            this.put("command-clearall-description", "&8 - &d/rs clearall &7- Clears all of a stack type");
            this.put("command-clearall-killed-entities", "&eCleared &b%amount% &eentity stacks.");
            this.put("command-clearall-killed-items", "&eCleared &b%amount% &eitem stacks.");
            this.put("command-clearall-killed-all", "&eCleared &b%entityAmount% &eentity stacks and &b%itemAmount% &eitem stacks.");

            this.put("#7", "Stats Command");
            this.put("command-stats-description", "&8 - &d/rs stats &7- Displays stats about the plugin");
            this.put("command-stats-header", "&aCurrent Plugin Stats:");
            this.put("command-stats-threads", "&b%amount% &eactive stacking threads.");
            this.put("command-stats-stacked-entities", "&b%stackAmount% &eloaded entity stacks, totaling &b%total% &eentities.");
            this.put("command-stats-stacked-items", "&b%stackAmount% &eloaded item stacks, totaling &b%total% &eitems.");
            this.put("command-stats-stacked-blocks", "&b%stackAmount% &eloaded block stacks, totaling &b%total% &eblocks.");
            this.put("command-stats-stacked-spawners", "&b%stackAmount% &eloaded spawner stacks, totaling &b%total% &espawners.");

            this.put("#8", "Convert Command");
            this.put("command-convert-description", "&8 - &d/rs convert &7- Converts data from another stacking plugin");
            this.put("command-convert-converted", "&eConverted data from &b%plugin% &eto RoseStacker. The converted plugin has been disabled. Make sure to remove the converted plugin from your plugins folder.");
            this.put("command-convert-failed", "&cFailed to convert &b%plugin%&c, plugin is not enabled.");
            this.put("command-convert-aborted", "&cAborted attempting to convert &b%plugin%&c. You have already converted from another stacking plugin.");

            this.put("#9", "Purge Data Command");
            this.put("command-purgedata-description", "&8 - &d/rs purgedata &7- Deletes stack data from a world");
            this.put("command-purgedata-none", "&eNo stack data was found with the given world name.");
            this.put("command-purgedata-purged", "&ePurged &b%amount% &estack data entries from the database.");

            this.put("#10", "Query Data Command");
            this.put("command-querydata-description", "&8 - &d/rs querydata &7- Gets saved stack data info");
            this.put("command-querydata-none", "&eNo data was found with the given world name.");
            this.put("command-querydata-header", "&aQueried Data Results:");
            this.put("command-querydata-entity", "&eSaved entity stacks: &b%amount%");
            this.put("command-querydata-item", "&eSaved item stacks: &b%amount%");
            this.put("command-querydata-block", "&eSaved block stacks: &b%amount%");
            this.put("command-querydata-spawner", "&eSaved spawner stacks: &b%amount%");

            this.put("#11", "Translate Command");
            this.put("command-translate-description", "&8 - &d/rs translate &7- Translates the stack names");
            this.put("command-translate-loading", "&eDownloading and applying translation data, this may take a moment.");
            this.put("command-translate-failure", "&cUnable to translate the stack names. There was a problem fetching the locale data. Please try again later.");
            this.put("command-translate-invalid-locale", "&cUnable to translate the stack names. The locale that you specified is invalid.");
            this.put("command-translate-spawner-format", "&eSpawner names cannot be translated accurately. To fix this, you can use &b/rs translate en_us &3{} " +
                    "Spawner &eto make a spawner appear as \"Cow Spawner\". Use &b{} &eas the placeholder for the mob name.");
            this.put("command-translate-spawner-format-invalid", "&cThe spawner format you provided is invalid. It must contain &b{} &cfor the mob name placement.");
            this.put("command-translate-success", "&aSuccessfully translated the stack names.");

            this.put("#12", "Stacking Tool Command");
            this.put("command-stacktool-description", "&8 - &d/rs stacktool &7- Gives a player the stacking tool");
            this.put("command-stacktool-given", "&eYou have been given the stacking tool.");
            this.put("command-stacktool-given-other", "&b%player% &ahas been given the stacking tool.");
            this.put("command-stacktool-marked-unstackable", "&eThe &b%type% &ehas been marked as &cunstackable&e.");
            this.put("command-stacktool-marked-stackable", "&eThe &b%type% &ehas been marked as &astackable&e.");
            this.put("command-stacktool-marked-all-unstackable", "&eThe entire &b%type% &estack has been marked as &cunstackable&e.");
            this.put("command-stacktool-select-1", "&eThe &b%type% &ehas been selected as entity #1. Select another entity to see if they can stack.");
            this.put("command-stacktool-select-2", "&eThe &b%type% &ehas been selected as entity #2.");


            this.put("#13", "Stacked Block GUI");
            this.put("gui-stacked-block-title", "Editing %name% Stack");
            this.put("gui-stacked-block-page-back", Collections.singletonList("&ePrevious Page (" + GuiUtil.PREVIOUS_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-page-forward", Collections.singletonList("&eNext Page (" + GuiUtil.NEXT_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-destroy", Arrays.asList("&cDestroy Stack", "&eDestroys the stack and drops the items"));
            this.put("gui-stacked-block-destroy-title", "Destroy Block Stack?");
            this.put("gui-stacked-block-destroy-confirm", Arrays.asList("&aConfirm", "&eYes, destroy the stack"));
            this.put("gui-stacked-block-destroy-cancel", Arrays.asList("&cCancel", "&eNo, go back to previous screen"));

            this.put("#14", "Stacked Spawner GUI");
            this.put("gui-stacked-spawner-title", "Viewing %name%");
            this.put("gui-stacked-spawner-stats", "&6Spawner Stats");
            this.put("gui-stacked-spawner-min-spawn-delay", "&eMin Spawn Delay: &b%delay%");
            this.put("gui-stacked-spawner-max-spawn-delay", "&eMax Spawn Delay: &b%delay%");
            this.put("gui-stacked-spawner-disabled-mob-ai", "&eDisabled Mob AI: &b%disabled%");
            this.put("gui-stacked-spawner-max-nearby-entities", "&eMax Nearby Entities: &b%max%");
            this.put("gui-stacked-spawner-player-activation-range", "&ePlayer Activation Range: &b%range%");
            this.put("gui-stacked-spawner-spawn-range", "&eSpawn Range: &b%range%");
            this.put("gui-stacked-spawner-min-spawn-amount", "&eMin Spawn Amount: &b%amount%");
            this.put("gui-stacked-spawner-max-spawn-amount", "&eMax Spawn Amount: &b%amount%");
            this.put("gui-stacked-spawner-spawn-amount", "&eSpawn Amount: &b%amount%");
            this.put("gui-stacked-spawner-spawn-conditions", "&6Spawn Conditions");
            this.put("gui-stacked-spawner-time-until-next-spawn", "&eTime until next spawn: &b%time% ticks");
            this.put("gui-stacked-spawner-valid-spawn-conditions", "&6Valid Spawn Conditions");
            this.put("gui-stacked-spawner-invalid-spawn-conditions", "&6Invalid Spawn Conditions");
            this.put("gui-stacked-spawner-entities-can-spawn", "&aEntities are able to spawn");
            this.put("gui-stacked-spawner-conditions-preventing-spawns", "&eConditions preventing spawns:");

            this.put("#15", "Spawn Condition Messages");
            this.put("spawner-condition-invalid", "&7 - &c%message%");
            this.put("spawner-condition-info", "&e%condition%");
            this.put("spawner-condition-single", "&e%condition%: &b%value%");
            this.put("spawner-condition-list", "&e%condition%:");
            this.put("spawner-condition-list-item", "&7 - &b%message%");
            this.put("spawner-condition-above-sea-level-info", "Above Sea Level");
            this.put("spawner-condition-above-sea-level-invalid", "No spawn area above sea level");
            this.put("spawner-condition-above-y-axis-info", "Above Y-Axis");
            this.put("spawner-condition-above-y-axis-invalid", "No spawn area above required Y-Axis");
            this.put("spawner-condition-air-info", "Open Air");
            this.put("spawner-condition-air-invalid", "No large enough air spaces available");
            this.put("spawner-condition-below-sea-level-info", "Below Sea Level");
            this.put("spawner-condition-below-sea-level-invalid", "No spawn area below sea level");
            this.put("spawner-condition-below-y-axis-info", "Below Y-Axis");
            this.put("spawner-condition-below-y-axis-invalid", "No spawn area below required Y-Axis");
            this.put("spawner-condition-biome-info", "Biome");
            this.put("spawner-condition-biome-invalid", "Incorrect biome");
            this.put("spawner-condition-block-info", "Spawn Block");
            this.put("spawner-condition-block-invalid", "No valid spawn blocks");
            this.put("spawner-condition-block-exception-info", "Spawn Block Exception");
            this.put("spawner-condition-block-exception-invalid", "Excluded spawn blocks");
            this.put("spawner-condition-darkness-info", "Low Light Level");
            this.put("spawner-condition-darkness-invalid", "Area is too bright");
            this.put("spawner-condition-fluid-info", "Requires Fluid");
            this.put("spawner-condition-fluid-invalid", "No nearby fluid");
            this.put("spawner-condition-lightness-info", "High Light Level");
            this.put("spawner-condition-lightness-invalid", "Area is too dark");
            this.put("spawner-condition-max-nearby-entities-info", "Max Nearby Entities");
            this.put("spawner-condition-max-nearby-entities-invalid", "Too many nearby entities");
            this.put("spawner-condition-no-skylight-access-info", "No Skylight Access");
            this.put("spawner-condition-no-skylight-access-invalid", "No spawn blocks without skylight access");
            this.put("spawner-condition-on-ground-info", "On Ground");
            this.put("spawner-condition-on-ground-invalid", "No solid ground nearby");
            this.put("spawner-condition-skylight-access-info", "Skylight Access");
            this.put("spawner-condition-skylight-access-invalid", "No spawn blocks with skylight access");
            this.put("spawner-condition-none-invalid", "Exceeded maximum spawn attempts");

            this.put("#16", "Given Stack Item Lore");
            this.put("#17", "Note: Changing these will invalidate items with the old lore");
            this.put("stack-item-lore-stack-size", "&7Stack Size: &c");
            this.put("stack-item-lore-entity-type", "&7Entity Type: &c");
            this.put("stack-item-lore-block-type", "&7Block Type: &c");
            this.put("stack-item-lore-spawner-type", "&7Spawner Type: &c");

            this.put("#18", "ACF-Core Messages");
            this.put("acf-core-permission-denied", "&cYou don't have permission for that!");
            this.put("acf-core-permission-denied-parameter", "&cYou don't have permission for that!");
            this.put("acf-core-error-generic-logged", "&cAn error occurred. Please report to the plugin author.");
            this.put("acf-core-error-performing-command", "&cAn error occurred executing the command.");
            this.put("acf-core-unknown-command", "&cUnknown command. Use &b/rs&c for commands.");
            this.put("acf-core-invalid-syntax", "&cUsage: &e{command}&e {syntax}");
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

            this.put("#19", "ACF-Minecraft Messages");
            this.put("acf-minecraft-no-player-found-server", "&cError: Could not find a player by the name: &b{search}");
            this.put("acf-minecraft-is-not-a-valid-name", "&cError: &b{name} &cis not a valid player name.");

            this.put("#20", "Convert Lock Messages");
            this.put("convert-lock-conflictions", "&cThere are plugins on your server that are known to conflict with RoseStacker. " +
                    "In order to prevent conflictions and/or data loss, RoseStacker has disabled one or more stack types. " +
                    "A file has been created at plugins/" + RoseStacker.getInstance().getName() + "/" + ConversionManager.FILE_NAME + " where you can configure the disabled stack types. " +
                    "That file will also allow you to acknowledge that you have read this warning and let you to disable this message.");
        }};
    }
}
