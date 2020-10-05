package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Piglin;

public class PiglinStackSettings extends EntityStackSettings {

    private final boolean dontStackIfConverting;
    private final boolean dontStackIfDifferentAge;
    private final boolean dontStackIfUnableToHunt;
    private final boolean dontStackIfImmuneToZombification;

    public PiglinStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfConverting = this.settingsConfiguration.getBoolean("dont-stack-if-converting");
        this.dontStackIfDifferentAge = this.settingsConfiguration.getBoolean("dont-stack-if-different-age");
        this.dontStackIfUnableToHunt = this.settingsConfiguration.getBoolean("dont-stack-if-unable-to-hunt");
        this.dontStackIfImmuneToZombification = this.settingsConfiguration.getBoolean("dont-stack-if-immune-to-zombification");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Piglin piglin1 = (Piglin) stack1.getEntity();
        Piglin piglin2 = (Piglin) stack2.getEntity();

        if (this.dontStackIfConverting && (piglin1.isConverting() || piglin2.isConverting()))
            return EntityStackComparisonResult.CONVERTING;

        if (this.dontStackIfDifferentAge && (piglin1.isBaby() != piglin2.isBaby()))
            return EntityStackComparisonResult.DIFFERENT_AGES;

        if (this.dontStackIfUnableToHunt && (!piglin1.isAbleToHunt() || !piglin2.isAbleToHunt()))
            return EntityStackComparisonResult.UNABLE_TO_HUNT;

        if (this.dontStackIfImmuneToZombification && (piglin1.isImmuneToZombification() || piglin2.isImmuneToZombification()))
            return EntityStackComparisonResult.IMMUNE_TO_ZOMBIFICATION;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-converting", false);
        this.setIfNotExists("dont-stack-if-different-age", false);
        this.setIfNotExists("dont-stack-if-unable-to-hunt", false);
        this.setIfNotExists("dont-stack-if-immune-to-zombification", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PIGLIN;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.PIGLIN_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Arrays.asList(
                "darkness",
                "block-exception:nether_wart_block"
        );
    }

}
