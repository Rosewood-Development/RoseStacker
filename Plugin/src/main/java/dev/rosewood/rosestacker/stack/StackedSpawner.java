package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.event.StackGUIOpenEvent;
import dev.rosewood.rosestacker.gui.StackedSpawnerGui;
import dev.rosewood.rosestacker.manager.ConfigurationManager;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.HologramManager;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.object.StackedSpawnerTile;
import dev.rosewood.rosestacker.spawner.conditions.ConditionTag;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class StackedSpawner extends Stack<SpawnerStackSettings> {

    private int size;
    private CreatureSpawner spawner;
    private StackedSpawnerTile spawnerTile;
    private Block block;
    private Location location;
    private boolean placedByPlayer;
    private StackedSpawnerGui stackedSpawnerGui;
    private List<Class<? extends ConditionTag>> lastInvalidConditions;

    private SpawnerStackSettings stackSettings;

    public StackedSpawner(int size, CreatureSpawner spawner, boolean placedByPlayer) {
        this.size = size;
        this.spawner = spawner;
        this.placedByPlayer = placedByPlayer;
        this.location = this.spawner.getLocation();
        this.stackedSpawnerGui = null;
        this.lastInvalidConditions = new ArrayList<>();

        if (this.spawner != null) {
            this.spawnerTile = NMSAdapter.getHandler().injectStackedSpawnerTile(this, RoseStacker.getInstance().getManager(ConfigurationManager.class).getSettingFetcher());
            this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getSpawnerStackSettings(this.spawner);
            this.block = spawner.getBlock();

            if (Bukkit.isPrimaryThread()) {
                this.updateSpawnerProperties(true);
                this.updateDisplay();
            }
        }
    }

    /**
     * This constructor should only be used by the converters and SHOULD NEVER be put into a StackingThread
     *
     * @param size The size of the stack
     * @param location The Location of the stack
     */
    public StackedSpawner(int size, Location location) {
        this.size = size;
        this.spawner = null;
        this.location = location;
    }

    public CreatureSpawner getSpawner() {
        return this.spawner;
    }

    public StackedSpawnerTile getSpawnerTile() {
        return this.spawnerTile;
    }

    public Block getBlock() {
        if (this.block == null)
            this.block = this.spawner.getBlock();
        return this.block;
    }

    public void kickOutGuiViewers() {
        if (this.stackedSpawnerGui != null)
            this.stackedSpawnerGui.kickOutViewers();
    }

    public void increaseStackSize(int amount) {
        this.size += amount;
        this.updateSpawnerProperties(false);
        this.updateDisplay();
    }

    public void setStackSize(int size) {
        this.size = size;
        this.updateSpawnerProperties(false);
        this.updateDisplay();
    }

    public void openGui(Player player) {
        StackGUIOpenEvent event = new StackGUIOpenEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

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

        String displayString;
        if (this.size == 1 && !Setting.SPAWNER_DISPLAY_TAGS_SINGLE_AMOUNT.getBoolean()) {
            displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display-single", StringPlaceholders.builder("amount", this.getStackSize())
                    .addPlaceholder("name", this.stackSettings.getDisplayName()).build());
        } else {
            displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display", StringPlaceholders.builder("amount", this.getStackSize())
                    .addPlaceholder("name", this.stackSettings.getDisplayName()).build());
        }

        hologramManager.createOrUpdateHologram(location, displayString);
    }

    @Override
    public SpawnerStackSettings getStackSettings() {
        return this.stackSettings;
    }

    public boolean isPlacedByPlayer() {
        return this.placedByPlayer;
    }

    public void updateSpawnerProperties(boolean resetDelay) {
        if (this.spawner.getBlock().getType() != Material.SPAWNER)
            return;

        // Handle the entity type changing
        EntityType oldEntityType = this.spawner.getSpawnedType();
        this.updateSpawnerState();
        if (oldEntityType != this.spawner.getSpawnedType())
            this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getSpawnerStackSettings(this.spawner);

        if (this.stackSettings.getSpawnCountStackSizeMultiplier() != -1) this.spawnerTile.setSpawnCount(this.size * this.stackSettings.getSpawnCountStackSizeMultiplier());
        if (this.stackSettings.getMaxSpawnDelay() != -1) this.spawnerTile.setMaxSpawnDelay(this.stackSettings.getMaxSpawnDelay());
        if (this.stackSettings.getMinSpawnDelay() != -1) this.spawnerTile.setMinSpawnDelay(this.stackSettings.getMinSpawnDelay());
        if (this.stackSettings.getPlayerActivationRange() != -1) this.spawnerTile.setRequiredPlayerRange(this.stackSettings.getPlayerActivationRange());
        if (this.stackSettings.getSpawnRange() != -1) this.spawnerTile.setSpawnRange(this.stackSettings.getSpawnRange());

        int delay;
        if (resetDelay) {
            delay = StackerUtils.randomInRange(this.spawnerTile.getMinSpawnDelay(), this.spawnerTile.getMaxSpawnDelay());
        } else {
            delay = this.spawner.getDelay();
        }

        this.spawnerTile.setDelay(delay);
    }

    public void updateSpawnerState() {
        if (this.spawner.getBlock().getType() == Material.SPAWNER)
            this.spawner = (CreatureSpawner) this.spawner.getBlock().getState();
    }

}
