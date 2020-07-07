package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class BeeStackSettings extends EntityStackSettings {

    private boolean dontStackIfAngry;
    private boolean dontStackIfHasHive;
    private boolean dontStackIfDifferentHives;
    private boolean dontStackIfStung;
    private boolean dontStackIfHasFlower;
    private boolean dontStackIfHasNectar;

    public BeeStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfAngry = this.settingsConfiguration.getBoolean("dont-stack-if-angry");
        this.dontStackIfHasHive = this.settingsConfiguration.getBoolean("dont-stack-if-has-hive");
        this.dontStackIfDifferentHives = this.settingsConfiguration.getBoolean("dont-stack-if-different-hives");
        this.dontStackIfStung = this.settingsConfiguration.getBoolean("dont-stack-if-stung");
        this.dontStackIfHasFlower = this.settingsConfiguration.getBoolean("dont-stack-if-has-flower");
        this.dontStackIfHasNectar = this.settingsConfiguration.getBoolean("dont-stack-if-has-nectar");
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
    public void applyUnstackProperties(LivingEntity stacked, LivingEntity unstacked) {
        Bee stackedBee = (Bee) stacked;
        Bee unstackedBee = (Bee) unstacked;

        stackedBee.setAnger(unstackedBee.getAnger());
        stackedBee.setTarget(unstackedBee.getTarget());
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
