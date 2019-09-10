package dev.esophose.rosestacker.stack;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.manager.HologramManager;
import dev.esophose.rosestacker.manager.LocaleManager.Locale;
import dev.esophose.rosestacker.stack.settings.SpawnerStackSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;

import java.util.regex.Matcher;

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
                this.updateDisplay();
                this.updateSpawnCount();
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
        if (!Setting.SPAWNER_DISPLAY_TAGS.getBoolean())
            return;

        HologramManager hologramManager = RoseStacker.getInstance().getHologramManager();

        Location location = this.spawner.getLocation().clone().add(0.5, 0.75, 0.5);

        if (this.size <= 1) {
            hologramManager.deleteHologram(location);
            return;
        }

        String displayString = ChatColor.translateAlternateColorCodes('&', Locale.SPAWNER_STACK_DISPLAY.get()
                .replaceAll("%amount%", String.valueOf(this.getStackSize()))
                .replaceAll("%name%", Matcher.quoteReplacement(this.stackSettings.getDisplayName())));

        hologramManager.createOrUpdateHologram(location, displayString);
    }

    private void updateSpawnCount() {
        int delay = this.spawner.getDelay();
        this.spawner.setSpawnCount(this.size * 4);
        this.spawner.setMaxNearbyEntities(6 + (this.size - 1) * 4);
        this.spawner.update();
        this.spawner.setDelay(delay);
    }

}
