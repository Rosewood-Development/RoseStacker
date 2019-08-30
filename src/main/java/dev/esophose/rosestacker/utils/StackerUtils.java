package dev.esophose.rosestacker.utils;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class StackerUtils {

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
        while (amount > 0) {
            ItemStack newItemStack = itemStack.clone();
            int toTake = Math.min(amount, itemStack.getMaxStackSize());
            newItemStack.setAmount(toTake);
            amount -= toTake;
            location.getWorld().dropItemNaturally(location, newItemStack);
        }
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