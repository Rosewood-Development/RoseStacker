package dev.esophose.sparkstacker.stack.settings.entity;

import dev.esophose.sparkstacker.config.CommentedFileConfiguration;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class MagmaCubeStackSettings extends SlimeStackSettings {

    private boolean dontStackIfDifferentSize;

    public MagmaCubeStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MAGMA_CUBE;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.MAGMA_CUBE_SPAWN_EGG;
    }

}
