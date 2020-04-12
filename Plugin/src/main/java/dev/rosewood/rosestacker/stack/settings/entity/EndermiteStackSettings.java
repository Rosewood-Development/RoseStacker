package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.EntityType;

public class EndermiteStackSettings extends EntityStackSettings {

    private boolean dontStackIfPlayerSpawned;

    public EndermiteStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfPlayerSpawned = this.settingsConfiguration.getBoolean("dont-stack-if-player-spawned");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Endermite endermite1 = (Endermite) stack1.getEntity();
        Endermite endermite2 = (Endermite) stack2.getEntity();

        return !this.dontStackIfPlayerSpawned || (!endermite1.isPlayerSpawned() && !endermite2.isPlayerSpawned());
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

}
