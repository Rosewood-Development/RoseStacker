package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import org.bukkit.entity.EntityType;

public class DrownedStackSettings extends ZombieStackSettings {

    public DrownedStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.DROWNED;
    }

}
