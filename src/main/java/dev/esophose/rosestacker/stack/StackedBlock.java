package dev.esophose.rosestacker.stack;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.HologramManager;
import dev.esophose.rosestacker.manager.LocaleManager.Locale;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class StackedBlock implements Stack {

    private int size;
    private Block block;

    public StackedBlock(int size, Block block) {
        this.size = size;
        this.block = block;

        this.updateDisplay();
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
        HologramManager hologramManager = RoseStacker.getInstance().getHologramManager();

        Location location = this.block.getLocation().clone().add(0.5, 0.75, 0.5);

        if (this.size <= 1) {
            hologramManager.deleteHologram(location);
            return;
        }

        String displayString = Locale.STACK_DISPLAY.get()
                .replaceAll("%amount%", String.valueOf(this.size))
                .replaceAll("%name%", StackerUtils.formatName(this.block.getType().name()));

        hologramManager.createOrUpdateHologram(location, displayString);
    }

}
