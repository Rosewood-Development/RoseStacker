package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MushroomCow;

public class MushroomCowStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentType;

    public MushroomCowStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentType = NMSUtil.getVersionNumber() >= 14 && this.settingsConfiguration.getBoolean("dont-stack-if-different-type");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        if (NMSUtil.getVersionNumber() <= 13)
            return EntityStackComparisonResult.CAN_STACK;

        MushroomCow mushroomCow1 = (MushroomCow) stack1.getEntity();
        MushroomCow mushroomCow2 = (MushroomCow) stack2.getEntity();

        if (this.dontStackIfDifferentType && mushroomCow1.getVariant() != mushroomCow2.getVariant())
            return EntityStackComparisonResult.DIFFERENT_TYPES;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        if (NMSUtil.getVersionNumber() >= 14)
            this.setIfNotExists("dont-stack-if-different-type", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MUSHROOM_COW;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.MOOSHROOM_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Arrays.asList(
                "block:mycelium",
                "lightness"
        );
    }

}
