package dev.esophose.sparkstacker.utils.reflection.nms;

public class World {

    private Object worldObject;

    public World(Object worldObject) {
        this.worldObject = worldObject;
    }

    public Object getNMS() {
        return this.worldObject;
    }

}
