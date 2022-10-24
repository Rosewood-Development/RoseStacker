package dev.rosewood.rosestacker.locale;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.locale.Locale;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConversionManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DutchLocale implements Locale {

    @Override
    public String getLocaleName() {
        return "nl_NL";
    }

    @Override
    public String getTranslatorName() {
        return "Suntrux";
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

            this.put("#2", "Base Command Message");
            this.put("base-command-color", "&e");
            this.put("base-command-help", "&eUse &b/rs help &evoor commando informatie.");

            this.put("#3", "Help Command");
            this.put("command-help-description", "&8 - &d/rs help &7- Weergeeft het help menu... Je bent gearriveerd!");
            this.put("command-help-title", "&eBeschikbare commando's:");

            this.put("#4", "Reload Command");
            this.put("command-reload-description", "&8 - &d/rs reload &7- Herlaad de plugin");
            this.put("command-reload-reloaded", "&ePlugin data, configuration, and locale files were reloaded.");

            this.put("#5", "Give Command");
            this.put("command-give-description", "&8 - &d/rs give &7- Geef vooraf gestackte items");
            this.put("command-give-usage", "&cUsage: &e/rs give <block|spawner|entity> <player> <type> [stackGrote] [hoeveelheid]");
            this.put("command-give-given", "&eGave &b%player% &e[%display%&e].");
            this.put("command-give-given-multiple", "&eGave &b%player% &e%amount%x [%display%&e].");
            this.put("command-give-unstackable", "&cHet type dat je hebt ingegeven is niet stackbaar.");
            this.put("command-give-too-large", "&cDe hoeveelheid dat je hebt ingegeven overschrijd de maximum stackgrootte.");

            this.put("#6", "Clearall Command");
            this.put("command-clearall-description", "&8 - &d/rs clearall &7- Wist alles van een type stack");
            this.put("command-clearall-killed-entities", "&eCleared &b%amount% &eentity stacks.");
            this.put("command-clearall-killed-items", "&eCleared &b%amount% &eitem stacks.");
            this.put("command-clearall-killed-all", "&eCleared &b%entityAmount% &eentity stacks en &b%itemAmount% &eitem stacks.");

            this.put("#7", "Stats Command");
            this.put("command-stats-description", "&8 - &d/rs stats &7- Geef statistieken van de plugin weer");
            this.put("command-stats-header", "&aHuidige plugin statistieken:");
            this.put("command-stats-threads", "&b%amount% &eActieve stacking threads.");
            this.put("command-stats-stacked-entities", "&b%stackAmount% &eEntity stacks geladen, totaal van &b%total% &eentities.");
            this.put("command-stats-stacked-items", "&b%stackAmount% &eItem stacks geladen, totaal van &b%total% &eitems.");
            this.put("command-stats-stacked-blocks", "&b%stackAmount% &eBlok stacks geladen, totaal van &b%total% &eblocks.");
            this.put("command-stats-stacked-spawners", "&b%stackAmount% &eSpawner stacks geladen, totaal van &b%total% &espawners.");

            this.put("#8", "Convert Command");
            this.put("command-convert-description", "&8 - &d/rs convert &7- Converteert data van een andere plugin");
            this.put("command-convert-converted", "&eData van &b%plugin% &eto RoseStacker geconverteerd. De geconverteerde data is uigeschakeld. Zorg zeker dat je de geconverteerde plugin uit je plugin folder verwijderd.");
            this.put("command-convert-failed", "&cConverteren mislukt &b%plugin%&c, de plugin is niet ingeschakeld.");
            this.put("command-convert-aborted", "&cPogin tot converteren geanuleerd &b%plugin%&c. Je hebt al geconverteerd van een andere stack plugin.");

            this.put("#9", "Translate Command");
            this.put("command-translate-description", "&8 - &d/rs translate &7- Vertaald de stack namen");
            this.put("command-translate-loading", "&eDownloaden en implementeren van stack vertaling data, dit kan even duren.");
            this.put("command-translate-failure", "&cStack data vertalen was niet mogelijk. Er was een probleem met het ophalen van de lokale data. Probeer het later opnieuw.");
            this.put("command-translate-invalid-locale", "&cNiet mogelijk om stack namen te vertalen. De taal die je hebt opgegeven is ongeldig.");
            this.put("command-translate-spawner-format", "&eSpawner namen kunnen niet accuraat worden vertaald. Om dit op te lossen, kan je &b/rs translate en_us &3{} Spawner &egebruiken om een spawner als \"Cow Spawner\" te laten verschijnen. Gebruik &b{} &eals een placeholder voor de mob.");
            this.put("command-translate-spawner-format-invalid", "&cHet spawner formaat dat je hebt ingegeven is ongeldig. Het moet &b{} &cbevatten voor de mob naam plaats.");
            this.put("command-translate-success", "&aStack namen zijn succesvol vertaald.");

            this.put("#10", "Stacking Tool Command");
            this.put("command-stacktool-description", "&8 - &d/rs stacktool &7- Geeft de speler het stack tool");
            this.put("command-stacktool-given", "&eJe hebt het stacking tool ontvangen.");
            this.put("command-stacktool-given-other", "&b%player% &aheeft het stacking tool ontvangen.");
            this.put("command-stacktool-no-permission", "&cJe hebt geen permissie om het stacking tool te gebruiken.");
            this.put("command-stacktool-invalid-entity", "&cDeze entity is geen deel van een stack, is het een custom mob?");
            this.put("command-stacktool-marked-unstackable", "&eDe &b%type% &eis gemarkeerd als &cunstackable&e.");
            this.put("command-stacktool-marked-stackable", "&eDe &b%type% &eIs gemarkeerd als &astackable&e.");
            this.put("command-stacktool-marked-all-unstackable", "&eDe volledige &b%type% &estack is gemarkeerd als &cunstackable&e.");
            this.put("command-stacktool-select-1", "&eDe &b%type% &eis geselecteerd als Entity #1. Selecteer een andere enitity om te kijken of het kan stacken.");
            this.put("command-stacktool-unselect-1", "&eDe &b%type% &eis gedeselecteerd.");
            this.put("command-stacktool-select-2", "&eDe &b%type% &eis geselecteerd als Entity #2.");
            this.put("command-stacktool-can-stack", "&aEntity #1 kan stacken met Entity #2.");
            this.put("command-stacktool-can-not-stack", "&cEntity 1 kan niet stacken met Entity 2. Reden: &b%reason%");
            this.put("command-stacktool-info", "&eStack Informatie:");
            this.put("command-stacktool-info-uuid", "&eUUID: &b%uuid%");
            this.put("command-stacktool-info-entity-id", "&eEntity ID: &b%id%");
            this.put("command-stacktool-info-custom-name", "&eCustom Naam: &r%name%");
            this.put("command-stacktool-info-location", "&eLocation: X: &b%x% &eY: &b%y% &eZ: &b%z% &eWereld: &b%world%");
            this.put("command-stacktool-info-chunk", "&eChunk: &b%x%&e, &b%z%");
            this.put("command-stacktool-info-true", "&atrue");
            this.put("command-stacktool-info-false", "&cfalse");
            this.put("command-stacktool-info-entity-type", "&eEntity soort: &b%type%");
            this.put("command-stacktool-info-entity-stackable", "&eStackbaar: %value%");
            this.put("command-stacktool-info-entity-has-ai", "&eHeeft AI: %value%");
            this.put("command-stacktool-info-entity-from-spawner", "&eVan Spawner: %value%");
            this.put("command-stacktool-info-item-type", "&eItem soort: &b%type%");
            this.put("command-stacktool-info-block-type", "&eBlok soort: &b%type%");
            this.put("command-stacktool-info-spawner-type", "&eSpawner soort: &b%type%");
            this.put("command-stacktool-info-stack-size", "&eStack grootte: &b%amount%");

            this.put("#11", "Stacked Block GUI");
            this.put("gui-stacked-block-title", "Wijzigen van %name% Stack");
            this.put("gui-stacked-block-page-back", List.of("&eVorige pagina (" + GuiUtil.PREVIOUS_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-page-forward", List.of("&eVolgende pagina (" + GuiUtil.PREVIOUS_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-destroy", List.of("&cStack slopen", "&eDit sloopt de stack en laat de items vallen"));
            this.put("gui-stacked-block-destroy-title", "Block stack slopen?");
            this.put("gui-stacked-block-destroy-confirm", List.of("&aBevestigen", "&eJaa, sloop de stack"));
            this.put("gui-stacked-block-destroy-cancel", List.of("&cAnnuleer", "&eNee, keer terug naar het vorige scherm"));

            this.put("#12", "Stacked Spawner GUI");
            this.put("gui-stacked-spawner-title", "Bekijken van %name%");
            this.put("gui-stacked-spawner-stats", "&6Spawner Statistieken");
            this.put("gui-stacked-spawner-min-spawn-delay", "&eMin Spawn Vertraging: &b%delay%");
            this.put("gui-stacked-spawner-max-spawn-delay", "&eMax Spawn Vertraging: &b%delay%");
            this.put("gui-stacked-spawner-disabled-mob-ai", "&eUitgeschakelde Mob AI: &b%disabled%");
            this.put("gui-stacked-spawner-entity-search-range", "&eEntity Zoek Afstand: &b%range%");
            this.put("gui-stacked-spawner-player-activation-range", "&eSpeler activatie afstand: &b%range%");
            this.put("gui-stacked-spawner-spawn-range", "&eSpawn afstand: &b%range%");
            this.put("gui-stacked-spawner-min-spawn-amount", "&eMin Spawn hoeveelheid: &b%amount%");
            this.put("gui-stacked-spawner-max-spawn-amount", "&eMax Spawn hoeveelheid: &b%amount%");
            this.put("gui-stacked-spawner-spawn-amount", "&eSpawn hoeveelheid: &b%amount%");
            this.put("gui-stacked-spawner-spawn-conditions", "&6Spawn Conditie");
            this.put("gui-stacked-spawner-time-until-next-spawn", "&eTijd tot de volgende spawn: &b%time% ticks");
            this.put("gui-stacked-spawner-total-spawns", "&eTotale mobs gespawnd: &b%amount%");
            this.put("gui-stacked-spawner-valid-spawn-conditions", "&6Geldige spawn conditie");
            this.put("gui-stacked-spawner-invalid-spawn-conditions", "&6Ongeldige spawn conditie");
            this.put("gui-stacked-spawner-entities-can-spawn", "&aEntities kunnen spawnen");
            this.put("gui-stacked-spawner-conditions-preventing-spawns", "&eConditie's die spawnen voorkomen:");

            this.put("#13", "Spawn Condition Messages");
            this.put("spawner-condition-invalid", "&7 - &c%message%");
            this.put("spawner-condition-info", "&e%condition%");
            this.put("spawner-condition-single", "&e%condition%: &b%value%");
            this.put("spawner-condition-list", "&e%condition%:");
            this.put("spawner-condition-list-item", "&7 - &b%message%");
            this.put("spawner-condition-above-sea-level-info", "Boven zee level");
            this.put("spawner-condition-above-sea-level-invalid", "Geen spawn arena boven zee level");
            this.put("spawner-condition-above-y-axis-info", "Boven Y-Axis");
            this.put("spawner-condition-above-y-axis-invalid", "Geen spawn arena boven nodige Y-Axis");
            this.put("spawner-condition-air-info", "Open lucht");
            this.put("spawner-condition-air-invalid", "Geen groot genoege open plek beschikbaar");
            this.put("spawner-condition-below-sea-level-info", "Onder zee level");
            this.put("spawner-condition-below-sea-level-invalid", "Geen spawn arena onder zee level");
            this.put("spawner-condition-below-y-axis-info", "Onder Y-Axis");
            this.put("spawner-condition-below-y-axis-invalid", "Geen spawn arena onder nodige Y-Axis");
            this.put("spawner-condition-biome-info", "Biome");
            this.put("spawner-condition-biome-invalid", "Foute biome");
            this.put("spawner-condition-block-info", "Spawn Blok");
            this.put("spawner-condition-block-invalid", "Geen geldige blocks");
            this.put("spawner-condition-block-exception-info", "Spawn Block Uitzondering");
            this.put("spawner-condition-block-exception-invalid", "Uitgezonderde spawn blocks");
            this.put("spawner-condition-darkness-info", "Laag licht level");
            this.put("spawner-condition-darkness-invalid", "Arena is te licht");
            this.put("spawner-condition-total-darkness-info", "Totale Duisternis");
            this.put("spawner-condition-total-darkness-invalid", "Gebied mag geen licht hebben");
            this.put("spawner-condition-fluid-info", "Heeft vloeistof nodig");
            this.put("spawner-condition-fluid-invalid", "Geen vloeistof in de buurt");
            this.put("spawner-condition-lightness-info", "Hoog licht level");
            this.put("spawner-condition-lightness-invalid", "Arena is te donker");
            this.put("spawner-condition-max-nearby-entities-info", "Maximum entities in de buurt");
            this.put("spawner-condition-max-nearby-entities-invalid", "Te veel entities in de buurt");
            this.put("spawner-condition-no-skylight-access-info", "Geen toegang tot zonlicht");
            this.put("spawner-condition-no-skylight-access-invalid", "Geen spawn blokken zonder zonlicht toegang");
            this.put("spawner-condition-on-ground-info", "Op de grond");
            this.put("spawner-condition-on-ground-invalid", "Geen vaste grond in de buurt");
            this.put("spawner-condition-skylight-access-info", "Zonlicht toegang");
            this.put("spawner-condition-skylight-access-invalid", "Geen spawn blokken met zonlicht toegang");
            this.put("spawner-condition-none-invalid", "Maximum spawn pogingen overschreden");
            this.put("spawner-condition-not-player-placed-invalid", "Moet geplaatst worden door een speler");

            this.put("#14", "Given Stack Item Lore");
            this.put("#15", "Note: This will appear in the lore of the items given from the '/rs give' command");
            this.put("stack-item-lore-spawner", new ArrayList<>());
            this.put("stack-item-lore-block", new ArrayList<>());
            this.put("stack-item-lore-entity", new ArrayList<>());

            this.put("#16", "ACF-Core Messages");
            this.put("acf-core-permission-denied", "&cJe hebt hier geen toestemming voor!");
            this.put("acf-core-permission-denied-parameter", "&cJe hebt hier geen toestemming voor!");
            this.put("acf-core-error-generic-logged", "&cEr is een fout opgetreden. Geef dit doort aan de auteur.");
            this.put("acf-core-error-performing-command", "&cEr is een fout opgetreden tijden het uitvoeren van dit commando.");
            this.put("acf-core-unknown-command", "&cOnbekend commando. Use &b/rs&c voor commando's.");
            this.put("acf-core-invalid-syntax", "&cGebruik: &e{command}&e {syntax}");
            this.put("acf-core-error-prefix", "&cFout: {message}");
            this.put("acf-core-info-message", "&e{message}");
            this.put("acf-core-please-specify-one-of", "&cFout: Er was een ongeldig argument gegeven.");
            this.put("acf-core-must-be-a-number", "&cFout: &b{num}&c must be a number.");
            this.put("acf-core-must-be-min-length", "&cFout: Moet minstens &b{min}&c karakters lang zijn.");
            this.put("acf-core-must-be-max-length", "&cFout: Moet maximum &b{max}&c karakters lang zijn.");
            this.put("acf-core-please-specify-at-most", "&cFout: Geef een waarde van maximum &b{max}&c.");
            this.put("acf-core-please-specify-at-least", "&cFout: Geef een waarde van minimum &b{min}&c.");
            this.put("acf-core-not-allowed-on-console", "&cAlleen spelers mogen dit commando uitvoeren.");
            this.put("acf-core-could-not-find-player", "&cFout: Kon geen speler vinden met deze naam: &b{search}");
            this.put("acf-core-no-command-matched-search", "&cFout: Geen commando komt overeen met &b{search}&c.");

            this.put("#17", "ACF-Minecraft Messages");
            this.put("acf-minecraft-no-player-found-server", "&cFout: Kon geen speler vinden met de naam: &b{search}");
            this.put("acf-minecraft-is-not-a-valid-name", "&cFout: &b{name} &cis geen geldige speler naam.");

            this.put("#18", "Convert Lock Messages");
            this.put("convert-lock-conflictions", "&cEr zijn plugin op je server die gekend zijn om te rotzooien met RoseStacker. " +
                    "Om conflicten en data verlies te voorkomen, heeft RoseStacker 1 of meerder stack soorten uitgeschakeld. " +
                    "Een bestand is aangemaakt in plugins/" + RoseStacker.getInstance().getName() + "/" + ConversionManager.FILE_NAME + " waar je de uitgeschakelde stack soorten kan configureren. " +
                    "Dat bestand zal ook doorhebben dat je dit heb gelezen en stoppen met dit bericht weer te geven.");

            this.put("#19", "Misc Messages");
            this.put("spawner-silk-touch-protect", "&cWarning! &eJe moet een silk touch pickaxe gebruiken of je hebt geen permissie om spawner op te rapen. Anders kan je dit niet doen.");
            this.put("spawner-advanced-place-no-permission", "&cWaarschuwing! &eJe hebt geen toestemming om dit soort spawner te plaatsen.");
            this.put("spawner-advanced-break-no-permission", "&cWaarschuwing! &eJe hebt geen toestemming om dit soort spawner op te rapen.");
            this.put("spawner-advanced-break-silktouch-no-permission", "&cWaarschuwing! &eJe moet een silk touch pickaxe gebruiken om dit soort spawner op te rapen.");
            this.put("spawner-convert-not-enough", "&cWaarschuwing! &eSpawner converteren met spawn eggs niet mogelijk. Je hebt niet genoeg spawn eggs in je hand.");
            this.put("number-separator", ",");
            this.put("silktouch-chance-placeholder", "%chance%%");
        }};
    }
}
