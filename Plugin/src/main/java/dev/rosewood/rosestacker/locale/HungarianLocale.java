package dev.rosewood.rosestacker.locale;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.locale.Locale;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HungarianLocale implements Locale {

    @Override
    public String getLocaleName() {
        return "hu_HU";
    }

    @Override
    public String getTranslatorName() {
        return "PatrikX";
    }

    @Override
    public Map<String, Object> getDefaultLocaleValues() {
        return new LinkedHashMap<>() {{
            this.put("#0", "Plugin Message Prefix");
            this.put("prefix", "&7[<g:#8A2387:#E94057:#F27121>RoseStacker&7] ");

            this.put("#1", "Stack Display Tags");
            this.put("entity-stack-display", "&c%amount%x &7%name%");
            this.put("entity-stack-display-custom-name", "%name% &7[&c%amount%x&7]");
            this.put("entity-stack-display-spawn-egg", "&c%amount%x &7%name% Idéző Tojás");
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
            this.put("base-command-help", "&eHasználd a &b/%cmd% help &eparancsot segítségért.");

            this.put("#3", "Help Command");
            this.put("command-help-description", "Előhozza a segítségeket... Ahova megérkeztél");
            this.put("command-help-title", "&eElérhető Parancsok:");
            this.put("command-help-list-description", "&8 - &d/%cmd% %subcmd% %args% &7- %desc%");
            this.put("command-help-list-description-no-args", "&8 - &d/%cmd% %subcmd% &7- %desc%");

            this.put("#4", "Reload Command");
            this.put("command-reload-description", "Újratölti a plugint");
            this.put("command-reload-reloaded", "&ePlugin adatok, konfiguráció, és a fordítások újratöltődnek.");

            this.put("#5", "Give Command");
            this.put("command-give-description", "Ad előre stackelt tárgyakat");
            this.put("command-give-usage", "&cHasználat: &e/rs give <blokk|spawner|entitás> <játékos> <típus> [stackMéret] [mennyiség]");
            this.put("command-give-given", "&eAdtál &b%player% &e[%display%&e].");
            this.put("command-give-given-multiple", "&eAdtál &b%player%-nak/nek &e%amount%x [%display%&e].");
            this.put("command-give-unstackable", "&cA típus amit megadtál nem stackelhető.");
            this.put("command-give-too-large", "&cA mennyiség amit megadtál nagyobb mint a maximum stack méret ennél a típusnál.");

            this.put("#6", "Clearall Command");
            this.put("command-clearall-description", "Törli az összes stack típust");
            this.put("command-clearall-killed-entities", "&eTörölve &b%amount% &eentitás stack.");
            this.put("command-clearall-killed-items", "&eTörölve &b%amount% &etárgy stack.");
            this.put("command-clearall-killed-all", "&eTörölve &b%entityAmount% &eentitás stack és &b%itemAmount% &etárgy stack.");

            this.put("#7", "Stats Command");
            this.put("command-stats-description", "Kiír statisztikákat a pluginról");
            this.put("command-stats-header", "&aJelenlegi Plugin Statisztika:");
            this.put("command-stats-threads", "&b%amount% &eaktív stackelés szál.");
            this.put("command-stats-stacked-entities", "&b%stackAmount% &ebetöltött entitás stack, összesen &b%total% &eentitás.");
            this.put("command-stats-stacked-items", "&b%stackAmount% &ebetöltött tárgy stack, összesen &b%total% &etárgy.");
            this.put("command-stats-stacked-blocks", "&b%stackAmount% &ebetöltött blokk stack, összesen &b%total% &eblokk.");
            this.put("command-stats-stacked-spawners", "&b%stackAmount% &ebetöltött spawner stack, összesen &b%total% &espawner.");
            this.put("command-stats-active-tasks", "&b%amount% &eactive feladatok.");

            this.put("#9", "Translate Command");
            this.put("command-translate-description", "Lefordítja a stack neveket");
            this.put("command-translate-loading", "&eFordítás adatok letöltése és alkalmazása, ez eltarthat egy darabig.");
            this.put("command-translate-failure", "&cNem lehet lefordítani a stack neveket. Problémák akadtak a helyi adatokkal. Kérlek próbáld újra később.");
            this.put("command-translate-invalid-locale", "&cNem lehet lefordítani a stack neveket. A nyelv amit megadtál nem érvényes.");
            this.put("command-translate-spawner-format", "&eSpawner nevek nem fordíthatók pontosan. Hogy kijavítsd ezt, használd így a parancsot &b/rs translate hu_hu &3{} " +
                    "Spawner &ehogy a spawner neve ez legyen \"Cow Spawner\". Használd a &b{} &eszörny név helyettesítéséhez.");
            this.put("command-translate-spawner-format-invalid", "&cA spawner formátum amit megadtál nem érvényes. Tartalmaznia kell a &b{} &cjelleket hogy kicserélhesse a szörny nevére.");
            this.put("command-translate-success", "&aSikeresen lefordítva a stackek nevei.");

            this.put("#10", "Stacking Tool Command");
            this.put("command-stacktool-description", "Ad a játékosnak egy stackelő eszközt");
            this.put("command-stacktool-given", "&eMegkaptad a stackelő eszközt.");
            this.put("command-stacktool-given-other", "&b%player% &amegkapta a stackelő eszközt.");
            this.put("command-stacktool-no-console", "&cNem tudod a konzolnak adni a stackelő eszközt.");
            this.put("command-stacktool-no-permission", "&cNincs jogod használni a stackelő eszközt.");
            this.put("command-stacktool-invalid-entity", "&cAz entitás nem része egy stacknek se, talán egyedi szörny?");
            this.put("command-stacktool-marked-unstackable", "&eA &b%type% &emegjelölve &cnem stackelhető &eként.");
            this.put("command-stacktool-marked-stackable", "&eA &b%type% &emegjelölve &astackelhető &eként.");
            this.put("command-stacktool-marked-all-unstackable", "&eAz egész &b%type% &estack megjelölve &cnem stackelhető &eként.");
            this.put("command-stacktool-select-1", "&eA &b%type% &ekiválasztva Entitás #1 ként. Válassz ki egy másik entitást hogy teszteld stackelhetők-e.");
            this.put("command-stacktool-unselect-1", "&eA &b%type% &ekiválasztása megszüntetve.");
            this.put("command-stacktool-select-2", "&eA &b%type% &ekiválasztva Entitás #2 ként.");
            this.put("command-stacktool-can-stack", "&aEntitás #1 tud stackelődni Entitás #2-vel.");
            this.put("command-stacktool-can-not-stack", "&cEntitás #1 nem tud stackelődni Entitás #2-vel. Oka: &b%reason%");
            this.put("command-stacktool-info", "&eStack Információ:");
            this.put("command-stacktool-info-uuid", "&eUUID: &b%uuid%");
            this.put("command-stacktool-info-entity-id", "&eEntitás ID: &b%id%");
            this.put("command-stacktool-info-custom-name", "&eEgyedi Név: &r%name%");
            this.put("command-stacktool-info-location", "&eHelyzete: X: &b%x% &eY: &b%y% &eZ: &b%z% &eVilág: &b%world%");
            this.put("command-stacktool-info-chunk", "&eChunk: &b%x%&e, &b%z%");
            this.put("command-stacktool-info-true", "&aigaz");
            this.put("command-stacktool-info-false", "&chamis");
            this.put("command-stacktool-info-entity-type", "&eEntitás Típus: &b%type%");
            this.put("command-stacktool-info-entity-stackable", "&eStackelhető: %value%");
            this.put("command-stacktool-info-entity-has-ai", "&eVan AI: %value%");
            this.put("command-stacktool-info-entity-from-spawner", "&eSpawnerből Idéződött: %value%");
            this.put("command-stacktool-info-item-type", "&eTárgy Típus: &b%type%");
            this.put("command-stacktool-info-block-type", "&eBlokk Típus: &b%type%");
            this.put("command-stacktool-info-spawner-type", "&eSpawner Típus: &b%type%");
            this.put("command-stacktool-info-stack-size", "&eStack Méret: &b%amount%");

            this.put("#11", "Stacked Block GUI");
            this.put("gui-stacked-block-title", "%name% Stack Szerkesztése");
            this.put("gui-stacked-block-page-back", List.of("&eElőző Oldal (" + GuiUtil.PREVIOUS_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-page-forward", List.of("&eKövetkező Oldal (" + GuiUtil.NEXT_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-destroy", List.of("&cStack Törlése", "&eTörli a stacket és kidobja a tárgyakat"));
            this.put("gui-stacked-block-destroy-title", "Blokk Stack Törlése?");
            this.put("gui-stacked-block-destroy-confirm", List.of("&aElfogad", "&eIgen, törlöm a stacket"));
            this.put("gui-stacked-block-destroy-cancel", List.of("&cMégse", "&eNem, vissza az előző képernyőre"));

            this.put("#12", "Stacked Spawner GUI");
            this.put("gui-stacked-spawner-title", "%name% Átnézés");
            this.put("gui-stacked-spawner-stats", "&6Spawner Statisztika");
            this.put("gui-stacked-spawner-min-spawn-delay", "&eMin Spawnolás Késleltetés: &b%delay%");
            this.put("gui-stacked-spawner-max-spawn-delay", "&eMax Spawnolás Késleltetés: &b%delay%");
            this.put("gui-stacked-spawner-disabled-mob-ai", "&eSzörny AI Kikapcsolva: &b%disabled%");
            this.put("gui-stacked-spawner-entity-search-range", "&eEntitás Keresés Hatótáv: &b%range%");
            this.put("gui-stacked-spawner-player-activation-range", "&eJátékos Aktiválási Hatótáv: &b%range%");
            this.put("gui-stacked-spawner-spawn-range", "&eSpawnolási Hatótáv: &b%range%");
            this.put("gui-stacked-spawner-min-spawn-amount", "&eMin Spawnolás Mennyiség: &b%amount%");
            this.put("gui-stacked-spawner-max-spawn-amount", "&eMax Spawnolás Mennyiség: &b%amount%");
            this.put("gui-stacked-spawner-spawn-amount", "&eSpawnolás Mennyiség: &b%amount%");
            this.put("gui-stacked-spawner-spawn-conditions", "&6Spawnolás Feltételek");
            this.put("gui-stacked-spawner-time-until-next-spawn", "&eIdő a következő spawnolásig: &b%time% tick");
            this.put("gui-stacked-spawner-total-spawns", "&eÖsszesen spawnolt szörnyek: &b%amount%");
            this.put("gui-stacked-spawner-valid-spawn-conditions", "&6Érvényes Spawnolási Feltételek");
            this.put("gui-stacked-spawner-invalid-spawn-conditions", "&6Érvénytelen Spawnolási Feltételek");
            this.put("gui-stacked-spawner-entities-can-spawn", "&aEntitások tudnak spawnolni");
            this.put("gui-stacked-spawner-conditions-preventing-spawns", "&eFeltételek akadályozzák a spawnolást:");

            this.put("#13", "Spawn Condition Messages");
            this.put("spawner-condition-invalid", "&7 - &c%message%");
            this.put("spawner-condition-info", "&e%condition%");
            this.put("spawner-condition-single", "&e%condition%: &b%value%");
            this.put("spawner-condition-list", "&e%condition%:");
            this.put("spawner-condition-list-item", "&7 - &b%message%");
            this.put("spawner-condition-above-sea-level-info", "Tenger Szint Felett");
            this.put("spawner-condition-above-sea-level-invalid", "Nincs spawnolási terület tenger szint felett");
            this.put("spawner-condition-above-y-axis-info", "Y tengely felett");
            this.put("spawner-condition-above-y-axis-invalid", "Nincs spawnolási terület a szükséges Y tengely felett");
            this.put("spawner-condition-air-info", "Nyitott Tér");
            this.put("spawner-condition-air-invalid", "Nincs elég nagy tér");
            this.put("spawner-condition-below-sea-level-info", "Tenger Szint Alatt");
            this.put("spawner-condition-below-sea-level-invalid", "Nincs spawnolási terület tenger szint alatt");
            this.put("spawner-condition-below-y-axis-info", "Y tengely alatt");
            this.put("spawner-condition-below-y-axis-invalid", "Nincs spawnolási terület a szükséges Y tengely alatt");
            this.put("spawner-condition-biome-info", "Biome");
            this.put("spawner-condition-biome-invalid", "Érvénytelen biome");
            this.put("spawner-condition-block-info", "Spawn Blokk");
            this.put("spawner-condition-block-invalid", "Nincs érvényes spawn blokk");
            this.put("spawner-condition-block-exception-info", "Spawn Blokk Kivétel");
            this.put("spawner-condition-block-exception-invalid", "Érvénytelen kivételes spawn blokkok");
            this.put("spawner-condition-darkness-info", "Alacsony Fény Szint");
            this.put("spawner-condition-darkness-invalid", "Terület túl fényes");
            this.put("spawner-condition-total-darkness-info", "Teljes sötétség");
            this.put("spawner-condition-total-darkness-invalid", "A területen nem lehet fény");
            this.put("spawner-condition-fluid-info", "Folyadék Szükséges");
            this.put("spawner-condition-fluid-invalid", "Nincs közelben folyadék");
            this.put("spawner-condition-lightness-info", "Nagy Fény Szint");
            this.put("spawner-condition-lightness-invalid", "Terület túl sötét");
            this.put("spawner-condition-max-nearby-entities-info", "Max közeli entitások");
            this.put("spawner-condition-max-nearby-entities-invalid", "Túl sok közeli entitás");
            this.put("spawner-condition-no-skylight-access-info", "Természetes Fény Nélküli Blokk");
            this.put("spawner-condition-no-skylight-access-invalid", "Nincs spawn blokk természetes fény nélkül");
            this.put("spawner-condition-on-ground-info", "Földön");
            this.put("spawner-condition-on-ground-invalid", "Nincs tömör föld a közelben");
            this.put("spawner-condition-skylight-access-info", "Természetes Fény");
            this.put("spawner-condition-skylight-access-invalid", "Nincs spawn blokk természetes fénnyel");
            this.put("spawner-condition-none-invalid", "Maximum spawnolási próba száma átlépve");
            this.put("spawner-condition-not-player-placed-invalid", "Egy játékosnak kell elhelyeznie");

            this.put("#14", "Given Stack Item Lore");
            this.put("#15", "Note: This will appear in the lore of the items given from the '/rs give' command");
            this.put("stack-item-lore-spawner", new ArrayList<>());
            this.put("stack-item-lore-block", new ArrayList<>());
            this.put("stack-item-lore-entity", new ArrayList<>());

            this.put("#16", "Generic Command Messages");
            this.put("no-permission", "&cNincs jogod ehhez!");
            this.put("only-player", "&cEzt a parancsot csak játékosok tudják használni.");
            this.put("unknown-command", "&cIsmeretlen parancs, használd a &b/%cmd% help &cparancsot további információért.");
            this.put("unknown-command-error", "&cEgy ismeretlen hiba történt; a részletek a konzolba lettek írva. Kérlek lépj kapcsolatba a szerver adminisztrátorral!");
            this.put("invalid-subcommand", "&cHelytelen alparancs.");
            this.put("invalid-argument", "&cHelyetlen változó: %message%.");
            this.put("invalid-argument-null", "&cHelytelen változó: %name% hiányzik.");
            this.put("missing-arguments", "&cHiányos változók, &b%amount% &cszükséges.");
            this.put("missing-arguments-extra", "&cHiányos változók, &b%amount%+ &cszükséges.");

            this.put("#17", "Argument Handler Error Messages");
            this.put("argument-handler-enum", "%enum% típus [%input%] nem létezik");
            this.put("argument-handler-enum-list", "%enum% típus [%input%] nem létezik. Létező típusok: %types%");
            this.put("argument-handler-string", "A szöveg nem lehet üres");
            this.put("argument-handler-integer", "A számnak [%input%] teljes számnak kell lennie -2^31 és 2^31-1 között!");
            this.put("argument-handler-player", "Ilyen felhasználónévvel [%input%], online játékost nincs");
            this.put("argument-handler-stackplugin", "Ilyen névvel [%input%], nincs plugin");
            this.put("argument-handler-material", "Ilyen névvel [%input%] nincs ismert anyag");
            this.put("argument-handler-stackamount", "A stack méret [%input%] nem érvényes, 0-nál nagyobb számnak kell lennie");
            this.put("argument-handler-stacktype", "Stack méret [%input%] nem érvényes");
            this.put("argument-handler-translationlocale", "A fordítási szín [%input%] nem érvényes");

            this.put("#19", "Misc Messages");
            this.put("spawner-silk-touch-protect", "&cFigyelmeztetés! &eSzükséged van egy Gyengéd Érintéses csákányra és/vagy legyen jogod felszedni anélkül a spawnert. Egyébként nem fogod tudni felszedni.");
            this.put("spawner-advanced-place-no-permission", "&cFigyelmeztetés! &eNincs jogod lerakni ezt a típusú spawnert.");
            this.put("spawner-advanced-break-no-permission", "&cFigyelmeztetés! &eNincs jogod felszedni ezt a típusú spawnert.");
            this.put("spawner-advanced-break-silktouch-no-permission", "&cFigyelmeztetés! &eSzükséged van egy Gyengéd Érintéses csákányra hogy felszed ezt a típusu spawnert.");
            this.put("spawner-convert-not-enough", "&cFigyelmeztetés! &eNem sikerült megváltoztatni a spawnert az Idéző Tojásokkal. Nem fogsz a kezedben elegendő mennyiségű Idéző Tojást.");
            this.put("number-separator", ",");
            this.put("silktouch-chance-placeholder", "%chance%%");
        }};
    }
}
