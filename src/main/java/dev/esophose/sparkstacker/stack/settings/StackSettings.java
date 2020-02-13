package dev.esophose.sparkstacker.stack.settings;

import dev.esophose.sparkstacker.config.CommentedConfigurationSection;
import dev.esophose.sparkstacker.config.CommentedFileConfiguration;
import dev.esophose.sparkstacker.stack.Stack;

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
