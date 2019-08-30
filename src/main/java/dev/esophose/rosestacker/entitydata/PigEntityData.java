package dev.esophose.rosestacker.entitydata;

import org.bukkit.Location;
import org.bukkit.entity.Pig;

import java.util.function.Consumer;

public class PigEntityData extends GenericEntityData<Pig> {

    private boolean saddled;

    public PigEntityData(Pig pig) {
        super(pig);
        this.saddled = pig.hasSaddle();
    }

    @Override
    protected Pig spawnAtInternal(Location location, Consumer<Pig> action) {
        return location.getWorld().spawn(location, Pig.class, (entity) -> {
            action.accept(entity);
            entity.setSaddle(this.saddled);
        });
    }

}
