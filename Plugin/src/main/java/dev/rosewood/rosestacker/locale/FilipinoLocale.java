package dev.rosewood.rosestacker.locale;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.locale.Locale;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConversionManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FilipinoLocale implements Locale {

    @Override
    public String getLocaleName() {
        return "fil_PH";
    }

    @Override
    public String getTranslatorName() {
        return "WesTheKing";
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
            this.put("base-command-help", "&eUse &b/%cmd% help &epara sa impormasyon ng utos");

            this.put("#3", "Help Command");
            this.put("command-help-description", "Ipinapakita ang menu ng tulong... Nakarating kana");
            this.put("command-help-title", "&eMagagamit na mga Utos:");
            this.put("command-help-list-description", "&8 - &d/%cmd% %subcmd% %args% &7- %desc%");
            this.put("command-help-list-description-no-args", "&8 - &d/%cmd% %subcmd% &7- %desc%");

            this.put("#4", "Reload Command");
            this.put("command-reload-description", "I-reload ang plugin");
            this.put("command-reload-reloaded", "&eAng data ng plugin, pagsasaayos, at mga lokal na file ay na-reload.");

            this.put("#5", "Give Command");
            this.put("command-give-description", "Bigyan ang mga paunang naka-stack na item");
            this.put("command-give-usage", "&cPaggamit: &e/rs give <block|spawner|entity> <player> <type> [stackSize] [amount]");
            this.put("command-give-given", "&eNagbigay &b%player% &e[%display%&e].");
            this.put("command-give-given-multiple", "&eNagbigay &b%player% &e%amount%x [%display%&e].");
            this.put("command-give-unstackable", "&cAng uri na iyong tinukoy ay hindi mailalagay.");
            this.put("command-give-too-large", "&cAng halagang tinukoy mo ay lumampas sa laki ng max na stack para sa ganitong uri.");

            this.put("#6", "Clearall Command");
            this.put("command-clearall-description", "Tinatanggal ang lahat ng uri ng stack");
            this.put("command-clearall-killed-entities", "&eTinanggal ang &b%amount% na &estacked na nilalang");
            this.put("command-clearall-killed-items", "&eTinanggal ang &b%amount% &estacked na gamit.");
            this.put("command-clearall-killed-all", "&eTinanggal ang &b%entityAmount% &estack na nilalang at &b%itemAmount% &eStacked na gamit.");

            this.put("#7", "Stats Command");
            this.put("command-stats-description", "Nagpapakita ng mga istatistika tungkol sa plugin");
            this.put("command-stats-header", "&aMga Kasalukuyang Plugin Stats:");
            this.put("command-stats-threads", "&b%amount% &emga aktibong stacking thread.");
            this.put("command-stats-stacked-entities", "&b%stackAmount% &ena-load na entity stack, na kabuuan &b%total% &ena nilalang.");
            this.put("command-stats-stacked-items", "&b%stackAmount% &enaka-load na mga stack gamit, na kabuuan &b%total% &ena gamit.");
            this.put("command-stats-stacked-blocks", "&b%stackAmount% &ena-load ang mga block stack, na kabuuan &b%total% &emga bloke.");
            this.put("command-stats-stacked-spawners", "&b%stackAmount% &ena-load na mga stack ng spawner, na kabuuan &b%total% &ena spawners.");
            this.put("command-stats-active-tasks", "&b%amount% &eaktibong gawain.");

            this.put("#8", "Convert Command");
            this.put("command-convert-description", "Nagko-convert ng data mula sa isa pang stacking plugin");
            this.put("command-convert-converted", "&eConverted data from &b%plugin% &eto RoseStacker. Hindi pinagana ang na-convert na plugin. Tiyaking aalisin ang na-convert na plugin mula sa iyong folder ng mga plugin.");
            this.put("command-convert-failed", "&cNabigong mag-convert &b%plugin%&c, plugin ay hindi pinagana.");
            this.put("command-convert-aborted", "&cInalis ang pagtatangkang mag-convert &b%plugin%&c. Nag-convert ka na mula sa isa pang stacking plugin.");

            this.put("#9", "Translate Command");
            this.put("command-translate-description", "Isinalin ang mga pangalan ng stack");
            this.put("command-translate-loading", "&ePag-download at paglalapat ng data ng pagsasalin, maaari itong tumagal ng ilang sandali.");
            this.put("command-translate-failure", "&cHindi maisalin ang mga pangalan ng stack. Mayroong isang problema sa pagkuha ng data ng lokal. Subukang muli mamaya.");
            this.put("command-translate-invalid-locale", "&cHindi maisalin ang mga pangalan ng stack. Ang lokal na iyong tinukoy ay hindi wasto.");
            this.put("command-translate-spawner-format", "&eAng mga pangalan ng Spawner ay hindi maisasalin nang tumpak. Upang ayusin ito, maaari mong gamitin ang &b/rs translate en_us &3{} Spawner &eupang lumitaw ang isang spawner bilang \"Cow Spawner\". gumamit ng &b{} &ebilang isang placeholder para sa pangalan ng nagkakagulong mga tao.");
            this.put("command-translate-spawner-format-invalid", "&cAng format ng spawner na iyong ibinigay ay hindi wasto. Dapat maglaman ito ng &b{} &cpara sa pagkakalagay ng pangalan ng mob.");
            this.put("command-translate-success", "&aMatagumpay na naisalin ang mga pangalan ng stack.");

            this.put("#10", "Stacking Tool Command");
            this.put("command-stacktool-description", "Binibigyan ang isang manlalaro ng stacking tool");
            this.put("command-stacktool-given", "&eNabigyan ka ng tool sa pag-stack.");
            this.put("command-stacktool-given-other", "&b%player% &abinigyan ng stacking tool.");
            this.put("command-stacktool-no-permission", "&cWala kang pahintulot na gamitin ang tool sa pag-stack.");
            this.put("command-stacktool-invalid-entity", "&cAng entity na iyon ay hindi bahagi ng isang stack, ito ba ay isang pasadyang manggugulo?");
            this.put("command-stacktool-marked-unstackable", "&eAng &b%type% &eay minarkahan bilang &cunstackable&e.");
            this.put("command-stacktool-marked-stackable", "&eAng &b%type% &eay minarkahan bilang &astackable&e.");
            this.put("command-stacktool-marked-all-unstackable", "&eAng buong &b%type% &eang stack ay minarkahan bilang &cunstackable&e.");
            this.put("command-stacktool-select-1", "&eAng &b%type% &enapili bilang Entity # 1. Pumili ng isa pang nilalang upang subukan kung maaari silang mag-stack.");
            this.put("command-stacktool-unselect-1", "&eAng &b%type% &eay napili.");
            this.put("command-stacktool-select-2", "&eAng &b%type% &enapili bilang Entity #2.");
            this.put("command-stacktool-can-stack", "&aEntity #1 maaaring i-stack sa Entity #2.");
            this.put("command-stacktool-can-not-stack", "&cAng Entity 1 ay hindi maaaring i-stack sa Entity 2. Dahilan: &b%reason%");
            this.put("command-stacktool-info", "&eImpormasyon ng stack::");
            this.put("command-stacktool-info-id", "&eStack ID: &b%id%");
            this.put("command-stacktool-info-uuid", "&eUUID: &b%uuid%");
            this.put("command-stacktool-info-entity-id", "&eEntity ID: &b%id%");
            this.put("command-stacktool-info-custom-name", "&ePasadyang Pangalan: &r%name%");
            this.put("command-stacktool-info-location", "&eLokasyon: X: &b%x% &eY: &b%y% &eZ: &b%z% &eMundo: &b%world%");
            this.put("command-stacktool-info-chunk", "&eChunk: &b%x%&e, &b%z%");
            this.put("command-stacktool-info-true", "&atrue");
            this.put("command-stacktool-info-false", "&cfalse");
            this.put("command-stacktool-info-entity-type", "&eUri ng Entity: &b%type%");
            this.put("command-stacktool-info-entity-stackable", "&eStackable: %value%");
            this.put("command-stacktool-info-entity-has-ai", "&eMerong AI: %value%");
            this.put("command-stacktool-info-entity-from-spawner", "&eFrom Spawner: %value%");
            this.put("command-stacktool-info-item-type", "&eUri ng gamit: &b%type%");
            this.put("command-stacktool-info-block-type", "&eUri ng Bloke Type: &b%type%");
            this.put("command-stacktool-info-spawner-type", "&eSpawner Type: &b%type%");
            this.put("command-stacktool-info-stack-size", "&eStack Size: &b%amount%");

            this.put("#11", "Stacked Block GUI");
            this.put("gui-stacked-block-title", "pag-edit ng %name% Stack");
            this.put("gui-stacked-block-page-back", List.of("&eNakaraang pahina (" + GuiUtil.PREVIOUS_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-page-forward", List.of("&eSusunod na pahina (" + GuiUtil.NEXT_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-destroy", List.of("&cWasakin ang Stack", "&eWinawasak ang stack at nahuhulog ang mga item"));
            this.put("gui-stacked-block-destroy-title", "Wasakin ang Block Stack?");
            this.put("gui-stacked-block-destroy-confirm", List.of("&aKumpirmahin", "&eOo, sirain ang stack"));
            this.put("gui-stacked-block-destroy-cancel", List.of("&cKanselahin", "&eHindi, bumalik sa nakaraang screen"));

            this.put("#12", "Stacked Spawner GUI");
            this.put("gui-stacked-spawner-title", "Pagtingin %name%");
            this.put("gui-stacked-spawner-stats", "&6Spawner Stats");
            this.put("gui-stacked-spawner-min-spawn-delay", "&eMin na Spawn Delay: &b%delay%");
            this.put("gui-stacked-spawner-max-spawn-delay", "&eMax na Spawn Delay: &b%delay%");
            this.put("gui-stacked-spawner-disabled-mob-ai", "&eDisable ang Mob AI: &b%disabled%");
            this.put("gui-stacked-spawner-entity-search-range", "&eSaklaw ng Paghahanap ng Entity: &b%range%");
            this.put("gui-stacked-spawner-player-activation-range", "&eSaklaw ng Pag-aktibo ng Player: &b%range%");
            this.put("gui-stacked-spawner-spawn-range", "&eSaklaw ng Spawn: &b%range%");
            this.put("gui-stacked-spawner-min-spawn-amount", "&eMinimum na Halaga ng Spawn: &b%amount%");
            this.put("gui-stacked-spawner-max-spawn-amount", "&eMaximum na Halaga ng Spawn: &b%amount%");
            this.put("gui-stacked-spawner-spawn-amount", "&eHalaga ng Spawn: &b%amount%");
            this.put("gui-stacked-spawner-spawn-conditions", "&6Mga Kundisyon ng Spawn");
            this.put("gui-stacked-spawner-time-until-next-spawn", "&eOras hanggang sa susunod na itlog: &b%time% ticks");
            this.put("gui-stacked-spawner-total-spawns", "&eKabuuang mga nagkakagulong mga tao ay nagsilang: &b%amount%");
            this.put("gui-stacked-spawner-valid-spawn-conditions", "&6Mga wastong Kundisyon ng Spawn");
            this.put("gui-stacked-spawner-invalid-spawn-conditions", "&6Di-wastong Mga Kundisyon ng Spawn");
            this.put("gui-stacked-spawner-entities-can-spawn", "&aAng mga entity ay nakapag-itlog");
            this.put("gui-stacked-spawner-conditions-preventing-spawns", "&eMga kundisyon na pumipigil sa mga spawns:");

            this.put("#13", "Spawn Condition Messages");
            this.put("spawner-condition-invalid", "&7 - &c%message%");
            this.put("spawner-condition-info", "&e%condition%");
            this.put("spawner-condition-single", "&e%condition%: &b%value%");
            this.put("spawner-condition-list", "&e%condition%:");
            this.put("spawner-condition-list-item", "&7 - &b%message%");
            this.put("spawner-condition-above-sea-level-info", "Sa Itaas na Antas ng Dagat");
            this.put("spawner-condition-above-sea-level-invalid", "Walang spawn area sa taas ng dagat");
            this.put("spawner-condition-above-y-axis-info", "Sa Itaas Y-Axis");
            this.put("spawner-condition-above-y-axis-invalid", "Walang spawn area sa itaas na kinakailangan ng Y-Axis");
            this.put("spawner-condition-air-info", "Open Air");
            this.put("spawner-condition-air-invalid", "Walang malalaking sapat na puwang ng hangin na magagamit");
            this.put("spawner-condition-below-sea-level-info", "Sa Ibabang Antas ng Dagat");
            this.put("spawner-condition-below-sea-level-invalid", "Walang spawn area sa ibaba ng antas ng dagat");
            this.put("spawner-condition-below-y-axis-info", "Sa ibaba ng Y-Axis");
            this.put("spawner-condition-below-y-axis-invalid", "Walang spawn area sa ibaba kinakailangan ng Y-Axis");
            this.put("spawner-condition-biome-info", "Biome");
            this.put("spawner-condition-biome-invalid", "Maling biome");
            this.put("spawner-condition-block-info", "Spawn Block");
            this.put("spawner-condition-block-invalid", "Walang wastong mga bloke ng itlog");
            this.put("spawner-condition-block-exception-info", "Pagbubukod ng Spawn Block");
            this.put("spawner-condition-block-exception-invalid", "Hindi kasama ang mga spawn block");
            this.put("spawner-condition-darkness-info", "Mababang Antas ng Magaan");
            this.put("spawner-condition-darkness-invalid", "Masyadong maliwanag ang lugar");
            this.put("spawner-condition-total-darkness-info", "Ganap na Kadiliman");
            this.put("spawner-condition-total-darkness-invalid", "Dapat walang ilaw ang lugar");
            this.put("spawner-condition-fluid-info", "Nangangailangan ng Fluid");
            this.put("spawner-condition-fluid-invalid", "Walang malapit na likido");
            this.put("spawner-condition-lightness-info", "Mataas na Antas ng Magaan");
            this.put("spawner-condition-lightness-invalid", "Masyadong madilim ang lugar");
            this.put("spawner-condition-max-nearby-entities-info", "Max na Kalapit na Entidad");
            this.put("spawner-condition-max-nearby-entities-invalid", "Masyadong maraming mga kalapit na entity");
            this.put("spawner-condition-no-skylight-access-info", "Walang Skylight Access");
            this.put("spawner-condition-no-skylight-access-invalid", "Walang mga bloke ng itlog nang walang pag-access sa skylight");
            this.put("spawner-condition-on-ground-info", "Sa Lupa");
            this.put("spawner-condition-on-ground-invalid", "Walang malapit na lupa sa malapit");
            this.put("spawner-condition-skylight-access-info", "Pag-access sa Skylight");
            this.put("spawner-condition-skylight-access-invalid", "Walang mga bloke ng itlog na may access sa skylight");
            this.put("spawner-condition-none-invalid", "Lumagpas sa maximum na mga pagtatangka ng spawn");
            this.put("spawner-condition-not-player-placed-invalid", "Dapat ilagay ng isang manlalaro");

            this.put("#14", "Given Stack Item Lore");
            this.put("#15", "Note: This will appear in the lore of the items given from the '/rs give' command");
            this.put("stack-item-lore-spawner", new ArrayList<>());
            this.put("stack-item-lore-block", new ArrayList<>());
            this.put("stack-item-lore-entity", new ArrayList<>());

            this.put("#16", "Generic Command Messages");
            this.put("no-permission", "&cWala kang pahintulot para doon!");
            this.put("only-player", "&cAng utos na ito ay maaari lamang isagawa ng isang manlalaro.");
            this.put("unknown-command", "&cHindi kilalang command, gamitin ang &b/%cmd% help &cpara sa higit pang impormasyon.");
            this.put("unknown-command-error", "&cMay naganap na hindi kilalang error. Ang mga detalye ay na-print para sa console. Mangyaring makipag-ugnayan sa isang administrator ng server.");
            this.put("invalid-subcommand", "&cDi-wastong subcommand.");
            this.put("invalid-argument", "&cDi-wastong argumento: %message%.");
            this.put("invalid-argument-null", "&cDi-wastong argumento: %name% ay null.");
            this.put("missing-arguments", "&cMga nawawalang argumento, &b%amount% &cang kailangan.");
            this.put("missing-arguments-extra", "&cMga nawawalang argumento, &b%amount%+ &cang kailangan.");

            this.put("#17", "Argument Handler Error Messages");
            this.put("argument-handler-enum", "%enum% uri [%input%] ay hindi umiiral");
            this.put("argument-handler-enum-list", "%enum% uri [%input%] ay hindi umiiral. Mga wastong uri: %types%");
            this.put("argument-handler-string", "Hindi maaaring walang laman ang string");
            this.put("argument-handler-integer", "Ang integer [%input%] ay dapat na isang buong numero sa pagitan ng -2^31 at 2^31-1 kasama");
            this.put("argument-handler-player", "Walang Player na may username na [%input%] ang natagpuan online");
            this.put("argument-handler-stackplugin", "Walang nakitang plugin na may pangalang [%input%]");
            this.put("argument-handler-material", "Walang nakitang materyal na may pangalang [%input%]");
            this.put("argument-handler-stackamount", "Ang laki ng stack [%input%] ay di-wasto, dapat ay isang numerong mas mataas sa 0");
            this.put("argument-handler-stacktype", "Ang uri ng stack na [%input%] ay hindi wasto");
            this.put("argument-handler-translationlocale", "Ang lokal na pagsasalin [%input%] ay hindi wasto");

            this.put("#18", "Convert Lock Messages");
            this.put("convert-lock-conflictions", "&cMayroong mga plugin sa iyong server na kilalang sumasalungat sa RoseStacker. " +
                    "Upang mapigilan ang mga pagkakasalungatan at/o pagkawala ng data, hindi pinagana ng RoseStacker ang isa o higit pang mga uri ng stack. " +
                    "Ang isang file ay nilikha sa mga plugin /" + RoseStacker.getInstance().getName() + "/" + ConversionManager.FILE_NAME + " kung saan maaari mong i-configure ang mga hindi pinagana na uri ng stack. " +
                    "Papayagan ka rin ng file na iyon na kilalanin na nabasa mo ang babalang ito at hahayaan kang huwag paganahin ang mensaheng ito.");

            this.put("#19", "Misc Messages");
            this.put("spawner-advanced-place-range","&cBabala! Hindi ka maaaring maglagay ng spawner dito!");
            this.put("spawner-silk-touch-protect", "&cBabala! &eKailangan mong gumamit ng isang sutla na pick picke at / o may pahintulot na kunin ang mga spawner. Hindi mo magagawa kung hindi man.");
            this.put("spawner-advanced-place-no-permission", "&cBabala! &eWala kang pahintulot na ilagay ang uri ng spawner.");
            this.put("spawner-advanced-break-no-permission", "&cBabala! &eWala kang pahintulot na kunin ang uri ng spawner.");
            this.put("spawner-advanced-break-silktouch-no-permission", "&cBabala! &eKailangan mong gumamit ng isang sutla na pick picke upang kunin ang ganitong uri ng spawner.");
            this.put("spawner-convert-not-enough", "&cBabala! &eHindi ma-convert ang mga spawner gamit ang mga itlog ng itlog. Wala kang sapat na itlog ng itlog upang gawin ang conversion na ito.");
            this.put("number-separator", ",");
            this.put("silktouch-chance-placeholder", "%chance%%");
        }};
    }
}
