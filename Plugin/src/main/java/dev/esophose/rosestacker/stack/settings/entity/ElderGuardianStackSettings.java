package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class ElderGuardianStackSettings extends GuardianStackSettings {

    public ElderGuardianStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ELDER_GUARDIAN;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.ELDER_GUARDIAN_SPAWN_EGG;
    }

}
