package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;

public class HorseStackSettings extends EntityStackSettings {

    private final boolean dontStackIfArmored;
    private final boolean dontStackIfDifferentStyle;
    private final boolean dontStackIfDifferentColor;

    public HorseStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfArmored = this.settingsConfiguration.getBoolean("dont-stack-if-armored");
        this.dontStackIfDifferentStyle = this.settingsConfiguration.getBoolean("dont-stack-if-different-style");
        this.dontStackIfDifferentColor = this.settingsConfiguration.getBoolean("dont-stack-if-different-color");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Horse horse1 = (Horse) stack1.getEntity();
        Horse horse2 = (Horse) stack2.getEntity();

        if (this.dontStackIfArmored && (horse1.getInventory().getArmor() != null || horse2.getInventory().getArmor() != null))
            return EntityStackComparisonResult.HAS_ARMOR;

        if (this.dontStackIfDifferentStyle && horse1.getStyle() != horse2.getStyle())
            return EntityStackComparisonResult.DIFFERENT_STYLES;

        if (this.dontStackIfDifferentColor && horse1.getColor() != horse2.getColor())
            return EntityStackComparisonResult.DIFFERENT_COLORS;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-armored", false);
        this.setIfNotExists("dont-stack-if-different-style", false);
        this.setIfNotExists("dont-stack-if-different-color", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.HORSE;
    }

}
