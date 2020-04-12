package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;

public class PigZombieStackSettings extends EntityStackSettings {

    protected boolean dontStackIfAngry;
    protected boolean dontStackIfDifferentAge;

    public PigZombieStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfAngry = this.settingsConfiguration.getBoolean("dont-stack-if-angry");
        this.dontStackIfDifferentAge = this.settingsConfiguration.getBoolean("dont-stack-if-different-age");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        PigZombie pigZombie1 = (PigZombie) stack1.getEntity();
        PigZombie pigZombie2 = (PigZombie) stack2.getEntity();

        if (this.dontStackIfAngry && (pigZombie1.isAngry() || pigZombie2.isAngry()))
            return false;

        return !this.dontStackIfDifferentAge || pigZombie1.isBaby() == pigZombie2.isBaby();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-angry", false);
        this.setIfNotExists("dont-stack-if-different-age", false);
    }

    @Override
    public void applyUnstackProperties(LivingEntity stacked, LivingEntity unstacked) {
        PigZombie stackedPigZombie = (PigZombie) stacked;
        PigZombie unstackedPigZombie = (PigZombie) unstacked;

        stackedPigZombie.setAnger(unstackedPigZombie.getAnger());
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PIG_ZOMBIE;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.ZOMBIE_PIGMAN_SPAWN_EGG;
    }

}
