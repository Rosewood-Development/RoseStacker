package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class DrownedStackSettings extends ZombieStackSettings {

    public DrownedStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);
    }

    @Override
    public boolean isSwimmingMob() {
        return true;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.DROWNED;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.DROWNED_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Arrays.asList(
                "darkness",
                "below-sea-level"
        );
    }

}
