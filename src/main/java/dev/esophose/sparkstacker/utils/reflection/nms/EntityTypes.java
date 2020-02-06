package dev.esophose.sparkstacker.utils.reflection.nms;

import dev.esophose.sparkstacker.utils.reflection.ReflectionUtils;
import java.lang.reflect.Method;
import java.util.Optional;

public class EntityTypes {

    private static Class<?> class_EntityTypes;
    private static Method method_EntityTypes_a;
    private static Method method_EntityTypes_spawnCreature;

    static {
        try {
            class_EntityTypes = ReflectionUtils.getNMSClass("EntityTypes");
            method_EntityTypes_a = class_EntityTypes.getDeclaredMethod("a", String.class);
            method_EntityTypes_spawnCreature = class_EntityTypes.getDeclaredMethod("spawnCreature",
                    ReflectionUtils.getNMSClass("World"),
                    ReflectionUtils.getNMSClass("NBTTagCompound"),
                    ReflectionUtils.getNMSClass("IChatBaseComponent"),
                    ReflectionUtils.getNMSClass("EntityHuman"),
                    ReflectionUtils.getNMSClass("BlockPosition"),
                    ReflectionUtils.getNMSClass("EnumMobSpawn"),
                    boolean.class,
                    boolean.class
            );
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private Object typeObject;

    public EntityTypes(Object typeObject) {
        this.typeObject = typeObject;
    }

    public static Optional<EntityTypes> getTypeByName(String name) {
        try {
            Optional<?> optional = (Optional<?>) method_EntityTypes_a.invoke(null, name);
            return optional.map(EntityTypes::new);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Object spawnCreature(World world, NBTTagCompound nbt, BlockPosition blockPosition, EnumMobSpawn enumMobSpawn) {
        try {
            return method_EntityTypes_spawnCreature.invoke(
                    this.typeObject,
                    world.getNMS(),
                    nbt.getNMS(),
                    null,
                    null,
                    blockPosition.getNMS(),
                    enumMobSpawn.getNMS(),
                    true,
                    false
            );
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object getNMS() {
        return this.typeObject;
    }

}
