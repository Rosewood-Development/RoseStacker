package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;

public class CreeperStackSettings extends EntityStackSettings {

    private boolean dontStackIfCharged;

    public CreeperStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfCharged = this.settingsConfiguration.getBoolean("dont-stack-if-charged");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Creeper creeper1 = (Creeper) stack1.getEntity();
        Creeper creeper2 = (Creeper) stack2.getEntity();

        return !this.dontStackIfCharged || (!creeper1.isPowered() && !creeper2.isPowered());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-charged", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.CREEPER;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.CREEPER_SPAWN_EGG;
    }

}
