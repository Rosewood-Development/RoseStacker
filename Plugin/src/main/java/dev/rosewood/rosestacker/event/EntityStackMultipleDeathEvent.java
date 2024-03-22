package dev.rosewood.rosestacker.event;

import com.google.common.collect.Multimap;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedEntity;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Only called when trigger-death-event-for-entire-stack-kill is disabled in the config.
 * This can be checked with {@link RoseStackerAPI#isEntityStackMultipleDeathEventCalled()}
 * Called when multiple entities in a stack are killed at the same time.
 * May be called async depending on the value of the death-event-trigger-async config setting.
 */
public class EntityStackMultipleDeathEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StackedEntity stackedEntity;
    private final Multimap<LivingEntity, EntityDrops> entityDrops;
    private final int originalStackSize;
    private final int entityKillCount;


    /**
     * @param stackedEntity The entity being killed
     * @param entityDrops A Map of the entities being killed and their drops
     * @param originalStackSize The original stack size
     * @param entityKillCount The number of entities being killed
     */
    public EntityStackMultipleDeathEvent(@NotNull StackedEntity stackedEntity,
                                         @NotNull Multimap<LivingEntity, EntityDrops> entityDrops,
                                         int originalStackSize, int entityKillCount) {
        super(!Bukkit.isPrimaryThread());

        this.stackedEntity = stackedEntity;
        this.entityDrops = entityDrops;
        this.originalStackSize = originalStackSize;
        this.entityKillCount = entityKillCount;
    }

    /**
     * @return the StackedEntity being killed
     */
    @NotNull
    public StackedEntity getStack() {
        return this.stackedEntity;
    }

    /**
     * @return a Multimap of the entities being killed and their drops
     */
    @NotNull
    public Multimap<LivingEntity, EntityDrops> getEntityDrops() {
        return this.entityDrops;
    }

    /**
     * @return the original stack size
     */
    public int getOriginalStackSize() {
        return this.originalStackSize;
    }

    /**
     * @return the number of entities being killed
     */
    public int getEntityKillCount() {
        return this.entityKillCount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public static class EntityDrops {
        
        private final List<ItemStack> drops;
        private int experience;
        
        public EntityDrops(@NotNull List<ItemStack> drops, int experience) {
            this.drops = drops;
            this.experience = experience;
        }
        
        @NotNull
        public List<ItemStack> getDrops() {
            return this.drops;
        }
        
        public int getExperience() {
            return this.experience;
        }

        public void setExperience(int experience) {
            this.experience = experience;
        }

    }

}
