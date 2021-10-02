package dev.rosewood.rosestacker.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.locale.Locale;
import dev.rosewood.rosegarden.manager.AbstractLocaleManager;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.locale.DutchLocale;
import dev.rosewood.rosestacker.locale.EnglishLocale;
import dev.rosewood.rosestacker.locale.FilipinoLocale;
import dev.rosewood.rosestacker.locale.GermanLocale;
import dev.rosewood.rosestacker.locale.HungarianLocale;
import dev.rosewood.rosestacker.locale.RomanianLocale;
import dev.rosewood.rosestacker.locale.SimplifiedChineseLocale;
import dev.rosewood.rosestacker.locale.TraditionalChineseLocale;
import dev.rosewood.rosestacker.manager.LocaleManager.TranslationResponse.Result;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class LocaleManager extends AbstractLocaleManager {

    private List<String> translationLocales;

    public LocaleManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.translationLocales = new ArrayList<>();
    }

    @Override
    public List<Locale> getLocales() {
        return Arrays.asList(
                new DutchLocale(),
                new EnglishLocale(),
                new FilipinoLocale(),
                new GermanLocale(),
                new HungarianLocale(),
                new RomanianLocale(),
                new SimplifiedChineseLocale(),
                new TraditionalChineseLocale()
        );
    }

    /**
     * @return a map of acf-core messages and their values
     */
    public Map<String, String> getAcfCoreMessages() {
        return this.locale.getKeys(false).stream()
                .filter(x -> x.startsWith("acf-core"))
                .collect(Collectors.toMap(x -> x.replaceFirst("acf-core-", "").replaceAll("-", "_"), this.locale::getString));
    }

    /**
     * @return a map of acf-core minecraft messages and their values
     */
    public Map<String, String> getAcfMinecraftMessages() {
        return this.locale.getKeys(false).stream()
                .filter(x -> x.startsWith("acf-minecraft"))
                .collect(Collectors.toMap(x -> x.replaceFirst("acf-minecraft-", "").replaceAll("-", "_"), this.locale::getString));
    }

    /**
     * Gets a gui locale message with the given placeholders applied
     *
     * @param messageKey The key of the message to get
     * @param stringPlaceholders The placeholders to apply
     * @return The locale message with the given placeholders applied
     */
    public List<String> getGuiLocaleMessage(String messageKey, StringPlaceholders stringPlaceholders) {
        List<String> message = this.locale.getStringList(messageKey);
        if (message.isEmpty())
            message.add(ChatColor.RED + "Missing message in locale file: " + messageKey);
        message.replaceAll(x -> HexUtils.colorify(stringPlaceholders.apply(x)));
        return message;
    }

    public void fetchMinecraftTranslationLocales() {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            String version;
            if (NMSUtil.getVersionNumber() >= 16) {
                version = StackerUtils.MAX_SUPPORTED_LOCALE_VERSION;
            } else {
                version = "1.15.2";
            }

            DataManager dataManager = this.rosePlugin.getManager(DataManager.class);

            List<String> locales = dataManager.getTranslationLocales(version);
            if (!locales.isEmpty()) {
                this.translationLocales = locales;
                return;
            }

            String queryLink;
            if (Setting.MISC_USE_MIRROR_SITE_LANGS.getBoolean()) {
                queryLink = "https://cross.obfs.dev/repos/InventivetalentDev/minecraft-assets/contents/assets/minecraft/lang?ref=" + version; #Nope
            } else {
                queryLink = "https://api.github.com/repos/InventivetalentDev/minecraft-assets/contents/assets/minecraft/lang?ref=" + version;
            }
            
            try {
                URL url = new URL(queryLink);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

                JsonParser parser = new JsonParser();
                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                    JsonArray json = parser.parse(reader).getAsJsonArray();
                    for (JsonElement element : json) {
                        String name = element.getAsJsonObject().get("name").getAsString().replaceAll(Pattern.quote(".json"), "");
                        if (!name.startsWith("_")) // ignore _all.json and _list.json
                            locales.add(name);
                    }
                }
            } catch (Exception ignored) { }

            locales.sort(String::compareTo);

            this.translationLocales = locales;

            dataManager.saveTranslationLocales(version, locales);

            this.rosePlugin.getLogger().info("Fetched " + locales.size() + " translation locales.");
        });
    }

    public void getMinecraftTranslationValues(String locale, Consumer<TranslationResponse> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            Map<Material, String> materialValues = new EnumMap<>(Material.class);
            Map<EntityType, String> entityValues = new EnumMap<>(EntityType.class);

            String version;
            if (NMSUtil.getVersionNumber() >= 16) {
                version = StackerUtils.MAX_SUPPORTED_LOCALE_VERSION;
            } else {
                version = "1.15.2";
            }

            String fileLink;
            if (Setting.MISC_USE_MIRROR_SITE_LANGS.getBoolean()) {
                fileLink = "https://cdn.jsdelivr.net/gh/InventivetalentDev/minecraft-assets@" + version + "/assets/minecraft/lang/" + locale.toLowerCase() + ".json";
            } else {
                fileLink = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/" + version + "/assets/minecraft/lang/" + locale.toLowerCase() + ".json";
            }
            Result result;
            try {
                URL url = new URL(fileLink);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if (connection.getResponseCode() == 200) {
                    JsonParser parser = new JsonParser();
                    try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                        JsonObject json = parser.parse(reader).getAsJsonObject();
                        Set<Entry<String, JsonElement>> entrySet = json.entrySet();
                        Map<String, String> entries = entrySet.stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().getAsString()));

                        for (Material material : Material.values()) {
                            if (material.isLegacy())
                                continue;

                            String type = material.isBlock() ? "block" : "item";
                            String key = type + ".minecraft." + material.getKey().getKey();
                            String value = entries.get(key);
                            if (value == null)
                                value = StackerUtils.formatName(material.name());

                            materialValues.put(material, value);
                        }

                        for (EntityType entityType : EntityType.values()) {
                            if (entityType == EntityType.UNKNOWN)
                                continue;

                            String key = "entity.minecraft." + entityType.getKey().getKey();
                            String value = entries.get(key);
                            if (value == null)
                                value = StackerUtils.formatName(entityType.name());

                            entityValues.put(entityType, value);
                        }
                    }

                    result = Result.SUCCESS;
                } else if (connection.getResponseCode() == 404) {
                    result = Result.INVALID_LOCALE;
                } else {
                    result = Result.FAILURE;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                result = Result.FAILURE;
            }

            callback.accept(new TranslationResponse(materialValues, entityValues, result));
        });
    }

    public List<String> getPossibleTranslationLocales() {
        return this.translationLocales;
    }

    public static class TranslationResponse {

        public enum Result {
            SUCCESS,
            INVALID_LOCALE,
            FAILURE
        }

        private final Map<Material, String> materialValues;
        private final Map<EntityType, String> entityValues;
        private final Result result;

        private TranslationResponse(Map<Material, String> materialValues, Map<EntityType, String> entityValues, Result result) {
            this.materialValues = materialValues;
            this.entityValues = entityValues;
            this.result = result;
        }

        public Map<Material, String> getMaterialValues() {
            return this.materialValues;
        }

        public Map<EntityType, String> getEntityValues() {
            return this.entityValues;
        }

        public Result getResult() {
            return this.result;
        }

    }

}
