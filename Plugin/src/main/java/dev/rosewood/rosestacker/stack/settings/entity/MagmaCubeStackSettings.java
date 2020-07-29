package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class MagmaCubeStackSettings extends SlimeStackSettings {

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

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Collections.emptyList();
    }

}
