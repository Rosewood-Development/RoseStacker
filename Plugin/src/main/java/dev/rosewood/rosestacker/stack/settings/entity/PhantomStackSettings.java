package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;

public class PhantomStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentSize;

    public PhantomStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfDifferentSize = this.settingsConfiguration.getBoolean("dont-stack-if-different-size");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Phantom phantom1 = (Phantom) stack1.getEntity();
        Phantom phantom2 = (Phantom) stack2.getEntity();

        if (this.dontStackIfDifferentSize && phantom1.getSize() != phantom2.getSize())
            return EntityStackComparisonResult.DIFFERENT_SIZES;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-size", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PHANTOM;
    }

}
