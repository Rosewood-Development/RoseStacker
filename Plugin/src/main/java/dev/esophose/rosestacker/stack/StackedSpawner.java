package dev.esophose.rosestacker.stack;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.manager.HologramManager;
import dev.esophose.rosestacker.manager.LocaleManager;
import dev.esophose.rosestacker.manager.StackSettingManager;
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
    private Location location;

    private SpawnerStackSettings stackSettings;

    public StackedSpawner(int id, int size, CreatureSpawner spawner) {
        super(id);

        this.size = size;
        this.spawner = spawner;
        this.location = this.spawner.getLocation();

        if (this.spawner != null) {
            this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getSpawnerStackSettings(this.spawner);

            if (Bukkit.isPrimaryThread()) {
                this.updateSpawnerProperties();
                this.updateDisplay();
            }
        }
    }

    public StackedSpawner(int size, CreatureSpawner spawner) {
        this(-1, size, spawner);
    }

    /**
     * This constructor should only be used by the converters and SHOULD NEVER be put into a StackingThread
     *
     * @param size The size of the stack
     * @param location The Location of the stack
     */
    public StackedSpawner(int size, Location location) {
        super(-1);

        this.size = size;
        this.spawner = null;
        this.location = location;
    }

    public CreatureSpawner getSpawner() {
        return this.spawner;
    }

    public void increaseStackSize(int amount) {
        this.size += amount;
        this.updateSpawnerProperties();
        this.updateDisplay();
    }

    public void setStackSize(int size) {
        this.size = size;
        this.updateSpawnerProperties();
        this.updateDisplay();
    }

    @Override
    public int getStackSize() {
        return this.size;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public void updateDisplay() {
        if (!Setting.SPAWNER_DISPLAY_TAGS.getBoolean())
            return;

        HologramManager hologramManager = RoseStacker.getInstance().getManager(HologramManager.class);

        Location location = this.location.clone().add(0.5, 0.75, 0.5);

        int sizeForHologram = Setting.SPAWNER_DISPLAY_TAGS_SINGLE.getBoolean() ? 0 : 1;
        if (this.size <= sizeForHologram) {
            hologramManager.deleteHologram(location);
            return;
        }

        String displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display", StringPlaceholders.builder("amount", this.getStackSize())
                .addPlaceholder("name", this.stackSettings.getDisplayName()).build());

        hologramManager.createOrUpdateHologram(location, displayString);
    }

    public void updateSpawnerProperties() {
        if (this.spawner.getBlock().getType() != Material.SPAWNER)
            return;

        // Handle the entity type changing
        EntityType oldEntityType = this.spawner.getSpawnedType();
        this.spawner = (CreatureSpawner) this.spawner.getBlock().getState();
        if (oldEntityType != this.spawner.getSpawnedType())
            this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getSpawnerStackSettings(this.spawner);

        int delay = this.spawner.getDelay();
        this.spawner.setSpawnCount(this.size * this.stackSettings.getSpawnCountStackSizeMultiplier());
        this.spawner.setMinSpawnDelay(this.stackSettings.getMinSpawnDelay());
        this.spawner.setMaxSpawnDelay(this.stackSettings.getMaxSpawnDelay());
        this.spawner.setMaxNearbyEntities(this.stackSettings.getMaxNearbyEntities());
        this.spawner.setRequiredPlayerRange(this.stackSettings.getPlayerActivationRange());
        this.spawner.setSpawnRange(this.stackSettings.getSpawnRange());
        this.spawner.setDelay(delay);
        this.spawner.update();
    }

}
