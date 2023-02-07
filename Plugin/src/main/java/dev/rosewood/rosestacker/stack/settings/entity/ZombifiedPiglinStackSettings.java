package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;

public class ZombifiedPiglinStackSettings extends EntityStackSettings {

    protected final boolean dontStackIfAngry;

    public ZombifiedPiglinStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfAngry = this.settingsConfiguration.getBoolean("dont-stack-if-angry");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        PigZombie pigZombie1 = (PigZombie) stack1.getEntity();
        PigZombie pigZombie2 = (PigZombie) stack2.getEntity();

        if (this.dontStackIfAngry && (pigZombie1.isAngry() || pigZombie2.isAngry()))
            return EntityStackComparisonResult.ANGRY;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-angry", false);
    }

    @Override
    public void applyUnstackProperties(LivingEntity stacked, LivingEntity unstacked) {
        super.applyUnstackProperties(stacked, unstacked);

        PigZombie stackedPigZombie = (PigZombie) stacked;
        PigZombie unstackedPigZombie = (PigZombie) unstacked;

        stackedPigZombie.setAnger(unstackedPigZombie.getAnger());
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ZOMBIFIED_PIGLIN;
    }

}
