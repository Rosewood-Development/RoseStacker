package dev.rosewood.rosestacker.stack.settings;

import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedItem;
import dev.rosewood.rosestacker.utils.StackerUtils;
import org.bukkit.Material;

public class ItemStackSettings extends StackSettings<StackedItem> {

    private Material material;
    private boolean enabled;
    private String displayName;

    public ItemStackSettings(CommentedFileConfiguration settingsConfiguration, Material material) {
        super(settingsConfiguration);
        this.material = material;
        this.setDefaults();

        this.enabled = this.settingsConfiguration.getBoolean("enabled");
        this.displayName = this.settingsConfiguration.getString("display-name");
    }

    @Override
    public boolean canStackWith(StackedItem stack1, StackedItem stack2, boolean comparingForUnstack) {
        if (!this.enabled)
            return false;

        return true;
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        this.setIfNotExists("enabled", true);
        this.setIfNotExists("display-name", StackerUtils.formatName(this.material.name()));
    }

    @Override
    protected String getConfigurationSectionKey() {
        return this.material.name();
    }

    public Material getType() {
        return this.material;
    }

    public boolean isStackingEnabled() {
        return this.enabled;
    }

    public String getDisplayName() {
        return this.displayName;
    }

}
