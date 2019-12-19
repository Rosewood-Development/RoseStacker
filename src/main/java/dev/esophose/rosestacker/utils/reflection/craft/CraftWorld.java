package dev.esophose.rosestacker.utils.reflection.craft;

import dev.esophose.rosestacker.utils.reflection.ReflectionUtils;
import dev.esophose.rosestacker.utils.reflection.nms.Entity;
import dev.esophose.rosestacker.utils.reflection.nms.World;
import java.lang.reflect.Method;
import org.bukkit.Location;

public class CraftWorld {

    private static Class<?> class_CraftWorld;
    private static Method method_CraftWorld_getHandle;
    private static Method method_CraftWorld_createEntity;

    static {
        try {
            class_CraftWorld = ReflectionUtils.getCraftClass("CraftWorld");
            method_CraftWorld_getHandle = class_CraftWorld.getDeclaredMethod("getHandle");
            method_CraftWorld_createEntity = class_CraftWorld.getDeclaredMethod("createEntity", Location.class, Class.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private org.bukkit.World world;

    public CraftWorld(org.bukkit.World world) {
        this.world = world;
    }

    public World getHandle() {
        try {
            return new World(method_CraftWorld_getHandle.invoke(this.world));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Entity createEntity(Location location, Class<? extends org.bukkit.entity.Entity> entityClass) {
        try {
            return new Entity(method_CraftWorld_createEntity.invoke(class_CraftWorld.cast(this.world), location, entityClass));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

}
