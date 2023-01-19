package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorage;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import javax.annotation.Nullable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

public interface StackedEntity extends Stack<EntityStackSettings> {

    LivingEntity getEntity();

    StackedEntityDataStorage getDataStorage();

    boolean isEntireStackKilledOnDeath(@Nullable Player overrideKiller);

    /**
     * @return true if the whole stack should die at once, otherwise false
     */
    default boolean isEntireStackKilledOnDeath() {
        return this.isEntireStackKilledOnDeath(null);
    }

    void killEntireStack(@Nullable EntityDeathEvent event);

    /**
     * Kills the entire entity stack and drops its loot
     */
    default void killEntireStack() {
        this.killEntireStack(null);
    }

    void killPartialStack(@Nullable EntityDeathEvent event, int amount);

}
