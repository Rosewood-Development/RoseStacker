package dev.esophose.rosestacker.utils.reflection.craft;

import dev.esophose.rosestacker.utils.reflection.ReflectionUtils;
import dev.esophose.rosestacker.utils.reflection.nms.EntityLiving;
import java.lang.reflect.Method;
import org.bukkit.entity.LivingEntity;

public class CraftLivingEntity {

    private static Class<?> class_CraftLivingEntity;
    private static Method method_CraftLivingEntity_getHandle;

    static {
        try {
            class_CraftLivingEntity = ReflectionUtils.getCraftClass("entity.CraftLivingEntity");
            method_CraftLivingEntity_getHandle = class_CraftLivingEntity.getDeclaredMethod("getHandle");
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private LivingEntity livingEntity;

    public CraftLivingEntity(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

    public EntityLiving getHandle() {
        try {
            return new EntityLiving(method_CraftLivingEntity_getHandle.invoke(this.livingEntity));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

}
