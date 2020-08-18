package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.gui.StackedSpawnerGui;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.HologramManager;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTag;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class StackedSpawner extends Stack<SpawnerStackSettings> {

    private int size;
    private CreatureSpawner spawner;
    private Location location;
    private StackedSpawnerGui stackedSpawnerGui;
    private List<Class<? extends ConditionTag>> lastInvalidConditions;

    private SpawnerStackSettings stackSettings;

    public StackedSpawner(int id, int size, CreatureSpawner spawner) {
        super(id);

        this.size = size;
        this.spawner = spawner;
        this.location = this.spawner.getLocation();
        this.stackedSpawnerGui = null;
        this.lastInvalidConditions = new ArrayList<>();

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

    public void kickOutViewers() {
        if (this.stackedSpawnerGui != null)
            this.stackedSpawnerGui.kickOutViewers();
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

    public void openGui(Player player) {
        if (this.stackedSpawnerGui == null)
            this.stackedSpawnerGui = new StackedSpawnerGui(this);
        this.stackedSpawnerGui.openFor(player);
    }

    public List<Class<? extends ConditionTag>> getLastInvalidConditions() {
        return this.lastInvalidConditions;
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
        if (!Setting.SPAWNER_DISPLAY_TAGS.getBoolean() || this.stackSettings == null)
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

    @Override
    public SpawnerStackSettings getStackSettings() {
        return this.stackSettings;
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
        this.spawner.setRequiredPlayerRange(this.stackSettings.getPlayerActivationRange());
        this.spawner.setSpawnRange(this.stackSettings.getSpawnRange());
        this.spawner.setDelay(delay);
        this.spawner.update();
    }

}
