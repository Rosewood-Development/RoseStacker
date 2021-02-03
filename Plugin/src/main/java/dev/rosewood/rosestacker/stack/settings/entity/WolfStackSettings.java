package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;

public class WolfStackSettings extends EntityStackSettings {

    private final boolean dontStackIfAngry;
    private final boolean dontStackIfDifferentCollarColor;

    public WolfStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfAngry = this.settingsConfiguration.getBoolean("dont-stack-if-angry");
        this.dontStackIfDifferentCollarColor = this.settingsConfiguration.getBoolean("dont-stack-if-different-collar-color");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Wolf wolf1 = (Wolf) stack1.getEntity();
        Wolf wolf2 = (Wolf) stack2.getEntity();

        if (this.dontStackIfAngry && (wolf1.isAngry() || wolf2.isAngry()))
            return EntityStackComparisonResult.ANGRY;

        if (this.dontStackIfDifferentCollarColor && wolf1.getCollarColor() != wolf2.getCollarColor())
            return EntityStackComparisonResult.DIFFERENT_COLLAR_COLORS;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-angry", false);
        this.setIfNotExists("dont-stack-if-different-collar-color", false);
    }

    @Override
    public void applyUnstackProperties(LivingEntity stacked, LivingEntity unstacked) {
        super.applyUnstackProperties(stacked, unstacked);

        Wolf stackedWolf = (Wolf) stacked;
        Wolf unstackedWolf = (Wolf) unstacked;

        stackedWolf.setAngry(unstackedWolf.isAngry());
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.WOLF;
    }

}
