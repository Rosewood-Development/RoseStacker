package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTags;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ZombieVillager;

public class ZombieVillagerStackSettings extends EntityStackSettings {

    protected boolean dontStackIfDifferentProfession;
    protected boolean dontStackIfConverting;
    protected boolean dontStackIfDifferentAge;

    public ZombieVillagerStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentProfession = this.settingsConfiguration.getBoolean("dont-stack-if-different-profession");
        this.dontStackIfConverting = this.settingsConfiguration.getBoolean("dont-stack-if-converting");
        this.dontStackIfDifferentAge = this.settingsConfiguration.getBoolean("dont-stack-if-different-age");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        ZombieVillager zombieVillager1 = (ZombieVillager) stack1.getEntity();
        ZombieVillager zombieVillager2 = (ZombieVillager) stack2.getEntity();

        if (this.dontStackIfDifferentProfession && zombieVillager1.getVillagerProfession() != zombieVillager2.getVillagerProfession())
            return false;

        if (this.dontStackIfConverting && (zombieVillager1.isConverting() || zombieVillager2.isConverting()))
            return false;

        return !this.dontStackIfDifferentAge || zombieVillager1.isBaby() == zombieVillager2.isBaby();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-profession", false);
        this.setIfNotExists("dont-stack-if-converting", false);
        this.setIfNotExists("dont-stack-if-different-age", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ZOMBIE_VILLAGER;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.ZOMBIE_VILLAGER_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return ConditionTags.MONSTER_TAGS;
    }

}
