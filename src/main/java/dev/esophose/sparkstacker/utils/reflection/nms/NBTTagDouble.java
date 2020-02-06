package dev.esophose.sparkstacker.utils.reflection.nms;

import dev.esophose.sparkstacker.utils.reflection.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class NBTTagDouble {

    private static Class<?> class_NBTTagDouble;
    private static Constructor<?> constructor_NBTTagDouble;
    private static Method method_NBTTagDouble_a;

    static {
        try {
            class_NBTTagDouble = ReflectionUtils.getNMSClass("NBTTagDouble");

            if (ReflectionUtils.versionNumber > 14) {
                method_NBTTagDouble_a = class_NBTTagDouble.getDeclaredMethod("a", double.class);
            } else {
                constructor_NBTTagDouble = class_NBTTagDouble.getConstructor(double.class);
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private Object nbtObject;

    public NBTTagDouble(double value) {
        try {
            if (ReflectionUtils.versionNumber > 14) {
                this.nbtObject = method_NBTTagDouble_a.invoke(null, value);
            } else {
                this.nbtObject = constructor_NBTTagDouble.newInstance(value);
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public Object getNMS() {
        return this.nbtObject;
    }

}
