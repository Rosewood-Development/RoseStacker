package dev.esophose.rosestacker.entitydata;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.io.Serializable;

public interface EntityData extends Serializable {

    LivingEntity spawnAt(Location location);

}
