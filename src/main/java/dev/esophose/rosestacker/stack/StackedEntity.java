package dev.esophose.rosestacker.stack;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class StackedEntity implements Stack {

    private int size;
    private LivingEntity entity;

    public StackedEntity(int size, LivingEntity entity) {
        this.size = size;
        this.entity = entity;

        this.updateDisplay();
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    @Override
    public void increaseStackSize(int amount) {
        this.size += amount;
        this.updateDisplay();
    }

    @Override
    public int getStackSize() {
        return this.size;
    }

    @Override
    public void setStackSize(int size) {
        this.size = size;
        this.updateDisplay();
    }

    @Override
    public Location getLocation() {
        return this.entity.getLocation();
    }

    @Override
    public void updateDisplay() {

    }

}
