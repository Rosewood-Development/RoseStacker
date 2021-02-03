package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Panda;

public class PandaStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentMainGene;
    private final boolean dontStackIfDifferentRecessiveGene;

    public PandaStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfDifferentMainGene = this.settingsConfiguration.getBoolean("dont-stack-if-different-main-gene");
        this.dontStackIfDifferentRecessiveGene = this.settingsConfiguration.getBoolean("dont-stack-if-different-recessive-gene");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Panda panda1 = (Panda) stack1.getEntity();
        Panda panda2 = (Panda) stack2.getEntity();

        if (this.dontStackIfDifferentMainGene && panda1.getMainGene() != panda2.getMainGene())
            return EntityStackComparisonResult.DIFFERENT_MAIN_GENES;

        if (this.dontStackIfDifferentRecessiveGene && panda1.getHiddenGene() != panda2.getHiddenGene())
            return EntityStackComparisonResult.DIFFERENT_RECESSIVE_GENES;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-main-gene", false);
        this.setIfNotExists("dont-stack-if-different-recessive-gene", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PANDA;
    }

}
