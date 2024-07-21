package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.EntitySpawnUtil;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Statistic;
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
        if (!(entity instanceof Animals animal) || !animal.canBreed())
            return;

        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        if (!stackManager.isEntityStackingEnabled())
            return;

        StackedEntity stackedEntity = stackManager.getStackedEntity(animal);
        if (stackedEntity == null)
            return;

        Player player = event.getPlayer();
        EntityStackSettings stackSettings = stackedEntity.getStackSettings();
        ItemStack breedingItem = player.getInventory().getItem(event.getHand());
        if (breedingItem == null || !stackSettings.getEntityTypeData().isValidBreedingMaterial(breedingItem.getType()) || (player.getGameMode() != GameMode.CREATIVE && breedingItem.getAmount() < 2))
            return;

        if (PersistentDataUtils.isAiDisabled(animal) && SettingKey.SPAWNER_DISABLE_MOB_AI_OPTIONS_DISABLE_BREEDING.get()) {
            event.setCancelled(true);
            return;
        }

        if (!SettingKey.ENTITY_CUMULATIVE_BREEDING.get())
            return;

        int stackSize = stackedEntity.getStackSize();
        if (stackSize < 2)
            return;

        Class<? extends Entity> entityClass = animal.getType().getEntityClass();
        if (entityClass == null)
            return;

        event.setCancelled(true);

        // Take the items for breeding
        int totalChildren;
        if (player.getGameMode() != GameMode.CREATIVE) {
            int requiredFood = Math.min(stackSize, breedingItem.getAmount());
            breedingItem.setAmount(breedingItem.getAmount() - requiredFood);
            totalChildren = requiredFood / 2;
        } else {
            // Creative mode should allow the entire stack to breed half as many babies as the total size
            totalChildren = stackSize / 2;
        }

        // Reset breeding timer and play the breeding effect
        animal.setAge(6000);
        animal.setBreedCause(player.getUniqueId());
        animal.playEffect(EntityEffect.LOVE_HEARTS);

        boolean disableAi = PersistentDataUtils.isAiDisabled(animal);

        // Drop experience and spawn entities a few ticks later
        int f_totalChildren = totalChildren;
        ThreadUtils.runSyncDelayed(() -> {
            for (int i = 0; i < f_totalChildren; i++)
                EntitySpawnUtil.spawn(animal.getLocation(), entityClass, x -> {
                    Ageable baby = (Ageable) x;
                    baby.setBaby();
                    if (disableAi)
                        PersistentDataUtils.removeEntityAi(baby);
                });

            StackerUtils.dropExperience(animal.getLocation(), totalChildren, 7 * totalChildren, totalChildren);

            // Increment statistic
            player.incrementStatistic(Statistic.ANIMALS_BRED, totalChildren);
        }, 30);
    }

}
