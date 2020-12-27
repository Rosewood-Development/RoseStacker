package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;

public class BatStackSettings extends EntityStackSettings {

    private final boolean dontStackIfSleeping;

    public BatStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfSleeping = this.settingsConfiguration.getBoolean("dont-stack-if-sleeping");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Bat bat1 = (Bat) stack1.getEntity();
        Bat bat2 = (Bat) stack2.getEntity();

        if (this.dontStackIfSleeping && (!bat1.isAwake() || !bat2.isAwake()))
            return EntityStackComparisonResult.SLEEPING;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-sleeping", false);
    }

    @Override
    public boolean isFlyingMob() {
        return true;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.BAT;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.BAT_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Arrays.asList(
                "below-sea-level",
                "darkness"
        );
    }

}
