package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.EntitySpawnUtil;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import io.papermc.paper.entity.CollarColorable;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Statistic;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;

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
            // Creative mode should allow the entire stack to breed half as many babies as the max stack size of the
            // item they are holding, without actually taking any items
            totalChildren = Math.max(1, breedingItem.getMaxStackSize() / 2);
        }

        // Reset breeding timer and play the breeding effect
        animal.setAge(6000);
        animal.setBreedCause(player.getUniqueId());
        animal.playEffect(EntityEffect.LOVE_HEARTS);

        boolean disableAi = PersistentDataUtils.isAiDisabled(animal);
        // Drop experience and spawn entities a few ticks later
        ThreadUtils.runSyncDelayed(() -> {
            ItemStack breedingItemCopy = breedingItem.clone();
            breedingItemCopy.setAmount(1);
            boolean callEvents = SettingKey.ENTITY_CUMULATIVE_BREEDING_TRIGGER_BREED_EVENT.get();
            int totalExperience = callEvents ? 0 : totalChildren * 7;
            boolean modern = NMSUtil.getVersionNumber() >= 21 && NMSUtil.isPaper();
            for (int i = 0; i < totalChildren; i++) {
                LivingEntity child;
                if (modern) {
                    child = (LivingEntity) animal.getLocation().getWorld().spawn(animal.getLocation(), entityClass, CreatureSpawnEvent.SpawnReason.BREEDING, x -> {
                        Ageable baby = (Ageable) x;
                        baby.setBaby();
                        this.transferEntityProperties(animal, baby);
                        if (disableAi)
                            PersistentDataUtils.removeEntityAi(baby);
                    });
                } else {
                    child = (LivingEntity) EntitySpawnUtil.spawn(animal.getLocation(), entityClass, x -> {
                        Ageable baby = (Ageable) x;
                        baby.setBaby();
                        this.transferEntityProperties(animal, baby);
                        if (disableAi)
                            PersistentDataUtils.removeEntityAi(baby);
                    });
                }

                if (callEvents) {
                    EntityBreedEvent breedEvent = new EntityBreedEvent(child, animal, animal, player, breedingItemCopy.clone(), 7);
                    Bukkit.getPluginManager().callEvent(breedEvent);
                    if (breedEvent.isCancelled()) {
                        child.remove();
                        breedingItem.setAmount(breedingItem.getAmount() + 2);
                    } else {
                        totalExperience += breedEvent.getExperience();
                    }
                }
            }

            StackerUtils.dropExperience(animal.getLocation(), totalChildren, totalExperience, totalChildren);

            // Increment statistic
            player.incrementStatistic(Statistic.ANIMALS_BRED, totalChildren);
        }, 30);
    }

    private void transferEntityProperties(LivingEntity parent, LivingEntity child) {
        if (parent instanceof Colorable colorableParent && child instanceof Colorable colorableChild)
            colorableChild.setColor(colorableParent.getColor());

        if (parent instanceof Tameable tameableParent && child instanceof Tameable tameableChild)
            tameableChild.setOwner(tameableParent.getOwner());

        if (NMSUtil.isPaper() && NMSUtil.getVersionNumber() >= 19 && parent instanceof CollarColorable collarColorableParent && child instanceof CollarColorable collarColorableChild)
            collarColorableChild.setCollarColor(collarColorableParent.getCollarColor());

        if (parent instanceof Cat parentCat && child instanceof Cat childCat)
            childCat.setCatType(parentCat.getCatType());

        if (parent instanceof Fox parentFox && child instanceof Fox childFox)
            childFox.setFoxType(parentFox.getFoxType());

        if (NMSUtil.getVersionNumber() >= 19 && parent instanceof Frog parentFrog && child instanceof Frog childFrog)
            childFrog.setVariant(parentFrog.getVariant());

        if (parent instanceof Horse parentHorse && child instanceof Horse childHorse) {
            childHorse.setStyle(parentHorse.getStyle());
            childHorse.setColor(parentHorse.getColor());
        }

        if (parent instanceof MushroomCow parentMooshroom && child instanceof MushroomCow childMooshroom)
            childMooshroom.setVariant(parentMooshroom.getVariant());

        if (parent instanceof Panda parentPanda && child instanceof Panda childPanda)
            childPanda.setMainGene(parentPanda.getMainGene());

        if (parent instanceof Rabbit parentRabbit && child instanceof Rabbit childRabbit)
            childRabbit.setRabbitType(parentRabbit.getRabbitType());

        if (parent instanceof Llama parentLlama && child instanceof Llama childLlama)
            childLlama.setColor(parentLlama.getColor());

        if ((NMSUtil.getVersionNumber() > 20 || (NMSUtil.getVersionNumber() == 20 && NMSUtil.getMinorVersionNumber() >= 6)) && parent instanceof Wolf parentWolf && child instanceof Wolf childWolf)
            childWolf.setVariant(parentWolf.getVariant());
    }

}
