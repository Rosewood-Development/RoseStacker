package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class CreeperStackSettings extends EntityStackSettings {

    private final boolean dontStackIfCharged;

    public CreeperStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfCharged = this.settingsConfiguration.getBoolean("dont-stack-if-charged");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Creeper creeper1 = (Creeper) stack1.getEntity();
        Creeper creeper2 = (Creeper) stack2.getEntity();

        if (this.dontStackIfCharged && (creeper1.isPowered() || creeper2.isPowered()))
            return EntityStackComparisonResult.CHARGED;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-charged", false);
    }

    @Override
    public void applyStackProperties(LivingEntity stacking, LivingEntity stack) {
        NMSAdapter.getHandler().unigniteCreeper((Creeper) stacking);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.CREEPER;
    }

}
