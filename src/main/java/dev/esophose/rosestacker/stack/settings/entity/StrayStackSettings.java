package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import org.bukkit.entity.EntityType;

public class StrayStackSettings extends SkeletonStackSettings {

    public StrayStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.STRAY;
    }

}
