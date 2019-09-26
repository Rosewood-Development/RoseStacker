package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;

public abstract class Manager {

    protected RoseStacker roseStacker;

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
