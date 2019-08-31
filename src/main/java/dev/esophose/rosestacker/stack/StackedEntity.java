package dev.esophose.rosestacker.stack;

import dev.esophose.rosestacker.manager.LocaleManager.Locale;
import dev.esophose.rosestacker.utils.EntitySerializer;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.Collections;
import java.util.List;

public class StackedEntity implements Stack {

    private LivingEntity entity;
    private List<String> serializedStackedEntities;

    public StackedEntity(LivingEntity entity, List<String> serializedStackedEntities) {
        this.entity = entity;
        this.serializedStackedEntities = serializedStackedEntities;

        this.updateDisplay();
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public void increaseStackSize(LivingEntity entity) {
        this.serializedStackedEntities.add(EntitySerializer.toNBTString(entity));
        this.updateDisplay();
    }

    public void increaseStackSize(List<String> entityNBTStrings) {
        this.serializedStackedEntities.addAll(entityNBTStrings);
        this.updateDisplay();
    }

    public void decreaseStackSize() {
        Location location = this.entity.getLocation();
        this.entity = null; // Null it first so the CreatureSpawnEvent doesn't conflict with this Stack
        this.entity = EntitySerializer.fromNBTString(this.serializedStackedEntities.remove(0), location);
        this.updateDisplay();
    }

    public List<String> getStackedEntityNBTStrings() {
        return Collections.unmodifiableList(this.serializedStackedEntities);
    }

    @Override
    public int getStackSize() {
        return this.serializedStackedEntities.size() + 1;
    }

    @Override
    public Location getLocation() {
        return this.entity.getLocation();
    }

    @Override
    public void updateDisplay() {
        if (this.getStackSize() > 1) {
            String displayString = Locale.STACK_DISPLAY.get()
                    .replaceAll("%amount%", String.valueOf(this.getStackSize()))
                    .replaceAll("%name%", StackerUtils.formatName(this.entity.getType().name()));

            this.entity.setCustomNameVisible(true);
            this.entity.setCustomName(displayString);
        } else {
            this.entity.setCustomNameVisible(false);
            this.entity.setCustomName(null);
        }
    }

}
