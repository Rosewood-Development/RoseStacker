package dev.rosewood.rosestacker.stack.settings;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.utils.StackerUtils;
import org.bukkit.Material;

public class ItemStackSettingsImpl extends BaseStackSettings implements ItemStackSettings {

    private final Material material;
    private final boolean enabled;
    private final String displayName;
    private final int maxStackSize;

    public ItemStackSettingsImpl(CommentedFileConfiguration settingsConfiguration, Material material) {
        super(settingsConfiguration);
        this.material = material;
        this.setDefaults();

        this.enabled = this.settingsConfiguration.getBoolean("enabled");
        this.displayName = this.settingsConfiguration.getString("display-name");
        this.maxStackSize = this.settingsConfiguration.getInt("max-stack-size");
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        this.setIfNotExists("enabled", true);
        this.setIfNotExists("display-name", StackerUtils.formatMaterialName(this.material));
        this.setIfNotExists("max-stack-size", -1);
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
        return Setting.ITEM_MAX_STACK_SIZE.getInt();
    }

    @Override
    public Material getType() {
        return this.material;
    }

}
