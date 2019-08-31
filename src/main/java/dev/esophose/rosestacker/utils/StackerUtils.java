package dev.esophose.rosestacker.utils;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.Lootable;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class StackerUtils {

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
            return null;

        String name = ChatColor.stripColor(itemMeta.getDisplayName());
        try {
            return EntityType.valueOf((name.substring(0, name.length() - 8)).toUpperCase());
        } catch (Exception ex) {
            return null;
        }
    }

}
