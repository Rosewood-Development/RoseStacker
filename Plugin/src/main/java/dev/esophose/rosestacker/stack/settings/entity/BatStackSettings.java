package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;

public class BatStackSettings extends EntityStackSettings {

    private boolean dontStackIfSleeping;

    public BatStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfSleeping = settingsConfiguration.getBoolean("dont-stack-if-sleeping");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Bat bat1 = (Bat) stack1.getEntity();
        Bat bat2 = (Bat) stack2.getEntity();

        return !this.dontStackIfSleeping || (bat1.isAwake() && bat2.isAwake());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-sleeping", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.BAT;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.BAT_SPAWN_EGG;
    }

}
