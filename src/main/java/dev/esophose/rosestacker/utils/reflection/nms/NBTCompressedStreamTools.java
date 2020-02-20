package dev.esophose.rosestacker.utils.reflection.nms;

import dev.esophose.rosestacker.utils.reflection.ReflectionUtils;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class NBTCompressedStreamTools {

    private static Class<?> class_NBTCompressedStreamTools;
    private static Method method_NBTCompressedStreamTools_a;
    private static Method method_NBTCompressedStreamTools_a_2;

    static {
        try {
            class_NBTCompressedStreamTools = ReflectionUtils.getNMSClass("NBTCompressedStreamTools");
            method_NBTCompressedStreamTools_a = class_NBTCompressedStreamTools.getDeclaredMethod("a", InputStream.class);
            method_NBTCompressedStreamTools_a_2 = class_NBTCompressedStreamTools.getDeclaredMethod("a", ReflectionUtils.getNMSClass("NBTTagCompound"), OutputStream.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public static NBTTagCompound decompress(InputStream inputStream) {
        try {
            return new NBTTagCompound(method_NBTCompressedStreamTools_a.invoke(null, inputStream));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void compress(NBTTagCompound nbt, OutputStream outputStream) {
        try {
            method_NBTCompressedStreamTools_a_2.invoke(null, nbt.getNMS(), outputStream);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

}
