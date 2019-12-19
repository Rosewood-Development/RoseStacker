package dev.esophose.rosestacker.utils.reflection.nms;

import dev.esophose.rosestacker.utils.reflection.ReflectionUtils;
import java.lang.reflect.Method;

public class IRegistry {

    private static Class<?> class_IRegistry;
    private static Method method_IRegistry_getKey;

    static {
        class_IRegistry = ReflectionUtils.getNMSClass("IRegistry");
        for (Method method : class_IRegistry.getDeclaredMethods()) {
            if (method.getName().equals("getKey")) {
                method_IRegistry_getKey = method;
                break;
            }
        }
    }

    private Object registryObject;

    private IRegistry(Object registryObject) {
        this.registryObject = registryObject;
    }

    public static IRegistry getRegistry(String value) {
        try {
            return new IRegistry(class_IRegistry.getDeclaredField(value).get(null));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public MinecraftKey getKey(EntityTypes entityType) {
        try {
            return new MinecraftKey(method_IRegistry_getKey.invoke(this.registryObject, entityType.getNMS()));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

}
