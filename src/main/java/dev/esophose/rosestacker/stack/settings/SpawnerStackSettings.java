package dev.esophose.rosestacker.stack.settings;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.stack.StackedSpawner;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class SpawnerStackSettings extends StackSettings<StackedSpawner> {

    private EntityType entityType;
    private boolean enabled;
    private String displayName;
    private boolean disableMobAI;

    public SpawnerStackSettings(CommentedFileConfiguration settingsConfiguration, EntityType entityType) {
        super(settingsConfiguration);
        this.entityType = entityType;
        this.setDefaults();

        this.enabled = this.settingsConfiguration.getBoolean("enabled");
        this.displayName = this.settingsConfiguration.getString("display-name");
        this.disableMobAI = this.settingsConfiguration.getBoolean("disable-mob-ai");
    }

    @Override
    public boolean canStackWith(StackedSpawner stack1, StackedSpawner stack2, boolean comparingForUnstack) {
        if (!this.enabled)
            return false;

        return false;
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        this.setIfNotExists("enabled", true);
        this.setIfNotExists("display-name", StackerUtils.formatName(this.entityType.name() + '_' + Material.SPAWNER.name()));
        this.setIfNotExists("disable-mob-ai", false);
    }

    @Override
    protected String getConfigurationSectionKey() {
        return this.entityType.name();
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public boolean isStackingEnabled() {
        return this.enabled;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean isMobAIDisabled() {
        return this.disableMobAI;
    }

}
