package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTags;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;

public class WolfStackSettings extends EntityStackSettings {

    private boolean dontStackIfAngry;
    private boolean dontStackIfDifferentCollarColor;

    public WolfStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfAngry = this.settingsConfiguration.getBoolean("dont-stack-if-angry");
        this.dontStackIfDifferentCollarColor = this.settingsConfiguration.getBoolean("dont-stack-if-different-collar-color");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Wolf wolf1 = (Wolf) stack1.getEntity();
        Wolf wolf2 = (Wolf) stack2.getEntity();

        if (this.dontStackIfAngry && (wolf1.isAngry() || wolf2.isAngry()))
            return false;

        return !this.dontStackIfDifferentCollarColor || wolf1.getCollarColor() == wolf2.getCollarColor();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-angry", false);
        this.setIfNotExists("dont-stack-if-different-collar-color", false);
    }

    @Override
    public void applyUnstackProperties(LivingEntity stacked, LivingEntity unstacked) {
        Wolf stackedWolf = (Wolf) stacked;
        Wolf unstackedWolf = (Wolf) unstacked;

        stackedWolf.setAngry(unstackedWolf.isAngry());
        unstackedWolf.setTarget(unstackedWolf.getTarget());
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.WOLF;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.WOLF_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return ConditionTags.ANIMAL_TAGS;
    }

}
