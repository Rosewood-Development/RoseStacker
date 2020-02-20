package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;

public class BeeStackSettings extends EntityStackSettings {

    private boolean dontStackIfAngry;
    private boolean dontStackIfHasHive;
    private boolean dontStackIfDifferentHives;
    private boolean dontStackIfStung;
    private boolean dontStackIfHasFlower;
    private boolean dontStackIfHasNectar;

    public BeeStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfAngry = settingsConfiguration.getBoolean("dont-stack-if-angry");
        this.dontStackIfHasHive = settingsConfiguration.getBoolean("dont-stack-if-has-hive");
        this.dontStackIfDifferentHives = settingsConfiguration.getBoolean("dont-stack-if-different-hives");
        this.dontStackIfStung = settingsConfiguration.getBoolean("dont-stack-if-stung");
        this.dontStackIfHasFlower = settingsConfiguration.getBoolean("dont-stack-if-has-flower");
        this.dontStackIfHasNectar = settingsConfiguration.getBoolean("dont-stack-if-has-nectar");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Bee bee1 = (Bee) stack1.getEntity();
        Bee bee2 = (Bee) stack2.getEntity();

        if (this.dontStackIfAngry && (bee1.getAnger() > 0 || bee2.getAnger() > 0))
            return false;

        if (this.dontStackIfHasHive && (bee1.getHive() != null || bee2.getHive() != null))
            return false;

        if (this.dontStackIfDifferentHives && (bee1.getHive() != null && bee2.getHive() != null && !bee1.getHive().equals(bee2.getHive())))
            return false;

        if (this.dontStackIfStung && (bee1.hasStung() || bee2.hasStung()))
            return false;

        if (this.dontStackIfHasFlower && (bee1.getFlower() != null || bee2.getFlower() != null))
            return false;

        return !this.dontStackIfHasNectar || (!bee1.hasNectar() && !bee2.hasNectar());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-angry", false);
        this.setIfNotExists("dont-stack-if-has-hive", false);
        this.setIfNotExists("dont-stack-if-different-hives", false);
        this.setIfNotExists("dont-stack-if-stung", false);
        this.setIfNotExists("dont-stack-if-has-flower", false);
        this.setIfNotExists("dont-stack-if-has-nectar", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.BEE;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.BEE_SPAWN_EGG;
    }

}
