package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vex;

public class VexStackSettings extends EntityStackSettings {

    private final boolean dontStackIfCharging;

    public VexStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

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
    public EntityType getEntityType() {
        return EntityType.VEX;
    }

}
