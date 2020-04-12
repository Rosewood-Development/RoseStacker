package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosestacker.RoseStacker;

public abstract class Manager {

    protected final RoseStacker roseStacker;

    public Manager(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    /**
     * Reloads the Manager's settings
     */
    public abstract void reload();

    /**
     * Cleans up the Manager's resources
     */
    public abstract void disable();

}
