package dev.esophose.rosestacker.stack.settings.entity;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

public class MagmaCubeStackSettings extends SlimeStackSettings {

    private boolean dontStackIfDifferentSize;

    public MagmaCubeStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MAGMA_CUBE;
    }

}
