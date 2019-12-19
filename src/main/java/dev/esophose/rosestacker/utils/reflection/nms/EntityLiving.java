package dev.esophose.rosestacker.utils.reflection.nms;

import dev.esophose.rosestacker.utils.reflection.ReflectionUtils;
import java.lang.reflect.Method;

public class EntityLiving extends Entity {

    private static Class<?> class_EntityLiving;
    private static Method method_EntityLiving_a;

    static {
        try {
            class_EntityLiving = ReflectionUtils.getNMSClass("EntityLiving");
            method_EntityLiving_a = class_EntityLiving.getDeclaredMethod("a", ReflectionUtils.getNMSClass("DamageSource"), boolean.class);
            method_EntityLiving_a.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public EntityLiving(Object entityObject) {
        super(entityObject);
    }

    public void updateLootTable(DamageSource damageSource, boolean flag) {
        try {
            method_EntityLiving_a.invoke(this.entityObject, damageSource.getNMS(), flag);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

}
