package dev.rosewood.rosestacker.stack.settings;

import dev.rosewood.rosestacker.spawner.SpawnerType;

public interface SpawnerStackSettings extends StackSettings {

    SpawnerType getSpawnerType();

    boolean isMobAIDisabled();

    int getSpawnCountStackSizeMultiplier();

    int getMinSpawnDelay();

    int getMaxSpawnDelay();

    int getEntitySearchRange();

    int getPlayerActivationRange();

    boolean hasUnlimitedPlayerActivationRange();

    int getSpawnRange();

}
