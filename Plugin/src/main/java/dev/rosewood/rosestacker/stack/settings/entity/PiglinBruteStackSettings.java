package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PiglinBrute;

public class PiglinBruteStackSettings extends EntityStackSettings {

    private boolean dontStackIfConverting;
    private boolean dontStackIfDifferentAge;
    private boolean dontStackIfImmuneToZombification;

    public PiglinBruteStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfConverting = this.settingsConfiguration.getBoolean("dont-stack-if-converting");
        this.dontStackIfDifferentAge = this.settingsConfiguration.getBoolean("dont-stack-if-different-age");
        this.dontStackIfImmuneToZombification = this.settingsConfiguration.getBoolean("dont-stack-if-immune-to-zombification");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        PiglinBrute piglinBrute1 = (PiglinBrute) stack1.getEntity();
        PiglinBrute piglinBrute2 = (PiglinBrute) stack2.getEntity();

        if (this.dontStackIfConverting && (piglinBrute1.isConverting() || piglinBrute2.isConverting()))
            return false;

        if (this.dontStackIfDifferentAge && (piglinBrute1.isBaby() != piglinBrute2.isBaby()))
            return false;

        return !this.dontStackIfImmuneToZombification || !(piglinBrute1.isImmuneToZombification() || piglinBrute2.isImmuneToZombification());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-converting", false);
        this.setIfNotExists("dont-stack-if-different-age", false);
        this.setIfNotExists("dont-stack-if-immune-to-zombification", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PIGLIN_BRUTE;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.PIGLIN_BRUTE_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Arrays.asList(
                "darkness",
                "block-exception:nether_wart_block"
        );
    }

}
