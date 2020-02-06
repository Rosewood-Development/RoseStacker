package dev.esophose.sparkstacker.utils.reflection.nms;

import dev.esophose.sparkstacker.utils.reflection.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class NBTTagCompound {

    private static Class<?> class_NBTTagCompound;
    private static Constructor<?> constructor_NBTTagCompound;
    private static Method method_NBTTagCompound_getList;
    private static Method method_NBTTagCompound_set;

    static {
        try {
            class_NBTTagCompound = ReflectionUtils.getNMSClass("NBTTagCompound");
            constructor_NBTTagCompound = class_NBTTagCompound.getConstructor();
            method_NBTTagCompound_getList = class_NBTTagCompound.getDeclaredMethod("getList", String.class, int.class);
            method_NBTTagCompound_set = class_NBTTagCompound.getDeclaredMethod("set", String.class, ReflectionUtils.getNMSClass("NBTBase"));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private Object nbtObject;

    public NBTTagCompound() {
        try {
            this.nbtObject = constructor_NBTTagCompound.newInstance();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public NBTTagCompound(Object nbtObject) {
        this.nbtObject = nbtObject;
    }

    public NBTTagList getList(String name, int index) {
        try {
            return new NBTTagList(method_NBTTagCompound_getList.invoke(this.nbtObject, name, index));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void set(String name, NBTTagList nbtTagList) {
        try {
            method_NBTTagCompound_set.invoke(this.nbtObject, name, nbtTagList.getNMS());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public Object getNMS() {
        return this.nbtObject;
    }

}
