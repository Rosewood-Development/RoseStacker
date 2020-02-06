package dev.esophose.sparkstacker.utils.reflection.nms;

import dev.esophose.sparkstacker.utils.reflection.ReflectionUtils;
import java.lang.reflect.Method;

public class NBTTagList {

    private static Class<?> class_NBTTagList;
    private static Method method_NBTTagList_set;

    static {
        try {
            class_NBTTagList = ReflectionUtils.getNMSClass("NBTTagList");
            method_NBTTagList_set = class_NBTTagList.getDeclaredMethod("set", int.class, ReflectionUtils.getNMSClass("NBTBase"));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private Object nbtObject;

    public NBTTagList(Object nbtObject) {
        this.nbtObject = nbtObject;
    }

    public void set(int index, NBTTagDouble nbtTagDouble) {
        try {
            method_NBTTagList_set.invoke(this.nbtObject, index, nbtTagDouble.getNMS());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public Object getNMS() {
        return this.nbtObject;
    }

}
