package dev.esophose.sparkstacker.event;

import java.lang.reflect.Field;
import java.util.List;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class AsyncEntityDeathEvent extends EntityDeathEvent {

    private static Field asyncField;
    static {
        try {
            asyncField = Event.class.getDeclaredField("async");
            asyncField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public AsyncEntityDeathEvent(LivingEntity what, List<ItemStack> drops, int droppedExp) {
        super(what, drops, droppedExp);
        try {
            asyncField.set(this, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
