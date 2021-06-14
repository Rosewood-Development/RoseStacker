package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.GlowSquid;

public class GlowSquidStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDark;

    public GlowSquidStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfDark = this.settingsConfiguration.getBoolean("dont-stack-if-dark");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        GlowSquid glowSquid1 = (GlowSquid) stack1.getEntity();
        GlowSquid glowSquid2 = (GlowSquid) stack2.getEntity();

        if (this.dontStackIfDark && (glowSquid1.getDarkTicksRemaining() > 0 || glowSquid2.getDarkTicksRemaining() > 0))
            return EntityStackComparisonResult.BRAVO_SIX_GOING_DARK;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-dark", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.GLOW_SQUID;
    }

}
