package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Piglin;

public class PiglinStackSettings extends EntityStackSettings {

    private boolean dontStackIfConverting;
    private boolean dontStackIfDifferentAge;
    private boolean dontStackIfUnableToHunt;
    private boolean dontStackIfImmuneToZombification;

    public PiglinStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfConverting = this.settingsConfiguration.getBoolean("dont-stack-if-converting");
        this.dontStackIfDifferentAge = this.settingsConfiguration.getBoolean("dont-stack-if-different-age");
        this.dontStackIfUnableToHunt = this.settingsConfiguration.getBoolean("dont-stack-if-unable-to-hunt");
        this.dontStackIfImmuneToZombification = this.settingsConfiguration.getBoolean("dont-stack-if-immune-to-zombification");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Piglin piglin1 = (Piglin) stack1.getEntity();
        Piglin piglin2 = (Piglin) stack2.getEntity();

        if (this.dontStackIfConverting && (piglin1.isConverting() || piglin2.isConverting()))
            return false;

        if (this.dontStackIfDifferentAge && (piglin1.isBaby() != piglin2.isBaby()))
            return false;

        if (this.dontStackIfUnableToHunt && (!piglin1.isAbleToHunt() || !piglin2.isAbleToHunt()))
            return false;

        return !this.dontStackIfImmuneToZombification || !(piglin1.isImmuneToZombification() || piglin2.isImmuneToZombification());
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
