package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vex;

public class VexStackSettings extends EntityStackSettings {

    private boolean dontStackIfCharging;

    public VexStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfCharging = this.settingsConfiguration.getBoolean("dont-stack-if-charging");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Vex vex1 = (Vex) stack1.getEntity();
        Vex vex2 = (Vex) stack2.getEntity();

        return !this.dontStackIfCharging || (!vex1.isCharging() && !vex2.isCharging());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-charging", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.VEX;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.VEX_SPAWN_EGG;
    }

}
