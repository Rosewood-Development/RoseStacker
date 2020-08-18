package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTags;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;

public class HorseStackSettings extends EntityStackSettings {

    private boolean dontStackIfArmored;
    private boolean dontStackIfDifferentStyle;
    private boolean dontStackIfDifferentColor;

    public HorseStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfArmored = this.settingsConfiguration.getBoolean("dont-stack-if-armored");
        this.dontStackIfDifferentStyle = this.settingsConfiguration.getBoolean("dont-stack-if-different-style");
        this.dontStackIfDifferentColor = this.settingsConfiguration.getBoolean("dont-stack-if-different-color");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Horse horse1 = (Horse) stack1.getEntity();
        Horse horse2 = (Horse) stack2.getEntity();

        if (this.dontStackIfArmored && (horse1.getInventory().getArmor() != null || horse2.getInventory().getArmor() != null))
            return false;

        if (this.dontStackIfDifferentStyle && horse1.getStyle() != horse2.getStyle())
            return false;

        return !this.dontStackIfDifferentColor || (horse1.getColor() == horse2.getColor());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-armored", false);
        this.setIfNotExists("dont-stack-if-different-style", false);
        this.setIfNotExists("dont-stack-if-different-color", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.HORSE;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.HORSE_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return ConditionTags.ANIMAL_TAGS;
    }

}
