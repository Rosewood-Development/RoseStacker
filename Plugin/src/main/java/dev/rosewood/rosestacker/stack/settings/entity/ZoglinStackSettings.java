package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zoglin;

public class ZoglinStackSettings extends EntityStackSettings {

    private boolean dontStackIfDifferentAge;

    public ZoglinStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentAge = this.settingsConfiguration.getBoolean("dont-stack-if-different-age");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Zoglin zoglin1 = (Zoglin) stack1.getEntity();
        Zoglin zoglin2 = (Zoglin) stack2.getEntity();

        return !this.dontStackIfDifferentAge || zoglin1.isBaby() == zoglin2.isBaby();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-age", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ZOGLIN;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.ZOGLIN_SPAWN_EGG;
    }

}
