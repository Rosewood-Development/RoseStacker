package dev.esophose.rosestacker.utils;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.Lootable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public final class StackerUtils {

    private static Random random = new Random();

    /**
     * Formats a string from THIS_FORMAT to This Format
     *
     * @param name The name to format
     * @return the reformatted string
     */
    public static String formatName(String name) {
        return WordUtils.capitalizeFully(name.toLowerCase().replace('_', ' '));
    }

    /**
     * Gets a location as a string key
     *
     * @param location The location
     * @return the location as a string key
     */
    public static String locationAsKey(Location location) {
        return String.format("%s-%.2f-%.2f-%.2f", location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

    /**
     * Drops items of the given ItemStack type on the ground
     *
     * @param location The location to drop the items
     * @param itemStack The ItemStack type to drop
     * @param amount The amount to drop
     */
    public static void dropItems(Location location, ItemStack itemStack, int amount) {
        if (location.getWorld() == null)
            return;

        while (amount > 0) {
            ItemStack newItemStack = itemStack.clone();
            int toTake = Math.min(amount, itemStack.getMaxStackSize());
            newItemStack.setAmount(toTake);
            amount -= toTake;
            location.getWorld().dropItemNaturally(location, newItemStack);
        }
    }

    /**
     * Get loot for a given entity
     *
     * @param entity The entity to drop loot for
     * @param killer The player who is killing that entity
     * @param lootedLocation The location the entity is being looted at
     * @return The loot
     */
    public static Collection<ItemStack> getEntityLoot(LivingEntity entity, Player killer, Location lootedLocation) {
        if (entity instanceof Lootable) {
            Lootable lootable = (Lootable) entity;
            Location dropLocation = entity.getLocation();
            if (lootable.getLootTable() == null || dropLocation.getWorld() == null)
                return Collections.emptySet();

            LootContext lootContext = new LootContext.Builder(lootedLocation)
                    .lootedEntity(entity)
                    .killer(killer)
                    .build();

            return lootable.getLootTable().populateLoot(random, lootContext);
        }

        return Collections.emptySet();
    }

    public static ItemStack getBlockAsStackedItemStack(Material material, int amount) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;

        itemMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Stack: " + ChatColor.RED + amount + "x"));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static boolean isSpawnEgg(Material material) {
        return material.name().endsWith("_SPAWN_EGG");
    }

    public static ItemStack getSpawnerAsStackedItemStack(EntityType entityType, int amount) {
        ItemStack itemStack = new ItemStack(Material.SPAWNER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;

        itemMeta.setDisplayName(ChatColor.RESET + formatName(entityType.name() + "_" + Material.SPAWNER.name()));
        itemMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Stack: " + ChatColor.RED + amount + "x"));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack getEntityAsStackedItemStack(EntityType entityType, int amount) {
        EntityStackSettings stackSettings = RoseStacker.getInstance().getStackSettingManager().getEntityStackSettings(entityType);
        Material spawnEggMaterial = stackSettings.getSpawnEggMaterial();
        if (spawnEggMaterial == null)
            return new ItemStack(Material.AIR);

        ItemStack itemStack = new ItemStack(spawnEggMaterial);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;

        itemMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Stack: " + ChatColor.RED + amount + "x"));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static int getStackedItemStackAmount(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || itemMeta.getLore() == null || itemMeta.getLore().isEmpty())
            return 1;

        String lore = ChatColor.stripColor(itemMeta.getLore().get(0));
        if (lore.contains("Stack: ")) {
            try {
                return Integer.parseInt(lore.substring(7, lore.length() - 1));
            } catch (Exception ex) {
                return 1;
            }
        }

        return 1;
    }

    public static EntityType getStackedItemEntityType(ItemStack itemStack) {
        if (itemStack.getType() != Material.SPAWNER)
            return null;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return EntityType.PIG;

        String name = ChatColor.stripColor(itemMeta.getDisplayName());
        try {
            return EntityType.valueOf((name.substring(0, name.length() - 8)).toUpperCase().replaceAll(" ", "_"));
        } catch (Exception ex) {
            return EntityType.PIG;
        }
    }

    public static boolean hasLineOfSight(LivingEntity entity1, LivingEntity entity2) {
        Location location1 = entity1.getLocation().clone().add(0, entity1.getEyeHeight(), 0);
        Location location2 = entity2.getLocation().clone().add(0, entity2.getEyeHeight(), 0);

        World world = location1.getWorld();

        if (world == null)
            return false;

        Vector direction = location2.toVector().subtract(location1.toVector()).normalize();
        if (Double.isNaN(direction.getX()) || Double.isNaN(direction.getY()) || Double.isNaN(direction.getZ()) ||
            Double.isInfinite(direction.getX()) || Double.isInfinite(direction.getY()) || Double.isInfinite(direction.getZ()))
            return false;

        int distance = (int) Math.round(location1.distance(location2));

        try {
            int maxChecks = distance * 2;
            int currentChecks = 0;
            BlockIterator blockIterator = new BlockIterator(world, location1.toVector(), direction, 0, distance);
            while (blockIterator.hasNext()) {
                Block block = blockIterator.next();
                if (block.getType().isSolid())
                    return false;
                if (currentChecks++ > maxChecks)
                    break;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static List<EntityType> getStackableEntityTypes() {
        return Stream.of(EntityType.values())
                .filter(EntityType::isAlive)
                .filter(EntityType::isSpawnable)
                .filter(x -> x != EntityType.PLAYER && x != EntityType.ARMOR_STAND)
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toList());
    }

    public static void takeOneItem(Player player, EquipmentSlot handType) {
        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        ItemStack itemStack = handType == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
        int newAmount = itemStack.getAmount() - 1;
        if (newAmount <= 0) {
            if (handType == EquipmentSlot.HAND) {
                player.getInventory().setItemInMainHand(null);
            } else {
                player.getInventory().setItemInOffHand(null);
            }
        } else {
            itemStack.setAmount(newAmount);
        }
    }

    public static boolean passesChance(double chance) {
        return random.nextDouble() <= chance;
    }

    /**
     * Drops experience at a given location
     *
     * @param location to spawn experience
     * @param lowerBound minimum amount to drop
     * @param upperBound maximum amount to drop
     * @param step the max size an orb can be, will drop multiple orbs if this is exceeded
     */
    public static void dropExperience(Location location, int lowerBound, int upperBound, int step) {
        World world = location.getWorld();
        if (world == null)
            return;

        int experience = random.nextInt(upperBound - lowerBound + 1) + lowerBound;

        while (experience > step) {
            ExperienceOrb orb = world.spawn(location.clone().add(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5), ExperienceOrb.class);
            orb.setExperience(experience);
            experience -= step;
        }

        if (experience > 0) {
            ExperienceOrb orb = world.spawn(location.clone().add(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5), ExperienceOrb.class);
            orb.setExperience(experience);
        }
    }

    public static void dropToInventory(Player player, ItemStack itemStack) {
        Map<Integer, ItemStack> remaining = player.getInventory().addItem(itemStack);
        ItemStack remainingItem = remaining.get(0);
        if (remainingItem != null)
            player.getWorld().dropItemNaturally(player.getLocation(), remainingItem);
    }

}
