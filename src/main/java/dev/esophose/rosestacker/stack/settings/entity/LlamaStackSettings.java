package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Llama;

public class LlamaStackSettings extends EntityStackSettings {

    private boolean dontStackIfDifferentDecor;
    private boolean dontStackIfDifferentColor;

    public LlamaStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentDecor = this.entitySettingsConfiguration.getBoolean("dont-stack-if-different-decor");
        this.dontStackIfDifferentColor = this.entitySettingsConfiguration.getBoolean("dont-stack-if-different-color");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Llama llama1 = (Llama) stack1.getEntity();
        Llama llama2 = (Llama) stack2.getEntity();

        if (this.dontStackIfDifferentDecor && llama1.getInventory().getDecor() != llama2.getInventory().getDecor())
            return false;

        return !this.dontStackIfDifferentColor || (llama1.getColor() == llama2.getColor());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-decor", false);
        this.setIfNotExists("dont-stack-if-different-color", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.LLAMA;
    }

}
