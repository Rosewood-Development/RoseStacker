package dev.rosewood.rosestacker.locale;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.locale.Locale;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RomanianLocale implements Locale {

    @Override
    public String getLocaleName() {
        return "ro_RO";
    }

    @Override
    public String getTranslatorName() {
        return "Sarah";
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
            this.put("spawner-hologram-display-empty", List.of("&c%amount%x &7%name%"));
            this.put("spawner-hologram-display-empty-single", List.of("&7%name%"));

            this.put("#2", "Base Command Message");
            this.put("base-command-color", "&e");
            this.put("base-command-help", "&eFoloseste &b/%cmd% help &epentru ajutor");

            this.put("#3", "Help command");
            this.put("command-help-description", "Deschide meniul de ajutor");
            this.put("command-help-title", "&eComenzi disponibile:");
            this.put("command-help-list-description", "&8 - &d/%cmd% %subcmd% %args% &7- %desc%");
            this.put("command-help-list-description-no-args", "&8 - &d/%cmd% %subcmd% &7- %desc%");

            this.put("#4", "Reload command");
            this.put("command-reload-description", "Reincarca configuratia");
            this.put("command-reload-reloaded", "&eConfiguratia a fost reincarcata.");

            this.put("#5", "Give Command");
            this.put("command-give-description", "Ofera obiecte pre-stackate");
            this.put("command-give-usage", "&cComanda: &e/rs give <block|spawner|entitate> <jucator> <tip> [dimensiuneStack] [cantitate]");
            this.put("command-give-given", "&b%player% &ea primit [%display%&e].");
            this.put("command-give-given-multiple", "&b%player% &ea primit %amount%x [%display%&e].");
            this.put("command-give-unstackable", "&cTipul pe care l-ai specificat nu este stackabil.");
            this.put("command-give-too-large", "&cCantitatea specificata depaseste dimensiunea maxima a stackarii pentru acest tip.");

            this.put("#6", "Clearall Command");
            this.put("command-clearall-description", "Sterge toate entitatile");
            this.put("command-clearall-killed-entities", "&eAu fost sterse &b%amount% &eentitati.");
            this.put("command-clearall-killed-items", "&eAu fost sterse &b%amount% &eobiecte.");
            this.put("command-clearall-killed-all", "&eAu fost sterse &b%entityAmount% &eentitati si &b%itemAmount% &eobiecte.");

            this.put("#7", "Stats Command");
            this.put("command-stats-description", "Arata statisticile plugin-ului");
            this.put("command-stats-header", "&aStatistici Plugin:");
            this.put("command-stats-threads", "&b%amount% &efire active de stackare.");
            this.put("command-stats-stacked-entities", "&b%stackAmount% &estack-uri de entitati incarcate, in total &b%total% &eentitati.");
            this.put("command-stats-stacked-items", "&b%stackAmount% &estack-uri de obiecte incarcate, in total &b%total% &eobiecte.");
            this.put("command-stats-stacked-blocks", "&b%stackAmount% &estack-uri de block-uri incarcate, in total &b%total% &eblock-uri.");
            this.put("command-stats-stacked-spawners", "&b%stackAmount% &estack-uri de spawnere incarcate, in total &b%total% &espawnere.");
            this.put("command-stats-active-tasks", "&b%amount% &eactive feladatok.");

            this.put("#9", "Translate Command");
            this.put("command-translate-description", "Traduce plugin-ul");
            this.put("command-translate-loading", "&eSe descarca si aplica datele traducerii, asteapta cateva momente.");
            this.put("command-translate-failure", "&cNu s-a putut traduce, a intervenit o eroare in datele locale. Incearca mai tarziu");
            this.put("command-translate-invalid-locale", "&cNu s-a putut traduce, fisierul locale specificat este invalid.");
            this.put("command-translate-spawner-format", "&eNumele spawnerelor nu pot fi traduse cu precizie. Pentru rezolvare, poti folosi &b/rs translate en_us &3{} Spawner &epentru a facu un spawner sa apara ca \"Cow Spawner\". Foloseste &b{} &eca placeholder pentru numele entitatii.");
            this.put("command-translate-spawner-format-invalid", "&cTipul spawner-ului este invalid. Trebuie sa contina &b{} &cpentru plasarea numelui.");
            this.put("command-translate-success", "&aTraducerea a fost efectuata cu succes.");

            this.put("#10", "Stacking Tool Command");
            this.put("command-stacktool-description", "Ofera unui jucator stacking tool");
            this.put("command-stacktool-given", "&eAi primit stacking tool.");
            this.put("command-stacktool-given-other", "&b%player% &aa primit stacking tool.");
            this.put("command-stacktool-no-console", "&cNu puteți da stacking tool la consolă.");
            this.put("command-stacktool-no-permission", "&cNu ai permisiunea sa folosesti stacking tool.");
            this.put("command-stacktool-invalid-entity", "&cThat entity is not part of a stack, is it a custom mob?");
            this.put("command-stacktool-marked-unstackable", "&b%type% &ea fost setat ca &cnestackabil&e.");
            this.put("command-stacktool-marked-stackable", "&b%type% &ea fost setat ca &astackabil&e.");
            this.put("command-stacktool-marked-all-unstackable", "&eIntregul &b%type% &estack a fost setat ca &cnestackabl&e.");
            this.put("command-stacktool-select-1", "&b%type% &ea fost setat ca Entity #1. Selecteaza alta entitate pentru a vedea daca se pot stacka.");
            this.put("command-stacktool-unselect-1", "&b%type% &ea fost deselectat.");
            this.put("command-stacktool-select-2", "&b%type% &ea fost selectat ca Entity #2.");
            this.put("command-stacktool-can-stack", "&aEntity #1 se poate stacka cu Entity #2.");
            this.put("command-stacktool-can-not-stack", "&cEntity 1 nu se poate stacka cu Entity 2. Motiv: &b%reason%");
            this.put("command-stacktool-info", "&eStatistici stack:");
            this.put("command-stacktool-info-uuid", "&eUUID: &b%uuid%");
            this.put("command-stacktool-info-entity-id", "&eID Entitate: &b%id%");
            this.put("command-stacktool-info-custom-name", "&eNume personalizat: &r%name%");
            this.put("command-stacktool-info-location", "&eLocatie: X: &b%x% &eY: &b%y% &eZ: &b%z% &eLume: &b%world%");
            this.put("command-stacktool-info-chunk", "&eChunk: &b%x%&e, &b%z%");
            this.put("command-stacktool-info-true", "&aadevarat");
            this.put("command-stacktool-info-false", "&cfals");
            this.put("command-stacktool-info-entity-type", "&eTip Entitate: &b%type%");
            this.put("command-stacktool-info-entity-stackable", "&eStackabil: %value%");
            this.put("command-stacktool-info-entity-has-ai", "&eAI: %value%");
            this.put("command-stacktool-info-entity-from-spawner", "&eDin Spawner: %value%");
            this.put("command-stacktool-info-item-type", "&eTip Obiect: &b%type%");
            this.put("command-stacktool-info-block-type", "&eTip Block: &b%type%");
            this.put("command-stacktool-info-spawner-type", "&eTip Spawner: &b%type%");
            this.put("command-stacktool-info-stack-size", "&eDimensiune Stack: &b%amount%");

            this.put("#11", "Stacked Block GUI");
            this.put("gui-stacked-block-title", "Se editeaza %name% Stack");
            this.put("gui-stacked-block-page-back", List.of("&ePagina precedenta (" + GuiUtil.PREVIOUS_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-page-forward", List.of("&ePagina urmatoare (" + GuiUtil.NEXT_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-destroy", List.of("&cDistrugere Stack", "&eDistruge stack-ul si dropeaza obiectele"));
            this.put("gui-stacked-block-destroy-title", "Distrugi block-urile?");
            this.put("gui-stacked-block-destroy-confirm", List.of("&aConfirma", "&eDa, distruge stack-ul"));
            this.put("gui-stacked-block-destroy-cancel", List.of("&cAnuleaza", "&eNu, intoarce-te la ecranul precedent"));

            this.put("#12", "Stacked Spawner GUI");
            this.put("gui-stacked-spawner-title", "Se vizualizeaza %name%");
            this.put("gui-stacked-spawner-stats", "&6Statistici Spawner");
            this.put("gui-stacked-spawner-min-spawn-delay", "&eTimp minim de spawnare: &b%delay%");
            this.put("gui-stacked-spawner-max-spawn-delay", "&eTimp maxim de spawnare: &b%delay%");
            this.put("gui-stacked-spawner-disabled-mob-ai", "&eMob AI Dezactivat: &b%disabled%");
            this.put("gui-stacked-spawner-entity-search-range", "&eRaza maxima de stackare: &b%range%");
            this.put("gui-stacked-spawner-player-activation-range", "&eRaza maxima jucator-spawner: &b%range%");
            this.put("gui-stacked-spawner-spawn-range", "&eRaza maxima de spawnare: &b%range%");
            this.put("gui-stacked-spawner-min-spawn-amount", "&eNumar minim de mobi spawnati: &b%amount%");
            this.put("gui-stacked-spawner-max-spawn-amount", "&eNumar maxim de mobi spawnati: &b%amount%");
            this.put("gui-stacked-spawner-spawn-amount", "&eNumar de mobi spawnati: &b%amount%");
            this.put("gui-stacked-spawner-spawn-conditions", "&6Conditii de spawnare");
            this.put("gui-stacked-spawner-time-until-next-spawn", "&eTimp pana la spawnare: &b%time% ticks");
            this.put("gui-stacked-spawner-total-spawns", "&eNumar total de mobi spawnati: &b%amount%");
            this.put("gui-stacked-spawner-valid-spawn-conditions", "&6Conditii valide de spawnare");
            this.put("gui-stacked-spawner-invalid-spawn-conditions", "&6Conditii invalide de spawnare");
            this.put("gui-stacked-spawner-entities-can-spawn", "&aMobii se pot spawna");
            this.put("gui-stacked-spawner-conditions-preventing-spawns", "&eConditii de prevenire a spawnarii:");

            this.put("#13", "Spawn Condition Messages");
            this.put("spawner-condition-invalid", "&7 - &c%message%");
            this.put("spawner-condition-info", "&e%condition%");
            this.put("spawner-condition-single", "&e%condition%: &b%value%");
            this.put("spawner-condition-list", "&e%condition%:");
            this.put("spawner-condition-list-item", "&7 - &b%message%");
            this.put("spawner-condition-above-sea-level-info", "Deasupra apei");
            this.put("spawner-condition-above-sea-level-invalid", "Niciun loc de spawnare deasupra apei");
            this.put("spawner-condition-above-y-axis-info", "Deasupra Axei-Y");
            this.put("spawner-condition-above-y-axis-invalid", "Niciun loc de spawnare deasupra Axei-Y");
            this.put("spawner-condition-air-info", "In aer liber");
            this.put("spawner-condition-air-invalid", "Spatiu insuficient");
            this.put("spawner-condition-below-sea-level-info", "Sub apa");
            this.put("spawner-condition-below-sea-level-invalid", "Niciun loc de spawnare sub apa");
            this.put("spawner-condition-below-y-axis-info", "Sub Axa-Y");
            this.put("spawner-condition-below-y-axis-invalid", "Niciun loc de spawnare sub Axa-Y");
            this.put("spawner-condition-biome-info", "Biome");
            this.put("spawner-condition-biome-invalid", "Biome nepotrivit");
            this.put("spawner-condition-block-info", "Block de spawnare");
            this.put("spawner-condition-block-invalid", "Block de spawnare invalid");
            this.put("spawner-condition-block-exception-info", "Exceptii de block-uri de spawnare");
            this.put("spawner-condition-block-exception-invalid", "Block-uri de spawnare excluse");
            this.put("spawner-condition-darkness-info", "Nivel scazut de lumina");
            this.put("spawner-condition-darkness-invalid", "Necesita intuneric");
            this.put("spawner-condition-total-darkness-info", "Întuneric total");
            this.put("spawner-condition-total-darkness-invalid", "Zona nu trebuie să aibă lumină");
            this.put("spawner-condition-fluid-info", "Necesita fluiditate");
            this.put("spawner-condition-fluid-invalid", "Fluiditate negasita");
            this.put("spawner-condition-lightness-info", "Nivel ridicat de lumina");
            this.put("spawner-condition-lightness-invalid", "Necesita lumina");
            this.put("spawner-condition-max-nearby-entities-info", "Numar maxim de mobi in preajma");
            this.put("spawner-condition-max-nearby-entities-invalid", "Numar maxim de mobi in preajma");
            this.put("spawner-condition-no-skylight-access-info", "Fara lumina naturala");
            this.put("spawner-condition-no-skylight-access-invalid", "Nu sunt block-uri fara lumina naturala");
            this.put("spawner-condition-on-ground-info", "Pe pamant");
            this.put("spawner-condition-on-ground-invalid", "Niciun teren solid in apropiere");
            this.put("spawner-condition-skylight-access-info", "Acces la lumina naturala");
            this.put("spawner-condition-skylight-access-invalid", "Necesita lumina naturala");
            this.put("spawner-condition-none-invalid", "Incercari de spawnere depasita");
            this.put("spawner-condition-not-player-placed-invalid", "Trebuie să fie plasat de către un jucător");

            this.put("#14", "Given Stack Item Lore");
            this.put("#15", "Note: This will appear in the lore of the items given from the '/rs give' command");
            this.put("stack-item-lore-spawner", new ArrayList<>());
            this.put("stack-item-lore-block", new ArrayList<>());
            this.put("stack-item-lore-entity", new ArrayList<>());

            this.put("#16", "Generic Command Messages");
            this.put("no-permission", "&cNu ai permisiunea pentru a face asta!");
            this.put("only-player", "&cAceastă comandă poate fi folosită doar de către un jucător.");
            this.put("unknown-command", "&cComandă necunoscută, utilizați &b/%cmd% help &cpentru mai multe informații.");
            this.put("unknown-command-error", "&cA apărut o eroare necunoscută; detaliile au fost tipărite în consolă. Vă rugăm să contactați un administrator de server.");
            this.put("invalid-subcommand", "&cSubcomandă invalidă.");
            this.put("invalid-argument", "&cArgument nevalabil: %message%.");
            this.put("invalid-argument-null", "&cArgument nevalabil: %name% era nul.");
            this.put("missing-arguments", "&cArgumente lipsă, &b%amount% &ceste necesar.");
            this.put("missing-arguments-extra", "&cArgumente lipsă, &b%amount%+ &ceste necesar.");

            this.put("#17", "Argument Handler Error Messages");
            this.put("argument-handler-enum", "%enum% tipul [%input%] nu există");
            this.put("argument-handler-enum-list", "Tipul %enum% [%input%] nu există. Tipuri valide: %types%");
            this.put("argument-handler-string", "Șirul nu poate fi gol");
            this.put("argument-handler-integer", "Întregul [%input%] trebuie să fie un număr întreg cuprins între -2^31 și 2^31-1 inclusiv.");
            this.put("argument-handler-player", "Niciun jucător cu numele de utilizator [%input%] nu a fost găsit online");
            this.put("argument-handler-stackplugin", "Nu a fost găsit niciun plugin cu numele [%input%].");
            this.put("argument-handler-material", "Nu a fost găsit niciun material cu numele [%input%].");
            this.put("argument-handler-stackamount", "Dimensiunea stivei [%input%] nu este validă, trebuie să fie un număr mai mare decât 0");
            this.put("argument-handler-stacktype", "Tipul de stivă [%input%] nu este valid");
            this.put("argument-handler-translationlocale", "Locația de traducere [%input%] nu este validă");

            this.put("#19", "Misc Messages");
            this.put("spawner-silk-touch-protect", "&cAtentie! &eAi nevoie de un tarnacop cu Silk Touch si/sau trebuie sa ai permisiunea de a lua spawnerele.");
            this.put("spawner-advanced-place-no-permission", "&cAtentie! &eNu ai permisiunea pentru a pune acest tip de spawner.");
            this.put("spawner-advanced-break-no-permission", "&cAtentie! &eNu ai permisiunea pentru a lua acest tip de spawner.");
            this.put("spawner-advanced-break-silktouch-no-permission", "&cAtentie! &eAi nevoie de un tarnacop cu Silk Touch pentru a lua acest spawner.");
            this.put("spawner-convert-not-enough", "&cAtentie! &eNu s-au putut converti spawnerele folosind oua. Nu ai in mana suficiente oua pentru a efectua conversiunea.");
            this.put("number-separator", ",");
            this.put("silktouch-chance-placeholder", "%chance%%");
        }};
    }
}
