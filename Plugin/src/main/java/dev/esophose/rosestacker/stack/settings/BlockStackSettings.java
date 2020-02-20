package dev.esophose.rosestacker.stack.settings;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.stack.StackedBlock;
import dev.esophose.rosestacker.utils.StackerUtils;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;

public class BlockStackSettings extends StackSettings<StackedBlock> {

    private static final List<Material> enabledByDefault = Arrays.asList(Material.DIAMOND_BLOCK, Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.EMERALD_BLOCK, Material.LAPIS_BLOCK);

    private Material material;
    private boolean enabled;
    private String displayName;

    public BlockStackSettings(CommentedFileConfiguration settingsConfiguration, Material material) {
        super(settingsConfiguration);
        this.material = material;
        this.setDefaults();

        this.enabled = this.settingsConfiguration.getBoolean("enabled");
        this.displayName = this.settingsConfiguration.getString("display-name");
    }

    @Override
    public boolean canStackWith(StackedBlock stack1, StackedBlock stack2, boolean comparingForUnstack) {
        if (!this.enabled)
            return false;

        return true;
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        this.setIfNotExists("enabled", enabledByDefault.contains(this.material));
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
