package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;

public class PigStackSettings extends EntityStackSettings {

    private final boolean dontStackIfSaddled;

    public PigStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfSaddled = this.settingsConfiguration.getBoolean("dont-stack-if-saddled");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Pig pig1 = (Pig) stack1.getEntity();
        Pig pig2 = (Pig) stack2.getEntity();

        if (this.dontStackIfSaddled && (pig1.hasSaddle() || pig2.hasSaddle()))
            return EntityStackComparisonResult.SADDLED;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-saddled", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PIG;
    }

}
