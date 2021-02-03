package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;

public class EndermanStackSettings extends EntityStackSettings {

    private final boolean dontStackIfHoldingBlock;

    public EndermanStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfHoldingBlock = this.settingsConfiguration.getBoolean("dont-stack-if-holding-block");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Enderman enderman1 = (Enderman) stack1.getEntity();
        Enderman enderman2 = (Enderman) stack2.getEntity();

        if (this.dontStackIfHoldingBlock && (enderman1.getCarriedBlock() != null || enderman2.getCarriedBlock() != null))
            return EntityStackComparisonResult.HOLDING_BLOCK;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-holding-block", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ENDERMAN;
    }

}
