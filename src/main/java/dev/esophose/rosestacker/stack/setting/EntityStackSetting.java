package dev.esophose.rosestacker.stack.setting;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

public abstract class EntityStackSetting {

    private ConfigurationSection entitySettingsConfiguration;

    private boolean enabled;
    private String displayName;
    private int minStackSize;
    private int maxStackSize;

    public EntityStackSetting(YamlConfiguration entitySettingsYamlConfiguration) {
        this.entitySettingsConfiguration = entitySettingsYamlConfiguration.getConfigurationSection(this.getEntityType().name());
        if (this.entitySettingsConfiguration == null)
            this.entitySettingsConfiguration = entitySettingsYamlConfiguration.createSection(this.getEntityType().name());

        this.setDefaults();

        this.enabled = this.entitySettingsConfiguration.getBoolean("enabled");
        this.displayName = this.entitySettingsConfiguration.getString("display-name");
        this.minStackSize = this.entitySettingsConfiguration.getInt("min-stack-size");
        this.maxStackSize = this.entitySettingsConfiguration.getInt("max-stack-size");
    }

    private void setDefaults() {
        this.setIfNotExists("enabled", true);
        this.setIfNotExists("display-name", StackerUtils.formatName(this.getEntityType().name()));
        this.setIfNotExists("min-stack-size", -1);
        this.setIfNotExists("max-stack-size", -1);

        this.setDefaultsInternal();
    }

    public boolean canStackWith(StackedEntity stack1, StackedEntity stack2) {
        if (!this.enabled)
            return false;

        if (stack1.getStackSize() + stack2.getStackSize() > this.maxStackSize)
            return false;

        return this.canStackWithInternal(stack1, stack2);
    }

    protected void setIfNotExists(String setting, Object value) {
        if (this.entitySettingsConfiguration.get(setting) == null)
            this.entitySettingsConfiguration.set(setting, value);
    }

    public boolean isStackingEnabled() {
        return this.enabled;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getMinStackSize() {
        return this.minStackSize;
    }

    public int getMaxStackSize() {
        return this.maxStackSize;
    }

    protected abstract void setDefaultsInternal();

    protected abstract boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2);

    public abstract EntityType getEntityType();

}
