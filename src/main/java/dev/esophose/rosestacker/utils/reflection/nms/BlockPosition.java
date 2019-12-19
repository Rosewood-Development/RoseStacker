package dev.esophose.rosestacker.utils.reflection.nms;

import dev.esophose.rosestacker.utils.reflection.ReflectionUtils;
import java.lang.reflect.Constructor;

public class BlockPosition {

    private static Class<?> class_BlockPosition;
    private static Constructor<?> constructor_BlockPosition;

    static {
        try {
            class_BlockPosition = ReflectionUtils.getNMSClass("BlockPosition");
            constructor_BlockPosition = class_BlockPosition.getConstructor(int.class, int.class, int.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private int x, y, z;

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Object getNMS() {
        try {
            return constructor_BlockPosition.newInstance(this.x, this.y, this.z);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

}
