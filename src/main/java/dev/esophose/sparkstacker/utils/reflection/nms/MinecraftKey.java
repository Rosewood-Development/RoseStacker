package dev.esophose.sparkstacker.utils.reflection.nms;

public class MinecraftKey {

    private Object keyObject;

    public MinecraftKey(Object keyObject) {
        this.keyObject = keyObject;
    }

    @Override
    public String toString() {
        return this.keyObject.toString();
    }

}
