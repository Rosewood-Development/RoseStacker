package dev.rosewood.rosestacker.nms.hologram;

import org.bukkit.Location;

public class HologramLine {

    private final int entityId;
    private final Location location;
    private String text;
    private boolean dirty;

    public HologramLine(int entityId, Location location, String text) {
        this.entityId = entityId;
        this.location = location.clone();
        this.text = text;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Location getLocation() {
        return this.location;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        if (this.text.equals(text))
            return;

        this.text = text;
        this.dirty = true;
    }

    /**
     * Checks if the text needs to be updated and marks it as no longer dirty if it does
     *
     * @return true if the text needs to be updated, false otherwise
     */
    public boolean checkDirty() {
        boolean value = this.dirty;
        this.dirty = false;
        return value;
    }

}
