package dev.esophose.sparkstacker.stack.settings.entity;

import dev.esophose.sparkstacker.config.CommentedFileConfiguration;
import dev.esophose.sparkstacker.stack.StackedEntity;
import dev.esophose.sparkstacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PufferFish;

public class PufferfishStackSettings extends EntityStackSettings {

    private boolean dontStackIfDifferentInflation;

    public PufferfishStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentInflation = this.settingsConfiguration.getBoolean("dont-stack-if-different-inflation");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        PufferFish pufferFish1 = (PufferFish) stack1.getEntity();
        PufferFish pufferFish2 = (PufferFish) stack2.getEntity();

        return !this.dontStackIfDifferentInflation || pufferFish1.getPuffState() == pufferFish2.getPuffState();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-inflation", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PUFFERFISH;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.PUFFERFISH_SPAWN_EGG;
    }

}
