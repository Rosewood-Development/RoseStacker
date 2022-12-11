package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.Camel;
import org.bukkit.entity.EntityType;

public class CamelStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDashing;

    public CamelStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfDashing = this.settingsConfiguration.getBoolean("dont-stack-if-dashing");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Camel camel1 = (Camel) stack1.getEntity();
        Camel camel2 = (Camel) stack2.getEntity();

        if (this.dontStackIfDashing && (camel1.isDashing() || camel2.isDashing()))
            return EntityStackComparisonResult.DASHING;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-dashing", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.CAMEL;
    }

}
