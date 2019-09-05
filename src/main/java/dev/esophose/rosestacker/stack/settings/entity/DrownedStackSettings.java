package dev.esophose.rosestacker.stack.settings.entity;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EntityType;

public class DrownedStackSettings extends ZombieStackSettings {

    public DrownedStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.DROWNED;
    }

}
