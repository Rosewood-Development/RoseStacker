package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.event.BlockStackEvent;
import dev.rosewood.rosestacker.event.BlockUnstackEvent;
import dev.rosewood.rosestacker.event.EntityStackClearEvent;
import dev.rosewood.rosestacker.event.EntityStackEvent;
import dev.rosewood.rosestacker.event.EntityUnstackEvent;
import dev.rosewood.rosestacker.event.ItemStackClearEvent;
import dev.rosewood.rosestacker.event.ItemStackEvent;
import dev.rosewood.rosestacker.event.SpawnerStackEvent;
import dev.rosewood.rosestacker.event.SpawnerUnstackEvent;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackedEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEnterBlockEvent;

public class BeeListener implements Listener {

    private RoseStacker roseStacker;

    public BeeListener(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBeeEnterHive(EntityEnterBlockEvent event) {
        if (event.getEntityType() != EntityType.BEE)
            return;

        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        if (!stackManager.isEntityStackingEnabled())
            return;

        Bee beeEntity = (Bee) event.getEntity();

        StackedEntity stackedEntity = stackManager.getStackedEntity(beeEntity);
        if (stackedEntity == null)
            return;

        if (stackedEntity.getStackSize() == 1) {
            stackManager.removeEntityStack(stackedEntity);
            return;
        }

        beeEntity.setCustomName(stackedEntity.getOriginalCustomName());
        Bukkit.getScheduler().runTask(this.roseStacker, stackedEntity::decreaseStackSize);
    }

    @EventHandler
    public void onBlockStack(BlockStackEvent event) {
        System.out.println(String.format("BlockStackEvent: %s | %s", event.getPlayer().getName(), event.getStack().getStackSize() + " + " + event.getIncreaseAmount()));
    }

    @EventHandler
    public void onBlockUnstack(BlockUnstackEvent event) {
        System.out.println(String.format("BlockUnstackEvent: %s | %s", event.getPlayer() == null ? "null" : event.getPlayer().getName(), event.getStack().getStackSize() + " - " + event.getDecreaseAmount()));
    }

    @EventHandler
    public void onBlockStack(SpawnerStackEvent event) {
        System.out.println(String.format("SpawnerStackEvent: %s | %s", event.getPlayer().getName(), event.getStack().getStackSize() + " + " + event.getIncreaseAmount()));
    }

    @EventHandler
    public void onBlockUnstack(SpawnerUnstackEvent event) {
        System.out.println(String.format("SpawnerUnstackEvent: %s | %s", event.getPlayer() == null ? "null" : event.getPlayer().getName(), event.getStack().getStackSize() + " - " + event.getDecreaseAmount()));
    }

    @EventHandler
    public void onEntityStackClear(EntityStackClearEvent event) {
        System.out.println(String.format("EntityStackClearEvent: %s | %d", event.getWorld().getName(), event.getStacks().size()));
    }

    @EventHandler
    public void onItemStackClear(ItemStackClearEvent event) {
        System.out.println(String.format("ItemStackClearEvent: %s | %d", event.getWorld().getName(), event.getStacks().size()));
    }

    @EventHandler
    public void onItemStack(ItemStackEvent event) {
        System.out.println(String.format("ItemStackEvent: %s | %s", event.getStack().getItem().getType(), event.getStack().getStackSize() + " + " + event.getTarget().getStackSize()));
    }

    @EventHandler
    public void onEntityStack(EntityStackEvent event) {
        System.out.println(String.format("EntityStackEvent: %s | %s", event.getStack().getEntity().getType(), event.getStack().getStackSize() + " + " + event.getTargets().stream().mapToInt(StackedEntity::getStackSize).sum()));
    }

    @EventHandler
    public void onEntityUnstack(EntityUnstackEvent event) {
        System.out.println(String.format("EntityUnstackEvent: %s | %s", event.getStack().getEntity().getType(), event.getStack().getStackSize() + " - " + event.getResult().getStackSize()));
    }

}
