package dev.esophose.rosestacker.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentedFileConfigurationHelper {

    private JavaPlugin plugin;

    /**
     * Manage custom configurations and files
     */
    public CommentedFileConfigurationHelper(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get new configuration
     *
     * @param file - Path to file
     * @return - New SimpleConfig
     */
    public CommentedFileConfiguration getNewConfig(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new CommentedFileConfiguration(this.getConfigContent(file), file, this.getCommentsNum(file), this.plugin);
    }

    /**
     * Adds header block to config
     *
     * @param file - Config file
     * @param header - Header lines
     */
    public void setHeader(File file, String[] header) {
        if (!file.exists())
            return;

        try {
            String currentLine;
            StringBuilder config = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));

            while ((currentLine = reader.readLine()) != null)
                config.append(currentLine).append("\n");

            reader.close();
            config.append("# +----------------------------------------------------+ #\n");

            for (String line : header) {
                if (line.length() > 50)
                    continue;

                int length = (50 - line.length()) / 2;
                StringBuilder finalLine = new StringBuilder(line);

                for (int i = 0; i < length; i++) {
                    finalLine.append(" ");
                    finalLine.reverse();
                    finalLine.append(" ");
                    finalLine.reverse();
                }

                if (line.length() % 2 != 0)
                    finalLine.append(" ");

                config.append("# < ").append(finalLine.toString()).append(" > #\n");
            }

            config.append("# +----------------------------------------------------+ #");

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(this.prepareConfigString(config.toString()));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read file and make comments SnakeYAML friendly
     *
     * @param file - Path to file
     * @return - File as Input Stream
     */
    public Reader getConfigContent(File file) {
        if (!file.exists())
            return null;

        try {
            int commentNum = 0;

            String addLine;
            String currentLine;
            String pluginName = this.getPluginName();

            StringBuilder whole = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));

            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.trim().startsWith("#")) {
                    addLine = (currentLine.replaceFirst("#", pluginName + "_COMMENT_" + commentNum + ": '") + "'");
                    whole.append(addLine).append("\n");
                    commentNum++;
                } else {
                    whole.append(currentLine).append("\n");
                }
            }

            String config = whole.toString();
            Reader configStream = new InputStreamReader(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)));

            reader.close();
            return configStream;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get comments from file
     *
     * @param file - File
     * @return - Comments number
     */
    private int getCommentsNum(File file) {
        if (!file.exists())
            return 0;

        try {
            int comments = 0;
            String currentLine;

            BufferedReader reader = new BufferedReader(new FileReader(file));

            while ((currentLine = reader.readLine()) != null)
                if (currentLine.trim().startsWith("#"))
                    comments++;

            reader.close();
            return comments;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String prepareConfigString(String configString) {
        boolean lastLine = false;
        boolean headerLine = false;

        String[] lines = configString.split("\n");
        StringBuilder config = new StringBuilder();

        for (String line : lines) {
            if (line.trim().startsWith(this.getPluginName() + "_COMMENT")) {
                int whitespaceIndex = line.indexOf(line.trim());
                String comment = line.substring(0, whitespaceIndex) + "#" + line.substring(line.indexOf(":") + 3, line.length() - 1);

                if (comment.trim().startsWith("# +-")) {
                    if (!headerLine) {
                        config.append(comment).append("\n");

                        lastLine = false;
                        headerLine = true;
                    } else {
                        config.append(comment).append("\n\n");

                        lastLine = false;
                        headerLine = false;
                    }
                } else {
                    String normalComment;

                    if (comment.trim().startsWith("#'")) {
                        normalComment = comment.substring(0, comment.length() - 1).replaceFirst("#'", "# ");
                    } else {
                        normalComment = comment;
                    }

                    normalComment = normalComment.replaceAll("''", "'");

                    if (!lastLine) {
                        config.append(normalComment).append("\n");
                    } else {
                        config.append("\n").append(normalComment).append("\n");
                    }

                    lastLine = false;
                }
            } else {
                config.append(line).append("\n");
                lastLine = true;
            }
        }

        return config.toString();
    }

    /**
     * Saves configuration to file
     *
     * @param configString - Config string
     * @param file - Config file
     * @param compactLines - If lines should forcefully be separated by only one newline character
     */
    public void saveConfig(String configString, File file, boolean compactLines) {
        String configuration = this.prepareConfigString(configString).replaceAll("\n\n", "\n");

        // Apply post-processing to config string to make it pretty
        StringBuilder stringBuilder = new StringBuilder();
        try (Scanner scanner = new Scanner(configuration)) {
            boolean lastLineHadContent = false;
            int lastCommentSpacing = -1;
            int lastLineSpacing = -1;
            boolean forceCompact = false;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                boolean lineHadContent = false;
                boolean lineWasComment = false;
                int commentSpacing = -1;
                int lineSpacing = line.indexOf(line.trim());

                if (line.trim().startsWith("#")) {
                    lineWasComment = true;
                    String trimmed = line.trim().replaceFirst("#", "");
                    commentSpacing = trimmed.indexOf(trimmed.trim());
                } else if (!line.trim().isEmpty()) {
                    lineHadContent = true;

                    if (line.trim().startsWith("-"))
                        forceCompact = true;
                }

                if (!compactLines && !forceCompact && ((lastLineSpacing != -1 && lineSpacing != lastLineSpacing)
                        || (commentSpacing != -1 && commentSpacing < lastCommentSpacing)
                        || (lastLineHadContent && lineHadContent)
                        || (lineWasComment && lastLineHadContent))) {
                    stringBuilder.append('\n');
                }

                stringBuilder.append(line).append('\n');

                lastLineHadContent = lineHadContent;
                lastCommentSpacing = commentSpacing;
                lastLineSpacing = lineSpacing;
                forceCompact = false;
            }
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(stringBuilder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPluginName() {
        return this.plugin.getDescription().getName();
    }

}