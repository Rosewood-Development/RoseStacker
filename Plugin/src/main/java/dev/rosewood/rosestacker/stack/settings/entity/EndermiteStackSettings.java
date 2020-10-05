package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTags;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.EntityType;

public class EndermiteStackSettings extends EntityStackSettings {

    private final boolean dontStackIfPlayerSpawned;

    public EndermiteStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfPlayerSpawned = this.settingsConfiguration.getBoolean("dont-stack-if-player-spawned");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Endermite endermite1 = (Endermite) stack1.getEntity();
        Endermite endermite2 = (Endermite) stack2.getEntity();

        if (this.dontStackIfPlayerSpawned && (endermite1.isPlayerSpawned() || endermite2.isPlayerSpawned()))
            return EntityStackComparisonResult.SPAWNED_BY_PLAYER;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-player-spawned", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ENDERMITE;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.ENDERMITE_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return ConditionTags.MONSTER_TAGS;
    }

}
