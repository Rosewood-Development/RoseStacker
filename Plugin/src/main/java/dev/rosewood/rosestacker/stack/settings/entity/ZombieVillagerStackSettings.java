package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ZombieVillager;

public class ZombieVillagerStackSettings extends EntityStackSettings {

    protected boolean dontStackIfDifferentProfession;
    protected boolean dontStackIfConverting;

    public ZombieVillagerStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfDifferentProfession = this.settingsConfiguration.getBoolean("dont-stack-if-different-profession");
        this.dontStackIfConverting = this.settingsConfiguration.getBoolean("dont-stack-if-converting");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        ZombieVillager zombieVillager1 = (ZombieVillager) stack1.getEntity();
        ZombieVillager zombieVillager2 = (ZombieVillager) stack2.getEntity();

        if (this.dontStackIfDifferentProfession && zombieVillager1.getVillagerProfession() != zombieVillager2.getVillagerProfession())
            return EntityStackComparisonResult.DIFFERENT_PROFESSIONS;

        if (this.dontStackIfConverting && (zombieVillager1.isConverting() || zombieVillager2.isConverting()))
            return EntityStackComparisonResult.CONVERTING;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-profession", false);
        this.setIfNotExists("dont-stack-if-converting", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ZOMBIE_VILLAGER;
    }

}
