package dev.rosewood.rosestacker.stack.settings;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;

public class BlockStackSettings extends StackSettings {

    private static final List<Material> enabledByDefault;
    static {
        enabledByDefault = new ArrayList<>(Arrays.asList(Material.DIAMOND_BLOCK, Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.EMERALD_BLOCK, Material.LAPIS_BLOCK));
        if (NMSUtil.getVersionNumber() >= 16)
            enabledByDefault.add(Material.NETHERITE_BLOCK);
    }

    private final Material material;
    private final boolean enabled;
    private final String displayName;
    private final int maxStackSize;

    public BlockStackSettings(CommentedFileConfiguration settingsConfiguration, Material material) {
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

        this.setIfNotExists("enabled", enabledByDefault.contains(this.material));
        this.setIfNotExists("display-name", StackerUtils.formatName(this.material.name()));
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
        return Setting.BLOCK_MAX_STACK_SIZE.getInt();
    }

    public Material getType() {
        return this.material;
    }

}
