package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.event.StackGUIOpenEvent;
import dev.rosewood.rosestacker.gui.StackedBlockGui;
import dev.rosewood.rosestacker.manager.HologramManager;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class StackedBlock extends Stack<BlockStackSettings> {

    private int size;
    private final Block block;
    private StackedBlockGui stackedBlockGui;

    private BlockStackSettings stackSettings;

    public StackedBlock(int size, Block block, boolean updateDisplay) {
        this.size = size;
        this.block = block;
        this.stackedBlockGui = null;

        if (this.block != null) {
            this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getBlockStackSettings(this.block);
            if (updateDisplay)
                this.updateDisplay();
        }
    }

    public StackedBlock(int size, Block block) {
        this(size, block, true);
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
        if (this.stackedBlockGui != null)
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
        if (SettingKey.BLOCK_GUI_ONLY_ONE_PLAYER_ALLOWED.get() && this.isLocked())
            return;

        StackGUIOpenEvent event = new StackGUIOpenEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

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
        if (!SettingKey.BLOCK_DISPLAY_TAGS.get() || this.stackSettings == null)
            return;

        HologramManager hologramManager = RoseStacker.getInstance().getManager(HologramManager.class);

        Location location = this.getHologramLocation();

        if (this.size <= 1) {
            hologramManager.deleteHologram(location);
            return;
        }

        List<String> displayStrings = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessages("block-hologram-display", StringPlaceholders.builder("amount", StackerUtils.formatNumber(this.getStackSize()))
                .add("name", this.stackSettings.getDisplayName()).build());

        hologramManager.createOrUpdateHologram(location, displayStrings);
    }

    public Location getHologramLocation() {
        return this.block.getLocation().add(0.5, SettingKey.BLOCK_DISPLAY_TAGS_HEIGHT_OFFSET.get(), 0.5);
    }

    @Override
    public BlockStackSettings getStackSettings() {
        return this.stackSettings;
    }

}
