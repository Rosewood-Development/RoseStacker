package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.event.StackGUIOpenEvent;
import dev.rosewood.rosestacker.gui.StackedSpawnerGui;
import dev.rosewood.rosestacker.manager.HologramManager;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.spawner.SpawnerType;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.ConditionTag;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
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
    private CreatureSpawner cachedCreatureSpawner;
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
        this.cachedCreatureSpawner = (CreatureSpawner) this.block.getState();
        this.spawnerTile = NMSAdapter.getHandler().injectStackedSpawnerTile(this);
        this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getSpawnerStackSettings(this.spawnerTile.getSpawnerType());

        if (Bukkit.isPrimaryThread()) {
            this.updateSpawnerProperties(true);
            this.updateDisplay();
        } else {
            ThreadUtils.runSync(() -> {
                this.updateSpawnerProperties(true);
                this.updateDisplay();
            });
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

    /**
     * @return the StackedSpawnerTile object containing the most up-to-date information about this spawner
     */
    public StackedSpawnerTile getSpawnerTile() {
        return this.spawnerTile;
    }

    /**
     * @return a copy of the spawner's CreatureSpawner state
     * @implNote It is recommended to use {@link #getSpawnerTile} instead as it is updated live, this object is likely to be <i>very</i> stale
     */
    public CreatureSpawner getSpawner() {
        if (this.cachedCreatureSpawner == null && this.block.getType() == Material.SPAWNER)
            this.cachedCreatureSpawner = (CreatureSpawner) this.getBlock().getState();
        return this.cachedCreatureSpawner;
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
        if (!SettingKey.SPAWNER_DISPLAY_TAGS.get() || this.stackSettings == null)
            return;

        HologramManager hologramManager = RoseStacker.getInstance().getManager(HologramManager.class);
        LocaleManager localeManager = RoseStacker.getInstance().getManager(LocaleManager.class);

        Location location = this.getHologramLocation();

        int sizeForHologram = SettingKey.SPAWNER_DISPLAY_TAGS_SINGLE.get() ? 0 : 1;
        if (this.size <= sizeForHologram) {
            hologramManager.deleteHologram(location);
            return;
        }

        List<String> displayStrings;
        if (this.size == 1 && !SettingKey.SPAWNER_DISPLAY_TAGS_SINGLE_AMOUNT.get()) {
            displayStrings = localeManager.getLocaleMessages("spawner-hologram-display" + (this.spawnerTile.getSpawnerType().isEmpty() ? "-empty" : "") + "-single", this.getPlaceholders());
        } else {
            displayStrings = localeManager.getLocaleMessages("spawner-hologram-display" + (this.spawnerTile.getSpawnerType().isEmpty() ? "-empty" : ""), this.getPlaceholders());
        }

        hologramManager.createOrUpdateHologram(location, displayStrings);
    }

    private StringPlaceholders getPlaceholders() {
        int delay = this.spawnerTile.getDelay();
        return StringPlaceholders.builder("name", this.stackSettings.getDisplayName())
                .add("amount", StackerUtils.formatNumber(this.getStackSize()))
                .add("max_amount", StackerUtils.formatNumber(this.getStackSettings().getMaxStackSize()))
                .add("time_remaining", StackerUtils.formatTicksAsTime(delay))
                .add("ticks_remaining", StackerUtils.formatNumber(delay))
                .add("total_spawned", StackerUtils.formatNumber(PersistentDataUtils.getTotalSpawnCount(this.spawnerTile)))
                .build();
    }

    public Location getHologramLocation() {
        return this.block.getLocation().add(0.5, SettingKey.SPAWNER_DISPLAY_TAGS_HEIGHT_OFFSET.get(), 0.5);
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
        SpawnerType spawnerType = this.spawnerTile.getSpawnerType();
        this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getSpawnerStackSettings(spawnerType);
        if (this.stackSettings == null)
            return;

        if (!spawnerType.isEmpty()) {
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

        if (this.block.getState() instanceof CreatureSpawner creatureSpawner)
            this.cachedCreatureSpawner = creatureSpawner;
    }

}
