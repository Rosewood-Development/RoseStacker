package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

public class StrayStackSettings extends SkeletonStackSettings {

    public StrayStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.STRAY;
    }

}
