package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Slime;

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
