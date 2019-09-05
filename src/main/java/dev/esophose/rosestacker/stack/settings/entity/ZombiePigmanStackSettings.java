package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;

public class ZombiePigmanStackSettings extends EntityStackSettings {

    protected boolean dontStackIfAngry;
    protected boolean dontStackIfDifferentAge;

    public ZombiePigmanStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);

        this.dontStackIfAngry = entitySettingsConfiguration.getBoolean("dont-stack-if-angry");
        this.dontStackIfDifferentAge = entitySettingsConfiguration.getBoolean("dont-stack-if-different-age");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        PigZombie zombiePigman1 = (PigZombie) stack1.getEntity();
        PigZombie zombiePigman2 = (PigZombie) stack2.getEntity();

        if (this.dontStackIfAngry && (zombiePigman1.isAngry() || zombiePigman2.isAngry()))
            return false;

        return !this.dontStackIfDifferentAge || zombiePigman1.isBaby() != zombiePigman2.isBaby();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-angry", false);
        this.setIfNotExists("dont-stack-if-different-age", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PIG_ZOMBIE;
    }

}
