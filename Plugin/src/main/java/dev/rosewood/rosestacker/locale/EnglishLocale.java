package dev.rosewood.rosestacker.locale;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.locale.Locale;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConversionManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        return new LinkedHashMap<>() {{
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
            this.put("spawner-stack-display-single", "&7%name%");

            this.put("#1.1", "Hologram Display Tags");
            this.put("#1.2", "Available spawner placeholders: %name%, %amount%, %max_amount%, %time_remaining%, %ticks_remaining%, %total_spawned%");
            this.put("#1.3", "Multiple lines are supported");
            this.put("block-hologram-display", List.of("&c%amount%x &7%name%"));
            this.put("spawner-hologram-display", List.of("&c%amount%x &7%name%"));
            this.put("spawner-hologram-display-single", List.of("&7%name%"));

            this.put("#2", "Base Command Message");
            this.put("base-command-color", "&e");
            this.put("base-command-help", "&eUse &b/%cmd% help &efor command information.");

            this.put("#3", "Help Command");
            this.put("command-help-description", "Displays the help menu... You have arrived");
            this.put("command-help-title", "&eAvailable Commands:");
            this.put("command-help-list-description", "&8 - &d/%cmd% %subcmd% %args% &7- %desc%");
            this.put("command-help-list-description-no-args", "&8 - &d/%cmd% %subcmd% &7- %desc%");

            this.put("#4", "Reload Command");
            this.put("command-reload-description", "Reloads the plugin");
            this.put("command-reload-reloaded", "&ePlugin data, configuration, and locale files were reloaded.");

            this.put("#5", "Give Command");
            this.put("command-give-description", "Give pre-stacked items");
            this.put("command-give-usage", "&cUsage: &e/rs give <block|spawner|entity> <player> <type> [stackSize] [amount]");
            this.put("command-give-given", "&eGave &b%player% &e[%display%&e].");
            this.put("command-give-given-multiple", "&eGave &b%player% &e%amount%x [%display%&e].");
            this.put("command-give-unstackable", "&cThe type that you specified is not stackable.");
            this.put("command-give-too-large", "&cThe amount that you specified exceeds the max stack size for that type.");

            this.put("#6", "Clearall Command");
            this.put("command-clearall-description", "Clears all of a stack type");
            this.put("command-clearall-killed-entities", "&eCleared &b%amount% &eentity stacks.");
            this.put("command-clearall-killed-items", "&eCleared &b%amount% &eitem stacks.");
            this.put("command-clearall-killed-all", "&eCleared &b%entityAmount% &eentity stacks and &b%itemAmount% &eitem stacks.");

            this.put("#7", "Stats Command");
            this.put("command-stats-description", "Displays stats about the plugin");
            this.put("command-stats-header", "&aCurrent Plugin Stats:");
            this.put("command-stats-threads", "&b%amount% &eactive stacking threads.");
            this.put("command-stats-stacked-entities", "&b%stackAmount% &eloaded entity stacks, totaling &b%total% &eentities.");
            this.put("command-stats-stacked-items", "&b%stackAmount% &eloaded item stacks, totaling &b%total% &eitems.");
            this.put("command-stats-stacked-blocks", "&b%stackAmount% &eloaded block stacks, totaling &b%total% &eblocks.");
            this.put("command-stats-stacked-spawners", "&b%stackAmount% &eloaded spawner stacks, totaling &b%total% &espawners.");
            this.put("command-stats-active-tasks", "&b%amount% &eactive tasks.");

            this.put("#8", "Convert Command");
            this.put("command-convert-description", "Converts data from another stacking plugin");
            this.put("command-convert-converted", "&eConverted data from &b%plugin% &eto RoseStacker. The converted plugin has been disabled. Make sure to remove the converted plugin from your plugins folder.");
            this.put("command-convert-failed", "&cFailed to convert &b%plugin%&c, plugin is not enabled.");
            this.put("command-convert-aborted", "&cAborted attempting to convert &b%plugin%&c. You have already converted from another stacking plugin.");

            this.put("#9", "Translate Command");
            this.put("command-translate-description", "Translates the stack names");
            this.put("command-translate-loading", "&eDownloading and applying translation data, this may take a moment.");
            this.put("command-translate-failure", "&cUnable to translate the stack names. There was a problem fetching the locale data. Please try again later.");
            this.put("command-translate-invalid-locale", "&cUnable to translate the stack names. The locale that you specified is invalid.");
            this.put("command-translate-spawner-format", "&eSpawner names cannot be translated accurately. To fix this, you can use &b/rs translate en_us &3{} " +
                    "Spawner &eto make a spawner appear as \"Cow Spawner\". Use &b{} &eas the placeholder for the mob name.");
            this.put("command-translate-spawner-format-invalid", "&cThe spawner format you provided is invalid. It must contain &b{} &cfor the mob name placement.");
            this.put("command-translate-success", "&aSuccessfully translated the stack names.");

            this.put("#10", "Stacking Tool Command");
            this.put("command-stacktool-description", "Gives a player the stacking tool");
            this.put("command-stacktool-given", "&eYou have been given the stacking tool.");
            this.put("command-stacktool-given-other", "&b%player% &ahas been given the stacking tool.");
            this.put("command-stacktool-no-console", "&cYou cannot give the stacking tool to the console.");
            this.put("command-stacktool-no-permission", "&cYou do not have permission to use the stacking tool.");
            this.put("command-stacktool-invalid-entity", "&cThat entity is not part of a stack, is it a custom mob?");
            this.put("command-stacktool-marked-unstackable", "&eThe &b%type% &ehas been marked as &cunstackable&e.");
            this.put("command-stacktool-marked-stackable", "&eThe &b%type% &ehas been marked as &astackable&e.");
            this.put("command-stacktool-marked-all-unstackable", "&eThe entire &b%type% &estack has been marked as &cunstackable&e.");
            this.put("command-stacktool-select-1", "&eThe &b%type% &ehas been selected as Entity #1. Select another entity to test if they can stack.");
            this.put("command-stacktool-unselect-1", "&eThe &b%type% &ehas been unselected.");
            this.put("command-stacktool-select-2", "&eThe &b%type% &ehas been selected as Entity #2.");
            this.put("command-stacktool-can-stack", "&aEntity #1 can stack with Entity #2.");
            this.put("command-stacktool-can-not-stack", "&cEntity 1 can not stack with Entity 2. Reason: &b%reason%");
            this.put("command-stacktool-info", "&eStack Info:");
            this.put("command-stacktool-info-uuid", "&eUUID: &b%uuid%");
            this.put("command-stacktool-info-entity-id", "&eEntity ID: &b%id%");
            this.put("command-stacktool-info-custom-name", "&eCustom Name: &r%name%");
            this.put("command-stacktool-info-location", "&eLocation: X: &b%x% &eY: &b%y% &eZ: &b%z% &eWorld: &b%world%");
            this.put("command-stacktool-info-chunk", "&eChunk: &b%x%&e, &b%z%");
            this.put("command-stacktool-info-true", "&atrue");
            this.put("command-stacktool-info-false", "&cfalse");
            this.put("command-stacktool-info-entity-type", "&eEntity Type: &b%type%");
            this.put("command-stacktool-info-entity-stackable", "&eStackable: %value%");
            this.put("command-stacktool-info-entity-has-ai", "&eHas AI: %value%");
            this.put("command-stacktool-info-entity-from-spawner", "&eFrom Spawner: %value%");
            this.put("command-stacktool-info-item-type", "&eItem Type: &b%type%");
            this.put("command-stacktool-info-block-type", "&eBlock Type: &b%type%");
            this.put("command-stacktool-info-spawner-type", "&eSpawner Type: &b%type%");
            this.put("command-stacktool-info-stack-size", "&eStack Size: &b%amount%");

            this.put("#11", "Stacked Block GUI");
            this.put("gui-stacked-block-title", "Editing %name% Stack");
            this.put("gui-stacked-block-page-back", List.of("&ePrevious Page (" + GuiUtil.PREVIOUS_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-page-forward", List.of("&eNext Page (" + GuiUtil.NEXT_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-destroy", List.of("&cDestroy Stack", "&eDestroys the stack and drops the items"));
            this.put("gui-stacked-block-destroy-title", "Destroy Block Stack?");
            this.put("gui-stacked-block-destroy-confirm", List.of("&aConfirm", "&eYes, destroy the stack"));
            this.put("gui-stacked-block-destroy-cancel", List.of("&cCancel", "&eNo, go back to previous screen"));

            this.put("#12", "Stacked Spawner GUI");
            this.put("gui-stacked-spawner-title", "Viewing %name%");
            this.put("gui-stacked-spawner-stats", "&6Spawner Stats");
            this.put("gui-stacked-spawner-min-spawn-delay", "&eMin Spawn Delay: &b%delay%");
            this.put("gui-stacked-spawner-max-spawn-delay", "&eMax Spawn Delay: &b%delay%");
            this.put("gui-stacked-spawner-disabled-mob-ai", "&eDisabled Mob AI: &b%disabled%");
            this.put("gui-stacked-spawner-entity-search-range", "&eEntity Search Range: &b%range%");
            this.put("gui-stacked-spawner-player-activation-range", "&ePlayer Activation Range: &b%range%");
            this.put("gui-stacked-spawner-spawn-range", "&eSpawn Range: &b%range%");
            this.put("gui-stacked-spawner-min-spawn-amount", "&eMin Spawn Amount: &b%amount%");
            this.put("gui-stacked-spawner-max-spawn-amount", "&eMax Spawn Amount: &b%amount%");
            this.put("gui-stacked-spawner-spawn-amount", "&eSpawn Amount: &b%amount%");
            this.put("gui-stacked-spawner-spawn-conditions", "&6Spawn Conditions");
            this.put("gui-stacked-spawner-time-until-next-spawn", "&eTime until next spawn: &b%time% ticks");
            this.put("gui-stacked-spawner-total-spawns", "&eTotal mobs spawned: &b%amount%");
            this.put("gui-stacked-spawner-valid-spawn-conditions", "&6Valid Spawn Conditions");
            this.put("gui-stacked-spawner-invalid-spawn-conditions", "&6Invalid Spawn Conditions");
            this.put("gui-stacked-spawner-entities-can-spawn", "&aEntities are able to spawn");
            this.put("gui-stacked-spawner-conditions-preventing-spawns", "&eConditions preventing spawns:");

            this.put("#13", "Spawn Condition Messages");
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
            this.put("spawner-condition-total-darkness-info", "Total Darkness");
            this.put("spawner-condition-total-darkness-invalid", "Area must have no light");
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
            this.put("spawner-condition-not-player-placed-invalid", "Must be placed by a player");

            this.put("#14", "Given Stack Item Lore");
            this.put("#15", "Note: This will appear in the lore of the items given from the '/rs give' command");
            this.put("stack-item-lore-spawner", new ArrayList<>());
            this.put("stack-item-lore-block", new ArrayList<>());
            this.put("stack-item-lore-entity", new ArrayList<>());

            this.put("#16", "Generic Command Messages");
            this.put("no-permission", "&cYou don't have permission for that!");
            this.put("only-player", "&cThis command can only be executed by a player.");
            this.put("unknown-command", "&cUnknown command, use &b/%cmd% help &cfor more info.");
            this.put("unknown-command-error", "&cAn unknown error occurred; details have been printed to console. Please contact a server administrator.");
            this.put("invalid-subcommand", "&cInvalid subcommand.");
            this.put("invalid-argument", "&cInvalid argument: %message%.");
            this.put("invalid-argument-null", "&cInvalid argument: %name% was null.");
            this.put("missing-arguments", "&cMissing arguments, &b%amount% &crequired.");
            this.put("missing-arguments-extra", "&cMissing arguments, &b%amount%+ &crequired.");

            this.put("#17", "Argument Handler Error Messages");
            this.put("argument-handler-enum", "%enum% type [%input%] does not exist");
            this.put("argument-handler-enum-list", "%enum% type [%input%] does not exist. Valid types: %types%");
            this.put("argument-handler-string", "String cannot be empty");
            this.put("argument-handler-integer", "Integer [%input%] must be a whole number between -2^31 and 2^31-1 inclusively");
            this.put("argument-handler-player", "No Player with the username [%input%] was found online");
            this.put("argument-handler-stackplugin", "No plugin with the name [%input%] was found");
            this.put("argument-handler-material", "No material with the name [%input%] was found");
            this.put("argument-handler-stackamount", "The stack size [%input%] is invalid, must be a number greater than 0");
            this.put("argument-handler-stacktype", "The stack type [%input%] is invalid");
            this.put("argument-handler-translationlocale", "The translation locale [%input%] is invalid");

            this.put("#18", "Convert Lock Messages");
            this.put("convert-lock-conflictions", "&cThere are plugins on your server that are known to conflict with RoseStacker. " +
                    "In order to prevent conflictions and/or data loss, RoseStacker has disabled one or more stack types. " +
                    "A file has been created at plugins/" + RoseStacker.getInstance().getName() + "/" + ConversionManager.FILE_NAME + " where you can configure the disabled stack types. " +
                    "That file will also allow you to acknowledge that you have read this warning and let you to disable this message.");

            this.put("#19", "Misc Messages");
            this.put("spawner-advanced-place-range","&cWarning! You cannot place a spawner here!");
            this.put("spawner-silk-touch-protect", "&cWarning! &eYou need to use a silk touch pickaxe and/or have the permission to pick up spawners. You will be unable to do so otherwise.");
            this.put("spawner-advanced-place-no-permission", "&cWarning! &eYou do not have permission to place that type of spawner.");
            this.put("spawner-advanced-break-no-permission", "&cWarning! &eYou do not have permission to pick up that type of spawner.");
            this.put("spawner-advanced-break-silktouch-no-permission", "&cWarning! &eYou need to use a silk touch pickaxe to pick up that type of spawner.");
            this.put("spawner-convert-not-enough", "&cWarning! &eUnable to convert spawners using spawn eggs. You are not holding enough spawn eggs to do this conversion.");
            this.put("number-separator", ",");
            this.put("silktouch-chance-placeholder", "%chance%%");
        }};
    }
}
