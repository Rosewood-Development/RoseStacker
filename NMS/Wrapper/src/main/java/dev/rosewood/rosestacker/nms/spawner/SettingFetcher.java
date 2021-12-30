package dev.rosewood.rosestacker.nms.spawner;

/**
 * Allows fetching settings from the Plugin module
 */
public interface SettingFetcher {

    boolean allowSpawnerRedstoneToggle();

    int redstoneCheckFrequency();

}
