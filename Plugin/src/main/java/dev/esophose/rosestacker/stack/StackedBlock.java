package dev.esophose.rosestacker.stack;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.manager.HologramManager;
import dev.esophose.rosestacker.manager.LocaleManager;
import dev.esophose.rosestacker.manager.StackSettingManager;
import dev.esophose.rosestacker.stack.settings.BlockStackSettings;
import dev.esophose.rosestacker.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class StackedBlock extends Stack {

    private int size;
    private Block block;

    private BlockStackSettings stackSettings;

    public StackedBlock(int id, int size, Block block) {
        super(id);

        this.size = size;
        this.block = block;

        if (this.block != null) {
            this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getBlockStackSettings(this.block);

            if (Bukkit.isPrimaryThread())
                this.updateDisplay();
        }
    }

    public StackedBlock(int size, Block block) {
        this(-1, size, block);
    }

    public Block getBlock() {
        return this.block;
    }

    public void increaseStackSize(int amount) {
        this.size += amount;
        this.updateDisplay();
    }

    public void setStackSize(int size) {
        this.size = size;
        this.updateDisplay();
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
        if (!Setting.BLOCK_DISPLAY_TAGS.getBoolean())
            return;

        HologramManager hologramManager = RoseStacker.getInstance().getManager(HologramManager.class);

        Location location = this.block.getLocation().clone().add(0.5, 0.75, 0.5);

        if (this.size <= 1) {
            hologramManager.deleteHologram(location);
            return;
        }

        String displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("block-stack-display", StringPlaceholders.builder("amount", this.getStackSize())
                .addPlaceholder("name", this.stackSettings.getDisplayName()).build());

        hologramManager.createOrUpdateHologram(location, displayString);
    }

}
