package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zoglin;

public class ZoglinStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentAge;

    public ZoglinStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfDifferentAge = this.settingsConfiguration.getBoolean("dont-stack-if-different-age");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Zoglin zoglin1 = (Zoglin) stack1.getEntity();
        Zoglin zoglin2 = (Zoglin) stack2.getEntity();

        if (this.dontStackIfDifferentAge && zoglin1.isBaby() != zoglin2.isBaby())
            return EntityStackComparisonResult.DIFFERENT_AGES;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-age", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ZOGLIN;
    }

}
