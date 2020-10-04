package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTags;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;

public class ZombifiedPiglinStackSettings extends EntityStackSettings {

    protected boolean dontStackIfAngry;
    protected boolean dontStackIfDifferentAge;

    public ZombifiedPiglinStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfAngry = this.settingsConfiguration.getBoolean("dont-stack-if-angry");
        this.dontStackIfDifferentAge = this.settingsConfiguration.getBoolean("dont-stack-if-different-age");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        PigZombie pigZombie1 = (PigZombie) stack1.getEntity();
        PigZombie pigZombie2 = (PigZombie) stack2.getEntity();

        if (this.dontStackIfAngry && (pigZombie1.isAngry() || pigZombie2.isAngry()))
            return false;

        return !this.dontStackIfDifferentAge || pigZombie1.isBaby() == pigZombie2.isBaby();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-angry", false);
        this.setIfNotExists("dont-stack-if-different-age", false);
    }

    @Override
    public void applyUnstackProperties(LivingEntity stacked, LivingEntity unstacked) {
        PigZombie stackedPigZombie = (PigZombie) stacked;
        PigZombie unstackedPigZombie = (PigZombie) unstacked;

        stackedPigZombie.setAnger(unstackedPigZombie.getAnger());
        stackedPigZombie.setTarget(unstackedPigZombie.getTarget());

        super.applyUnstackProperties(stacked, unstacked);
    }

    @Override
    public EntityType getEntityType() {
        return NMSUtil.getVersionNumber() >= 16 ? EntityType.ZOMBIFIED_PIGLIN : EntityType.valueOf("PIG_ZOMBIE");
    }

    @Override
    public Material getSpawnEggMaterial() {
        return NMSUtil.getVersionNumber() >= 16 ? Material.ZOMBIFIED_PIGLIN_SPAWN_EGG : Material.valueOf("ZOMBIE_PIGMAN_SPAWN_EGG");
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        if (NMSUtil.getVersionNumber() >= 16) {
            return Arrays.asList(
                    "darkness",
                    "block-exception:nether_wart_block"
            );
        } else {
            return ConditionTags.MONSTER_TAGS;
        }
    }

}
