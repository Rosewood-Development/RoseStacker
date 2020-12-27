package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTags;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vex;

public class VexStackSettings extends EntityStackSettings {

    private final boolean dontStackIfCharging;

    public VexStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfCharging = this.settingsConfiguration.getBoolean("dont-stack-if-charging");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Vex vex1 = (Vex) stack1.getEntity();
        Vex vex2 = (Vex) stack2.getEntity();

        if (this.dontStackIfCharging && (vex1.isCharging() || vex2.isCharging()))
            return EntityStackComparisonResult.CHARGING;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-charging", false);
    }

    @Override
    public boolean isFlyingMob() {
        return true;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.VEX;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.VEX_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return ConditionTags.MONSTER_TAGS;
    }

}
