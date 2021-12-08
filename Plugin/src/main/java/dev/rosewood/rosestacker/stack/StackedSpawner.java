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
import org.bukkit.entity.Player;

public class StackedSpawner extends Stack<SpawnerStackSettings> {

    private int size;
    private StackedSpawnerTile spawnerTile;
    private Block block;
    private boolean placedByPlayer;
    private StackedSpawnerGui stackedSpawnerGui;
    private List<Class<? extends ConditionTag>> lastInvalidConditions;
    private SpawnerStackSettings stackSettings;

    public StackedSpawner(int size, Block spawner, boolean placedByPlayer) {
        if (spawner.getType() != Material.SPAWNER)
            throw new IllegalArgumentException("Block must be a spawner");

        this.size = size;
        this.placedByPlayer = placedByPlayer;
        this.stackedSpawnerGui = null;
        this.lastInvalidConditions = new ArrayList<>();

        this.block = spawner;
        this.spawnerTile = NMSAdapter.getHandler().injectStackedSpawnerTile(this, RoseStacker.getInstance().getManager(ConfigurationManager.class).getSettingFetcher());
        this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getSpawnerStackSettings(this.spawnerTile.getSpawnedType());

        if (Bukkit.isPrimaryThread()) {
            this.updateSpawnerProperties(true);
            this.updateDisplay();
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
        if (location.getWorld() != null)
            this.block = location.getWorld().getBlockAt(location);
    }

    public StackedSpawnerTile getSpawnerTile() {
        return this.spawnerTile;
    }

    public Block getBlock() {
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
        return this.block.getLocation();
    }

    @Override
    public void updateDisplay() {
        if (!Setting.SPAWNER_DISPLAY_TAGS.getBoolean() || this.stackSettings == null)
            return;

        HologramManager hologramManager = RoseStacker.getInstance().getManager(HologramManager.class);

        Location location = this.block.getLocation().add(0.5, 0.75, 0.5);

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
        // Handle the entity type changing
        this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getSpawnerStackSettings(this.spawnerTile.getSpawnedType());

        if (this.stackSettings.getSpawnCountStackSizeMultiplier() != -1) this.spawnerTile.setSpawnCount(this.size * this.stackSettings.getSpawnCountStackSizeMultiplier());
        if (this.stackSettings.getMaxSpawnDelay() != -1) this.spawnerTile.setMaxSpawnDelay(this.stackSettings.getMaxSpawnDelay());
        if (this.stackSettings.getMinSpawnDelay() != -1) this.spawnerTile.setMinSpawnDelay(this.stackSettings.getMinSpawnDelay());
        if (this.stackSettings.getPlayerActivationRange() != -1) this.spawnerTile.setRequiredPlayerRange(this.stackSettings.getPlayerActivationRange());
        if (this.stackSettings.getSpawnRange() != -1) this.spawnerTile.setSpawnRange(this.stackSettings.getSpawnRange());

        int delay;
        if (resetDelay) {
            delay = StackerUtils.randomInRange(this.spawnerTile.getMinSpawnDelay(), this.spawnerTile.getMaxSpawnDelay());
        } else {
            delay = this.spawnerTile.getDelay();
        }

        this.spawnerTile.setDelay(delay);
    }

    /**
     * @return a freshly fetched copy of the spawner's CreatureSpawner state
     * @deprecated Use {@link #getSpawnerTile} instead as it is updated live
     */
    @Deprecated
    public CreatureSpawner getSpawner() {
        return (CreatureSpawner) this.block.getState();
    }

}
