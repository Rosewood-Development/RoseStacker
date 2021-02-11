package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class BreedingListener implements Listener {

    private final RosePlugin rosePlugin;

    public BreedingListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreed(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Animals))
            return;

        Animals animal = (Animals) entity;
        if (!animal.canBreed())
            return;

        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (!stackManager.isEntityStackingEnabled())
            return;

        StackedEntity stackedEntity = stackManager.getStackedEntity(animal);
        if (stackedEntity == null)
            return;

        int stackSize = stackedEntity.getStackSize();
        if (stackSize < 2)
            return;

        Player player = event.getPlayer();
        EntityStackSettings stackSettings = stackedEntity.getStackSettings();
        ItemStack breedingItem = player.getInventory().getItem(event.getHand());
        if (!stackSettings.getEntityTypeData().isValidBreedingMaterial(breedingItem.getType()) ||
                (player.getGameMode() != GameMode.CREATIVE && breedingItem.getAmount() < 2))
            return;

        Class<? extends Entity> entityClass = animal.getType().getEntityClass();
        if (entityClass == null)
            return;

        event.setCancelled(true);

        // Total children should be half the stack size, rounded down
        int totalChildren = stackSize / 2;

        // Take the items for breeding
        if (player.getGameMode() != GameMode.CREATIVE) {
            int requiredFood = Math.min(totalChildren, breedingItem.getAmount());
            breedingItem.setAmount(breedingItem.getAmount() - requiredFood);
            totalChildren = requiredFood;
        }

        // Reset breeding timer and play the breeding effect
        animal.setAge(6000);
        animal.setBreedCause(player.getUniqueId());
        animal.playEffect(EntityEffect.LOVE_HEARTS);

        boolean disableAi = PersistentDataUtils.isAiDisabled(animal);

        // Drop experience and spawn entities a few ticks later
        int f_totalChildren = totalChildren;
        Bukkit.getScheduler().runTaskLater(this.rosePlugin, () -> {
            for (int i = 0; i < f_totalChildren; i++)
                animal.getWorld().spawn(animal.getLocation(), entityClass, x -> {
                    Ageable baby = (Ageable) x;
                    baby.setBaby();
                    if (disableAi)
                        PersistentDataUtils.removeEntityAi(baby);
                });

            StackerUtils.dropExperience(animal.getLocation(), stackSize, 7 * stackSize, 10);
        }, 30);
    }

}
