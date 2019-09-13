package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LocaleManager extends Manager {

    public enum Locale {
        PREFIX,

        ENTITY_STACK_DISPLAY,
        ITEM_STACK_DISPLAY,
        BLOCK_STACK_DISPLAY,
        SPAWNER_STACK_DISPLAY,

        COMMAND_RELOAD_DESCRIPTION,
        COMMAND_RELOAD_RELOADED,

        ACF_CORE;

        private String message;

        /**
         * Gets a Locale message
         *
         * @return A message formatted for chat
         */
        public String get() {
            if (this.message == null)
                this.loadMessage();
            return this.message;
        }

        public ConfigurationSection getConfigurationSection() {
            return RoseStacker.getInstance().getLocaleManager().getLocale().getConfigurationSection(this.getNameAsKey());
        }

        /**
         * Loads the locale message and caches it
         */
        private void loadMessage() {
            String message = RoseStacker.getInstance().getLocaleManager().getLocale().getString(this.getNameAsKey());
            if (message != null)
                this.message = ChatColor.translateAlternateColorCodes('&', message);
        }

        /**
         * Resets the cached message
         */
        private void reset() {
            this.message = null;
        }

        /**
         * Gets the name of this Setting as a FileConfiguration-compatible key
         *
         * @return The key for a FileConfiguration
         */
        private String getNameAsKey() {
            return this.name().replace("_", "-").toLowerCase();
        }
    }

    public LocaleManager(RoseStacker roseStacker) {
        super(roseStacker);
    }

    private FileConfiguration locale;

    @Override
    public void reload() {
        for (Locale value : Locale.values())
            value.reset();

        String targetLocaleName = ConfigurationManager.Setting.LOCALE.getString() + ".lang";
        File targetLocaleFile = new File(this.roseStacker.getDataFolder() + "/locale", targetLocaleName);
        if (!targetLocaleFile.exists()) {
            targetLocaleFile = new File(this.roseStacker.getDataFolder() + "/locale", "en_US.lang");
            if (!targetLocaleFile.exists())
                this.roseStacker.saveResource("locale/en_US.lang", false);
        }

        this.locale = YamlConfiguration.loadConfiguration(targetLocaleFile);
    }

    @Override
    public void disable() {

    }

    /**
     * Gets the FileConfiguration that contains the locale messages
     *
     * @return A FileConfiguration of the messages
     */
    public FileConfiguration getLocale() {
        return this.locale;
    }

    /**
     * Sends a message to a CommandSender with the prefix
     *
     * @param sender The CommandSender to send to
     * @param locale The Locale to send
     */
    public void sendPrefixedMessage(CommandSender sender, Locale locale) {
        sender.sendMessage(Locale.PREFIX.get() + locale.get());
    }

    /**
     * Sends a message to a CommandSender
     *
     * @param sender The CommandSender to send to
     * @param locale The Locale to send
     */
    public void sendMessage(CommandSender sender, Locale locale) {
        sender.sendMessage(locale.get());
    }

    /**
     * Saves a locale to disk in the /locale folder if it doesn't already exist
     * If it does exist, it checks to see if anything needs to be updated
     *
     * @param fileUrl The URL of the file to download
     * @param fileName The name of the file to save
     */
    public static void saveDefaultLocale(URL fileUrl, String fileName) {
        File localeFolder = new File(RoseStacker.getInstance().getDataFolder() + "/locale");
        if (!localeFolder.exists())
            localeFolder.mkdirs();

        File targetFile = new File(localeFolder, fileName);
        if (targetFile.exists()) {
            checkExistingFile(fileUrl, targetFile);
            return;
        }

        try (OutputStream outputStream = new FileOutputStream(targetFile)) {
            copy(fileUrl.openStream(), outputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Checks and updates a locale file with additions if any exist
     *
     * @param fileUrl The URL of the file to download
     * @param targetFile The target file
     */
    private static void checkExistingFile(URL fileUrl, File targetFile) {
        RoseStacker roseStacker = RoseStacker.getInstance();

        List<String> keysToUpdate = new ArrayList<>();
        FileConfiguration existingConfiguration = YamlConfiguration.loadConfiguration(targetFile);
        for (Locale locale : Locale.values())
            if (existingConfiguration.get(locale.getNameAsKey()) == null)
                keysToUpdate.add(locale.getNameAsKey());

        if (keysToUpdate.isEmpty())
            return;

        try (Reader reader = new InputStreamReader(fileUrl.openStream());
             BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, true))) {
            FileConfiguration newFileConfiguration = YamlConfiguration.loadConfiguration(reader);

            writer.newLine();
            writer.newLine();
            writer.write("# Changes since " + roseStacker.getName() + " v" + roseStacker.getDescription().getVersion());

            for (String key : keysToUpdate) {
                writer.newLine();
                writer.write(key + ": " + "\"" + newFileConfiguration.getString(key) + "\"");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Copies a file from an InputStream to an OutputStream
     *
     * @param input The InputStream to copy
     * @param output The OutputStream to copy to
     */
    private static void copy(InputStream input, OutputStream output) {
        try {
            byte[] buffer = new byte[1024 * 4];
            int n;
            while ((n = input.read(buffer)) != -1)
                output.write(buffer, 0, n);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
