package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.gui.StackedBlockGui;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.HologramManager;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class StackedBlock extends Stack {

    private int size;
    private Block block;
    private StackedBlockGui stackedBlockGui;

    private BlockStackSettings stackSettings;

    public StackedBlock(int id, int size, Block block) {
        super(id);

        this.size = size;
        this.block = block;
        this.stackedBlockGui = null;

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

    public boolean isLocked() {
        if (this.stackedBlockGui == null)
            return false;
        return this.stackedBlockGui.hasViewers();
    }

    public void kickOutGuiViewers() {
        this.stackedBlockGui.kickOutViewers();
    }

    public void increaseStackSize(int amount) {
        this.size += amount;

        this.updateDisplay();
    }

    public void setStackSize(int size) {
        this.size = size;

        this.updateDisplay();
    }

    public void openGui(Player player) {
        if (this.stackedBlockGui == null)
            this.stackedBlockGui = new StackedBlockGui(this);
        this.stackedBlockGui.openFor(player);
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
        if (!Setting.BLOCK_DISPLAY_TAGS.getBoolean() || this.stackSettings == null)
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
