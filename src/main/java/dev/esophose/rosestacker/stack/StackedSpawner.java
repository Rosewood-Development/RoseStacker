package dev.esophose.rosestacker.stack;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.HologramManager;
import dev.esophose.rosestacker.manager.LocaleManager.Locale;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;

public class StackedSpawner extends Stack {

    private int size;
    private CreatureSpawner spawner;

    public StackedSpawner(int id, int size, CreatureSpawner spawner) {
        super(id);

        this.size = size;
        this.spawner = spawner;

        if (Bukkit.isPrimaryThread()) {
            this.updateDisplay();
            this.updateSpawnCount();
        }
    }

    public StackedSpawner(int size, CreatureSpawner spawner) {
        this(-1, size, spawner);
    }

    public CreatureSpawner getSpawner() {
        return this.spawner;
    }

    public void increaseStackSize(int amount) {
        this.size += amount;
        this.updateDisplay();
        this.updateSpawnCount();
    }

    public void setStackSize(int size) {
        this.size = size;
        this.updateDisplay();
        this.updateSpawnCount();
    }

    @Override
    public int getStackSize() {
        return this.size;
    }

    @Override
    public Location getLocation() {
        return this.spawner.getLocation();
    }

    @Override
    public void updateDisplay() {
        HologramManager hologramManager = RoseStacker.getInstance().getHologramManager();

        Location location = this.spawner.getLocation().clone().add(0.5, 0.75, 0.5);

        if (this.size <= 1) {
            hologramManager.deleteHologram(location);
            return;
        }

        String displayString = Locale.STACK_DISPLAY.get()
                .replaceAll("%amount%", String.valueOf(this.size))
                .replaceAll("%name%", StackerUtils.formatName(this.spawner.getSpawnedType().name() + "_" + this.spawner.getType().name()));

        hologramManager.createOrUpdateHologram(location, displayString);
    }

    private void updateSpawnCount() {
        this.spawner.setSpawnCount(this.size * 4);
        this.spawner.setMaxNearbyEntities(6 + (this.size - 1) * 4);
        this.spawner.update();
    }

}
