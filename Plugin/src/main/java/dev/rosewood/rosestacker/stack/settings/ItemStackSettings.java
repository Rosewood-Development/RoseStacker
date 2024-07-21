package dev.rosewood.rosestacker.stack.settings;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.utils.StackerUtils;
import org.bukkit.Material;

public class ItemStackSettings extends StackSettings {

    private final Material material;
    private final boolean enabled;
    private final String displayName;
    private final int maxStackSize;
    private final Boolean displayTags;

    public ItemStackSettings(CommentedFileConfiguration settingsConfiguration, Material material) {
        super(settingsConfiguration);
        this.material = material;
        this.setDefaults();

        this.enabled = this.settingsConfiguration.getBoolean("enabled");
        this.displayName = this.settingsConfiguration.getString("display-name");
        this.maxStackSize = this.settingsConfiguration.getInt("max-stack-size");
        this.displayTags = this.settingsConfiguration.getDefaultedBoolean("display-tags");
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        this.setIfNotExists("enabled", true);
        this.setIfNotExists("display-name", StackerUtils.formatMaterialName(this.material));
        this.setIfNotExists("max-stack-size", -1);
        this.setIfNotExists("display-tags", "default");
    }

    @Override
    protected String getConfigurationSectionKey() {
        return this.material.name();
    }

    @Override
    public boolean isStackingEnabled() {
        return this.enabled;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public int getMaxStackSize() {
        if (this.maxStackSize != -1)
            return this.maxStackSize;
        return SettingKey.ITEM_MAX_STACK_SIZE.get();
    }

    public Material getType() {
        return this.material;
    }

    public boolean shouldDisplayTags() {
        if (this.displayTags == null)
            return SettingKey.ITEM_DISPLAY_TAGS.get();
        return this.displayTags;
    }

}
