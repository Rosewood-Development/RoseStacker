package dev.rosewood.rosestacker.locale;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.locale.Locale;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConversionManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class GermanLocale implements Locale {

    @Override
    public String getLocaleName() {
        return "de_DE";
    }

    @Override
    public String getTranslatorName() {
        return "morose";
    }

    @Override
    public Map<String, Object> getDefaultLocaleValues() {
        return new LinkedHashMap<String, Object>() {{
            this.put("#0", "Plugin Nachricht Prefix");
            this.put("prefix", "&7[<g:#8A2387:#E94057:#F27121>RoseStacker&7] ");

            this.put("#1", "Stack Anzeige-Tags");
            this.put("entity-stack-display", "&c%amount%x &7%name%");
            this.put("entity-stack-display-custom-name", "%name% &7[&c%amount%x&7]");
            this.put("entity-stack-display-spawn-egg", "&c%amount%x &7%name% Spawn Ei");
            this.put("item-stack-display", "&c%amount%x &7%name%");
            this.put("item-stack-display-single", "&7%name%");
            this.put("block-stack-display", "&c%amount%x &7%name%");
            this.put("spawner-stack-display", "&c%amount%x &7%name%");
            this.put("spawner-stack-display-single", "&7%name%");

            this.put("#2", "Base Command Message");
            this.put("base-command-color", "&e");
            this.put("base-command-help", "&eNutze &b/rs help &efür Befehl-Informationen.");

            this.put("#3", "Help Command");
            this.put("command-help-description", "&8 - &d/rs help &7- Zeigt das Hilfe-Menü an... du bist angekommen!");
            this.put("command-help-title", "&eVerfügbare Befehle:");

            this.put("#4", "Reload Command");
            this.put("command-reload-description", "&8 - &d/rs reload &7- Läd das Plugin neu");
            this.put("command-reload-reloaded", "&ePlugin Daten, Konfiguration, und Sprachdateien wurden neugeladen.");

            this.put("#5", "Give Command");
            this.put("command-give-description", "&8 - &d/rs give &7- Gibt vor-gestackte Gegenstände");
            this.put("command-give-usage", "&cNutzen: &e/rs give <block|spawner|entity> <Spieler> <typ> [stackGröße] [anzahl]");
            this.put("command-give-given", "&eHat &b%player% &e[%display%&e] gegeben.");
            this.put("command-give-given-multiple", "&eHat &b%player% &e%amount%x [%display%&e] gegeben.");
            this.put("command-give-unstackable", "&cDer Typ den du angegeben hast ist nicht Stackbar.");
            this.put("command-give-too-large", "&cDie anzahl die du angegeben hast überschreitet die Maximale Stack Anzahl für diesen Typen.");

            this.put("#6", "Clearall Command");
            this.put("command-clearall-description", "&8 - &d/rs clearall &7- Entfernt alle von einem Stacktypen");
            this.put("command-clearall-killed-entities", "&b%amount% &eEntity stacks wurden entfernt.");
            this.put("command-clearall-killed-items", "&b%amount% &eGegenstand stacks wurden entfernt.");
            this.put("command-clearall-killed-all", "&b%entityAmount% &eEntity stacks und &b%itemAmount% &eItem Stacks wurden entfernt.");

            this.put("#7", "Stats Command");
            this.put("command-stats-description", "&8 - &d/rs stats &7- Zeige Statistiken über das Plugin an");
            this.put("command-stats-header", "&aZurzeitige Plugin Statistiken:");
            this.put("command-stats-threads", "&b%amount% &eAktive Stacking Threads.");
            this.put("command-stats-stacked-entities", "&b%stackAmount% &eGeladene Entity stacks, insgesamt &b%total% &eEntities.");
            this.put("command-stats-stacked-items", "&b%stackAmount% &eloaded Gegenstand stacks, insgesamt &b%total% &eGegenstände.");
            this.put("command-stats-stacked-blocks", "&b%stackAmount% &eGeladene Block stacks, insgesamt &b%total% &eBlöcke.");
            this.put("command-stats-stacked-spawners", "&b%stackAmount% &eGeladene Spawner-Stacks, insgesamt &b%total% &eSpawner.");

            this.put("#8", "Convert Command");
            this.put("command-convert-description", "&8 - &d/rs convert &7- Daten von anderem Plugin Konvertieren");
            this.put("command-convert-converted", "&eDaten von &b%plugin% &ezu RoseStacker Konvertiert. Das Konvertierte Plugin wurde Deaktiviert. Stelle sicher das Konvertierte Plugin vom Plugins Ordner zu Entfernen.");
            this.put("command-convert-failed", "&cFehler beim Konvertieren von &b%plugin%&c, Plugin ist nicht aktiv.");
            this.put("command-convert-aborted", "&cKonvertierung des Plugins &b%plugin%&c abgebrochen. Du hast schon von einem anderen Stack-Plugin Konvertiert.");

            this.put("#9", "Translate Command");
            this.put("command-translate-description", "&8 - &d/rs translate &7- Übersetzt die Stack-Namen");
            this.put("command-translate-loading", "&eHerunterladen und anwenden der Übersetzungs-Daten, dies kann einen Moment dauern.");
            this.put("command-translate-failure", "&cNicht im Stande die Stack-Namen zu Übersetzen. Es gab ein problem deine Daten zu erhalten. Bitte versuche es später erneut.");
            this.put("command-translate-invalid-locale", "&cUnable to translate the stack names. The locale that du specified is invalid.");
            this.put("command-translate-spawner-format", "&eSpawner-Namen konnten nicht erfolgreich Übersetzt werden. Um dies zu beheben, kannst du folgendes nutzen: &b/rs translate de_DE &3{} " +
                    "Spawner &eum einen Spawner als \"Kuh Spawner\" anzuzeigen. Nutze  &b{} &eals Platzhalter für den Mob-Namen");
            this.put("command-translate-spawner-format-invalid", "&cDas Spawnerformat von dir ist untültig. Es muss &b{} &cBeinhalten für die Platzierung des Mob-Namen");
            this.put("command-translate-success", "&aErfolgreich Stack-Namen Übersetzt");

            this.put("#10", "Stacking Tool Command");
            this.put("command-stacktool-description", "&8 - &d/rs stacktool &7- Gibt einem Spieler das Stacktool");
            this.put("command-stacktool-given", "&eDir wurde das Stacktool gegeben.");
            this.put("command-stacktool-given-other", "&b%player% &aHat das Stacktool erhalten.");
            this.put("command-stacktool-no-permission", "&cDu hast nicht die Berechtigungen das Stacktool zu nutzen.");
            this.put("command-stacktool-invalid-entity", "&cEntity gehört keinem Stack an, ist es ein Benutzerdefiniertes Mob?");
            this.put("command-stacktool-marked-unstackable", "&b%type% &eWurde als &cUnstackbar &egekennzeichnet.");
            this.put("command-stacktool-marked-stackable", "&b%type% &eWurde als &cStackbar &egekennzeichnet.");
            this.put("command-stacktool-marked-all-unstackable", "&eDer gesamte &b%type% &eStack wurde als &cUnstackbar &egekennzeichnet.");
            this.put("command-stacktool-select-1", "&eDer &b%type% &eWurde als Entity#1 ausgewählt. Wähle ein zweites Entity aus um zu testen ob diese Stacken können.");
            this.put("command-stacktool-unselect-1", "&eDer &b%type% &ewurde abgewählt.");
            this.put("command-stacktool-select-2", "&eDer &b%type% &eWurde als Entity #2 ausgewählt.");
            this.put("command-stacktool-can-stack", "&aEntity #1 Kann mit Entity #2 Stacken.");
            this.put("command-stacktool-can-not-stack", "&cEntity 1 Kann nicht mit Entity 2 Stacken. Grund: &b%reason%");
            this.put("command-stacktool-info", "&eStack Info:");
            this.put("command-stacktool-info-uuid", "&eUUID: &b%uuid%");
            this.put("command-stacktool-info-entity-id", "&eEntity ID: &b%id%");
            this.put("command-stacktool-info-custom-name", "&eBenutzerdefinierter Name: &r%name%");
            this.put("command-stacktool-info-location", "&eLage: X: &b%x% &eY: &b%y% &eZ: &b%z% &eWorld: &b%world%");
            this.put("command-stacktool-info-chunk", "&eChunk: &b%x%&e, &b%z%");
            this.put("command-stacktool-info-true", "&awahr");
            this.put("command-stacktool-info-false", "&cfalsch");
            this.put("command-stacktool-info-entity-type", "&eEntity Typ: &b%type%");
            this.put("command-stacktool-info-entity-stackable", "&eStackbar: %value%");
            this.put("command-stacktool-info-entity-has-ai", "&eHat AI: %value%");
            this.put("command-stacktool-info-entity-from-spawner", "&eVon Spawner: %value%");
            this.put("command-stacktool-info-item-type", "&eGegenstands Typ: &b%type%");
            this.put("command-stacktool-info-block-type", "&eBlock Typ: &b%type%");
            this.put("command-stacktool-info-spawner-type", "&eSpawner Typ: &b%type%");
            this.put("command-stacktool-info-stack-size", "&eStackgröße: &b%amount%");

            this.put("#11", "Stacked Block GUI");
            this.put("gui-stacked-block-title", "Editiere %name% Stack");
            this.put("gui-stacked-block-page-back", Collections.singletonList("&eVorherige Seite (" + GuiUtil.PREVIOUS_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-page-forward", Collections.singletonList("&eNächste Seite (" + GuiUtil.NEXT_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-destroy", Arrays.asList("&cDestroy Stack", "&eZerstört den Stack und lässt Gegenställe fallen"));
            this.put("gui-stacked-block-destroy-title", "Block Stack zerstören?");
            this.put("gui-stacked-block-destroy-confirm", Arrays.asList("&aConfirm", "&eJa, Stack zerstören"));
            this.put("gui-stacked-block-destroy-cancel", Arrays.asList("&cCancel", "&eNein, zurück zum Screen davor"));

            this.put("#12", "Stacked Spawner GUI");
            this.put("gui-stacked-spawner-title", "Beobachte %name%");
            this.put("gui-stacked-spawner-stats", "&6Spawner Statistiken");
            this.put("gui-stacked-spawner-min-spawn-delay", "&eMinimale Spawn verzögerung: &b%delay%");
            this.put("gui-stacked-spawner-max-spawn-delay", "&eMaximale Spawn verzögerung: &b%delay%");
            this.put("gui-stacked-spawner-disabled-mob-ai", "&eDeaktivierte Mob AI: &b%disabled%");
            this.put("gui-stacked-spawner-entity-search-range", "&eEntity Such-Bereich: &b%range%");
            this.put("gui-stacked-spawner-player-activation-range", "&eSpieleraktivierungsbereich: &b%range%");
            this.put("gui-stacked-spawner-spawn-range", "&eSpawn Reichweite: &b%range%");
            this.put("gui-stacked-spawner-min-spawn-amount", "&eMinimale Spawn Anzahl: &b%amount%");
            this.put("gui-stacked-spawner-max-spawn-amount", "&eMaximale Spawn Anzahl: &b%amount%");
            this.put("gui-stacked-spawner-spawn-amount", "&eSpawn Anzahl: &b%amount%");
            this.put("gui-stacked-spawner-spawn-conditions", "&6Spawn Konditionen");
            this.put("gui-stacked-spawner-time-until-next-spawn", "&eZeit bis zum nächsten Spawn: &b%time% ticks");
            this.put("gui-stacked-spawner-total-spawns", "&eInsgesamt Gespawnte Mobs: &b%amount%");
            this.put("gui-stacked-spawner-valid-spawn-conditions", "&6Valide Konditionen zum Spawnen");
            this.put("gui-stacked-spawner-invalid-spawn-conditions", "&6Invalide Konditionen zum Spawnen");
            this.put("gui-stacked-spawner-entities-can-spawn", "&aEntities können Spawnen");
            this.put("gui-stacked-spawner-conditions-preventing-spawns", "&eKonditionen die Mobspawns verhindern:");

            this.put("#13", "Spawn Condition Messages");
            this.put("spawner-condition-invalid", "&7 - &c%message%");
            this.put("spawner-condition-info", "&e%condition%");
            this.put("spawner-condition-single", "&e%condition%: &b%value%");
            this.put("spawner-condition-list", "&e%condition%:");
            this.put("spawner-condition-list-item", "&7 - &b%message%");
            this.put("spawner-condition-above-sea-level-info", "Über Meereshöhe");
            this.put("spawner-condition-above-sea-level-invalid", "Kein Spawnbereich über Meereshöhe");
            this.put("spawner-condition-above-y-axis-info", "Über Y-Achse");
            this.put("spawner-condition-above-y-axis-invalid", "Die Y Koordinate dieses Spawners ist zu niedrig");
            this.put("spawner-condition-air-info", "Offene Luft");
            this.put("spawner-condition-air-invalid", "Keine ausreichend großen Lufträume vorhanden");
            this.put("spawner-condition-below-sea-level-info", "Unter Meereshöhe");
            this.put("spawner-condition-below-sea-level-invalid", "Kein Spawnbereich unter Meereshöhe");
            this.put("spawner-condition-below-y-axis-info", "Unter Y-Achse");
            this.put("spawner-condition-below-y-axis-invalid", "Die Y Koordinate dieses Spawners ist zu hoch");
            this.put("spawner-condition-biome-info", "Biom");
            this.put("spawner-condition-biome-invalid", "Falsches Biom");
            this.put("spawner-condition-block-info", "Spawn Block");
            this.put("spawner-condition-block-invalid", "Keine validen Spawn Blöcke");
            this.put("spawner-condition-block-exception-info", "Spawn Block Fehler");
            this.put("spawner-condition-block-exception-invalid", "Ausgeschlossene Spawn Blöcke");
            this.put("spawner-condition-darkness-info", "Niedriges Licht Level");
            this.put("spawner-condition-darkness-invalid", "Bereich ist zu Hell");
            this.put("spawner-condition-fluid-info", "Benötigt Flüssigkeit");
            this.put("spawner-condition-fluid-invalid", "Keine Flüssigkeit in der Nähe");
            this.put("spawner-condition-lightness-info", "Hohes Licht Level");
            this.put("spawner-condition-lightness-invalid", "Gebiet zu Dunkel");
            this.put("spawner-condition-max-nearby-entities-info", "Maximale Entities in der Nähe");
            this.put("spawner-condition-max-nearby-entities-invalid", "Zu viele Entities in der Nähe");
            this.put("spawner-condition-no-skylight-access-info", "Kein Zugang zum Himmelslicht");
            this.put("spawner-condition-no-skylight-access-invalid", "Keine Spawn Blöcke ohne Zugang zum Himmelslicht");
            this.put("spawner-condition-on-ground-info", "Auf dem Boden");
            this.put("spawner-condition-on-ground-invalid", "Kein Solider Boden in der nähe");
            this.put("spawner-condition-skylight-access-info", "Zugang zum Himmelslicht");
            this.put("spawner-condition-skylight-access-invalid", "Keine Spawn Blöcke mit Zugang zum Himmelslicht");
            this.put("spawner-condition-none-invalid", "Maximale Spawn-Versuche überschritten");

            this.put("#14", "Lore der Items die durch /rs give gegeben werden");
            this.put("#15", "Bemerkung: Wenn du dies änderst werden Gegenstände mit der alten lore unbrauchbar.");
            this.put("stack-item-lore-stack-size", "&7Stack Größe: &c");
            this.put("stack-item-lore-entity-type", "&7Entity Typ: &c");
            this.put("stack-item-lore-block-type", "&7Block Typ: &c");
            this.put("stack-item-lore-spawner-type", "&7Spawner Typ: &c");

            this.put("#16", "ACF-Core Messages");
            this.put("acf-core-permission-denied", "&cDafür hast du nicht die benötigten Berechtigungen!");
            this.put("acf-core-permission-denied-parameter", "&cDu hast keine Berechtigungen dafür!");
            this.put("acf-core-error-generic-logged", "&cEin Fehler ist aufgetreten, bitte beim Ersteller des Plugins melden.");
            this.put("acf-core-error-performing-command", "&cEin Fehler ist beim ausführen dieses Befehls aufgekommen.");
            this.put("acf-core-unknown-command", "&cUnbekannter befehl. Nutze &b/rs&c für Befehle.");
            this.put("acf-core-invalid-syntax", "&cKorrekte nutzung: &e{command}&e {syntax}");
            this.put("acf-core-error-prefix", "&cFehler: {message}");
            this.put("acf-core-info-message", "&e{message}");
            this.put("acf-core-please-specify-one-of", "&cFehler: Das gegebene argumet ist ungültig.");
            this.put("acf-core-must-be-a-number", "&cFehler: &b{num}&c muss eine Nummer sein.");
            this.put("acf-core-must-be-min-length", "&cFehler: Muss mindestens &b{min}&c Zeichen lang sein.");
            this.put("acf-core-must-be-max-length", "&cFehler: Kann maximal &b{max}&c Zeichen lang sein.");
            this.put("acf-core-please-specify-at-most", "&cFehler: Bitte maximal einen Wert von &b{max} auswählen&c.");
            this.put("acf-core-please-specify-at-least", "&cFehler: Bitte mindestens einen Wert von &b{min} auswählen&c.");
            this.put("acf-core-not-allowed-on-console", "&cNur Spieler können diesen Befehl ausführen.");
            this.put("acf-core-could-not-find-player", "&cFehler: Ich konnte keinen Spieler mit dem Namen finden: &b{search}");
            this.put("acf-core-no-command-matched-search", "&cFehler: Keinen Befehl gefunden: &b{search}&c.");

            this.put("#17", "ACF-Minecraft Messages");
            this.put("acf-minecraft-no-player-found-server", "&cFehler: Ich konnte keinen Spieler mit dem Namen finden: &b{search}");
            this.put("acf-minecraft-is-not-a-valid-name", "&cFehler: &b{name} &cist kein Valider Spielername");

            this.put("#18", "Convert Lock Messages");
            this.put("convert-lock-conflictions", "&cAuf deinem Server gibt es Plugins die dafür bekannt sind probleme mit RoseStacker aufzubringen. " +
                    "Um konflikte und/oder Datenverlust zu verhindern hat RoseStacker ein oder mehrere Stack-Typen deaktiviert. " +
                    "Eine Datei wurde erstellt im verzeichnis: plugins/" + RoseStacker.getInstance().getName() + "/" + ConversionManager.FILE_NAME + " Wo du deine Deaktivierten Stack-Typen Konfigurieren kannst. " +
                    "Diese Datei wird dir ebenso die möglichkeit geben zu bestätigen dass du diese Warnung gelesen hast und somit diese Warnmeldung deaktivieren.");

            this.put("#19", "Misc Messages");
            this.put("spawner-silk-touch-protect", "&cWarnung! &eDu musst eine Spitzhacke mit Behutsamkeit nutzen und/oder die berechtigung Spawner aufzuheben. Andernfalls wird es nicht möglich sein");
            this.put("spawner-advanced-place-no-permission", "&cWarnung! &eDu hast nicht die nötigen berechtigungen um diesen Spawner zu plazieren.");
            this.put("spawner-advanced-break-no-permission", "&cWarnung! &eDu hast nicht die nötigen berechtigungen um diesen Spawner abzubauen.");
            this.put("spawner-advanced-break-silktouch-no-permission", "&cWarnung! &eDu musst eine Spitzhacke mit Behutsamkeit nutzen um den Spawner abzubauen.");
            this.put("spawner-convert-not-enough", "&cWarnung! &eUmwandlung nicht möglich. Du hast nicht genug spawn-eier um diese konversion zu machen.");
            this.put("number-separator", ",");
        }};
    }
}
