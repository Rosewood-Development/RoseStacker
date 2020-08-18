package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;

public class EndermanStackSettings extends EntityStackSettings {

    private boolean dontStackIfHoldingBlock;

    public EndermanStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfHoldingBlock = this.settingsConfiguration.getBoolean("dont-stack-if-holding-block");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Enderman enderman1 = (Enderman) stack1.getEntity();
        Enderman enderman2 = (Enderman) stack2.getEntity();

        return !this.dontStackIfHoldingBlock || (enderman1.getCarriedBlock() == null && enderman2.getCarriedBlock() == null);
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-holding-block", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ENDERMAN;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.ENDERMAN_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Collections.singletonList("darkness");
    }

}
