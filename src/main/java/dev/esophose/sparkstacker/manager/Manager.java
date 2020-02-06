package dev.esophose.sparkstacker.manager;

import dev.esophose.sparkstacker.SparkStacker;

public abstract class Manager {

    protected SparkStacker sparkStacker;

    public Manager(SparkStacker sparkStacker) {
        this.sparkStacker = sparkStacker;
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
