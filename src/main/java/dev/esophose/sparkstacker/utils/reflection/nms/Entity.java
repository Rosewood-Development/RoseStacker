package dev.esophose.sparkstacker.utils.reflection.nms;

import dev.esophose.sparkstacker.utils.reflection.ReflectionUtils;
import java.lang.reflect.Method;

public class Entity {

    private static Class<?> class_Entity;
    protected static Method method_Entity_getBukkitEntity;
    protected static Method method_Entity_f;
    protected static Method method_Entity_save;
    protected static Method method_Entity_getEntityType;

    static {
        try {
            class_Entity = ReflectionUtils.getNMSClass("Entity");
            method_Entity_getBukkitEntity = class_Entity.getDeclaredMethod("getBukkitEntity");
            method_Entity_f = class_Entity.getDeclaredMethod("f", ReflectionUtils.getNMSClass("NBTTagCompound"));
            method_Entity_save = class_Entity.getDeclaredMethod("save", ReflectionUtils.getNMSClass("NBTTagCompound"));
            method_Entity_getEntityType = class_Entity.getDeclaredMethod("getEntityType");
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    protected Object entityObject;

    public Entity(Object entityObject) {
        this.entityObject = entityObject;
    }

    public org.bukkit.entity.Entity getBukkitEntity() {
        try {
            return (org.bukkit.entity.Entity) method_Entity_getBukkitEntity.invoke(this.entityObject);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setNBT(NBTTagCompound nbt) {
        try {
            method_Entity_f.invoke(this.entityObject, nbt.getNMS());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public void save(NBTTagCompound nbt) {
        try {
            method_Entity_save.invoke(this.entityObject, nbt.getNMS());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public EntityTypes getEntityType() {
        try {
            return new EntityTypes(method_Entity_getEntityType.invoke(this.entityObject));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object getNMS() {
        return this.entityObject;
    }

}
