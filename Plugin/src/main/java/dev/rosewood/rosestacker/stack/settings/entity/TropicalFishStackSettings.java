package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TropicalFish;

public class TropicalFishStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentBodyColor;
    private final boolean dontStackIfDifferentPattern;
    private final boolean dontStackIfDifferentPatternColor;

    public TropicalFishStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentBodyColor = this.settingsConfiguration.getBoolean("dont-stack-if-different-body-color");
        this.dontStackIfDifferentPattern = this.settingsConfiguration.getBoolean("dont-stack-if-different-pattern");
        this.dontStackIfDifferentPatternColor = this.settingsConfiguration.getBoolean("dont-stack-if-different-pattern-color");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        TropicalFish tropicalFish1 = (TropicalFish) stack1.getEntity();
        TropicalFish tropicalFish2 = (TropicalFish) stack2.getEntity();

        if (this.dontStackIfDifferentBodyColor && (tropicalFish1.getBodyColor() != tropicalFish2.getBodyColor()))
            return EntityStackComparisonResult.DIFFERENT_BODY_COLORS;

        if (this.dontStackIfDifferentPattern && tropicalFish1.getPattern() != tropicalFish2.getPattern())
            return EntityStackComparisonResult.DIFFERENT_PATTERNS;

        if (this.dontStackIfDifferentPatternColor && tropicalFish1.getPatternColor() != tropicalFish2.getPatternColor())
            return EntityStackComparisonResult.DIFFERENT_PATTERN_COLORS;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-body-color", false);
        this.setIfNotExists("dont-stack-if-different-pattern", false);
        this.setIfNotExists("dont-stack-if-different-pattern-color", false);
    }

    @Override
    public boolean isSwimmingMob() {
        return true;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.TROPICAL_FISH;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.TROPICAL_FISH_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Collections.singletonList("fluid:water");
    }

}
