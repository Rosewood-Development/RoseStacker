package dev.esophose.rosestacker.stack.settings.entity;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

public class ElderGuardianStackSettings extends GuardianStackSettings {

    public ElderGuardianStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ELDER_GUARDIAN;
    }

}
