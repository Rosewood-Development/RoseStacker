package dev.rosewood.rosestacker.stack.settings;

import dev.rosewood.rosestacker.config.CommentedConfigurationSection;
import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.Stack;
import org.bukkit.Bukkit;

public abstract class StackSettings<T extends Stack> {

    protected CommentedConfigurationSection settingsConfiguration;
    private boolean hasChanges;

    public StackSettings(CommentedFileConfiguration settingsConfiguration) {
        this.settingsConfiguration = settingsConfiguration;
    }

    public abstract boolean canStackWith(T stack1, T stack2, boolean comparingForUnstack);

    protected void setDefaults() {
        CommentedConfigurationSection settingsConfiguration = this.settingsConfiguration;
        this.settingsConfiguration = this.settingsConfiguration.getConfigurationSection(this.getConfigurationSectionKey());
        if (this.settingsConfiguration == null)
            this.settingsConfiguration = settingsConfiguration.createSection(this.getConfigurationSectionKey());
    }

    protected abstract String getConfigurationSectionKey();

    protected void setIfNotExists(String setting, Object value) {
        if (this.settingsConfiguration.get(setting) == null) {
            this.settingsConfiguration.set(setting, value);
            this.hasChanges = true;
        }
    }

    public boolean hasChanges() {
        return this.hasChanges;
    }

}
