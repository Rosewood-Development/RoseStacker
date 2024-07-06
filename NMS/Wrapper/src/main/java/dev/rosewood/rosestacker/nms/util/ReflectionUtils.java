package dev.rosewood.rosestacker.nms.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A util class to aid in reflection.
 * Mainly used to deal with looking up fields and methods for remapped jars.
 */
public final class ReflectionUtils {

    private ReflectionUtils() {

    }

    /**
     * Gets a Class's field by name and makes it accessible.
     * Does not work for remapped jars, see {@link #getFieldByPositionAndType}.
     * Does not include parent class fields.
     *
     * @param clazz The class
     * @param name The name of the field
     * @return the discovered field
     * @throws IllegalStateException if the field could not be found
     */
    public static Field getFieldByName(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to get field reflectively: [" + clazz.getName() + ";" + name + "]");
        }
    }

    /**
     * Gets a Class's field by position and type and makes it accessible.
     * Does not include parent class fields.
     *
     * @param clazz The class
     * @param index The index of the field, relative to all other fields with the same type
     * @param type The type of field
     * @return the discovered field
     * @throws IllegalStateException if the field could not be found
     */
    public static Field getFieldByPositionAndType(Class<?> clazz, int index, Class<?> type) {
        int n = 0;
        for (Field field : clazz.getDeclaredFields()) {
            if (type.isAssignableFrom(field.getType()) && n++ == index) {
                field.setAccessible(true);
                return field;
            }
        }

        throw new IllegalStateException("Failed to get field reflectively: [" + clazz.getName() + ";" + type.getName() + ";" + index + "]");
    }

    /**
     * Gets a Class's method by name and parameter types and makes it accessible.
     * Does not work for remapped jars, see {@link #getMethodByPositionAndTypes}.
     *
     * @param clazz The class
     * @param name The name of the method
     * @param parameterTypes The parameter types of the method
     * @return the discovered method
     * @throws IllegalStateException if the method could not be found
     */
    public static Method getMethodByName(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException e) {
            try {
                Method method = clazz.getMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (ReflectiveOperationException e2) {
                throw new IllegalStateException("Failed to get method reflectively: [" + clazz.getName() + ";" + Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(";")) + "]");
            }
        }
    }

    /**
     * Gets a Class's method by position and parameter types and makes it accessible.
     * Does not include parent class methods.
     *
     * @param clazz The class
     * @param index The index of the method, relative to all other methods with the same parameter types
     * @param parameterTypes The types of the parameters
     * @return the discovered method
     */
    public static Method getMethodByPositionAndTypes(Class<?> clazz, int index, Class<?>... parameterTypes) {
        int n = 0;
        outer:
        for (Method method : clazz.getDeclaredMethods()) {
            Class<?>[] types = method.getParameterTypes();
            if (types.length != parameterTypes.length)
                continue;

            for (int i = 0; i < types.length; i++)
                if (!parameterTypes[i].isAssignableFrom(types[i]))
                    continue outer;

            if (n++ == index) {
                method.setAccessible(true);
                return method;
            }
        }

        throw new IllegalStateException("Failed to get method reflectively: [" + clazz.getName() + ";" + index + ";" + Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(";")) + "]");
    }

    /**
     * Gets a Class's constructor by parameter types and makes it accessible.
     *
     * @param clazz The class
     * @param parameterTypes The parameter types of the constructor
     * @return the discovered constructor
     * @throws IllegalStateException if the constructor could not be found
     */
    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to get constructor reflectively: [" + clazz.getName() + ";" + Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(";")) + "]");
        }
    }

}
