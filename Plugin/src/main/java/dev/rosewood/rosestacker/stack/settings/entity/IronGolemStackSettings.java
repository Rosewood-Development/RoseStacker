package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;

public class IronGolemStackSettings extends EntityStackSettings {

    private final boolean dontStackIfPlayerCreated;

    public IronGolemStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfPlayerCreated = this.settingsConfiguration.getBoolean("dont-stack-if-player-created");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        IronGolem ironGolem1 = (IronGolem) stack1.getEntity();
        IronGolem ironGolem2 = (IronGolem) stack2.getEntity();

        if (this.dontStackIfPlayerCreated && (ironGolem1.isPlayerCreated() || ironGolem2.isPlayerCreated()))
            return EntityStackComparisonResult.SPAWNED_BY_PLAYER;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-player-created", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.IRON_GOLEM;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return null;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Collections.singletonList("on-ground");
    }

}
