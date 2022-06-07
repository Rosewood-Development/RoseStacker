package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.Allay;
import org.bukkit.entity.EntityType;

public class AllayStackSettings extends EntityStackSettings {

    private final boolean dontStackIfSleeping;

    public AllayStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfSleeping = this.settingsConfiguration.getBoolean("dont-stack-if-holding-items");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Allay allay1 = (Allay) stack1.getEntity();
        Allay allay2 = (Allay) stack2.getEntity();

        if (this.dontStackIfSleeping && (!allay1.getInventory().isEmpty() || !allay2.getInventory().isEmpty()))
            return EntityStackComparisonResult.HOLDING_ITEMS;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-holding-items", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ALLAY;
    }

}
