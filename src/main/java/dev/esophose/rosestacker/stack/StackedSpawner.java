package dev.esophose.rosestacker.stack;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.manager.HologramManager;
import dev.esophose.rosestacker.stack.settings.SpawnerStackSettings;
import dev.esophose.rosestacker.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

public class StackedSpawner extends Stack {

    private int size;
    private CreatureSpawner spawner;

    private SpawnerStackSettings stackSettings;

    public StackedSpawner(int id, int size, CreatureSpawner spawner) {
        super(id);

        this.size = size;
        this.spawner = spawner;

        if (this.spawner != null) {
            this.stackSettings = RoseStacker.getInstance().getStackSettingManager().getSpawnerStackSettings(this.spawner);

            if (Bukkit.isPrimaryThread()) {
                this.updateSpawnCount();
                this.updateDisplay();
            }
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
        this.updateSpawnCount();
        this.updateDisplay();
    }

    public void setStackSize(int size) {
        this.size = size;
        this.updateSpawnCount();
        this.updateDisplay();
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
        if (!Setting.SPAWNER_DISPLAY_TAGS.getBoolean())
            return;

        HologramManager hologramManager = RoseStacker.getInstance().getHologramManager();

        Location location = this.spawner.getLocation().clone().add(0.5, 0.75, 0.5);

        if (this.size <= 1) {
            hologramManager.deleteHologram(location);
            return;
        }

        String displayString = RoseStacker.getInstance().getLocaleManager().getLocaleMessage("spawner-stack-display", StringPlaceholders.builder("amount", this.getStackSize())
                .addPlaceholder("name", this.stackSettings.getDisplayName()).build());

        hologramManager.createOrUpdateHologram(location, displayString);
    }

    public void updateSpawnCount() {
        if (this.spawner.getBlock().getType() != Material.SPAWNER)
            return;

        // Handle the entity type changing
        EntityType oldEntityType = this.spawner.getSpawnedType();
        this.spawner = (CreatureSpawner) this.spawner.getBlock().getState();
        if (oldEntityType != this.spawner.getSpawnedType())
            this.stackSettings = RoseStacker.getInstance().getStackSettingManager().getSpawnerStackSettings(this.spawner);

        int delay = this.spawner.getDelay();
        this.spawner.setSpawnCount(this.size * 4);
        this.spawner.setDelay(delay);
        this.spawner.update();
    }

}
