package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.Lootable;
import org.bukkit.util.Vector;

public final class StackerUtils {

    private static Random random = new Random();
    private static List<EntityType> cachedAlphabeticalEntityTypes;
    private static Set<EntityType> cachedStackableEntityTypes;

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
     * Drops a List of ItemStacks into a Player's Inventory, with any overflow dropped onto the ground
     *
     * @param player The Player to give items to
     * @param itemStacks The ItemStacks to give
     */
    public static void dropItemsToPlayer(Player player, List<ItemStack> itemStacks) {
        List<ItemStack> extraItems = new ArrayList<>();
        for (ItemStack itemStack : itemStacks)
            extraItems.addAll(player.getInventory().addItem(itemStack).values());
        Location location = player.getLocation().clone().subtract(0.5, 0, 0.5);
        for (ItemStack extraItem : extraItems)
            player.getWorld().dropItemNaturally(location, extraItem);
        RoseStacker.getInstance().getManager(StackManager.class).preStackItems(extraItems, location);
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

        itemMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Stack Size: " + ChatColor.RED + amount + "x",
                ChatColor.GRAY + "Block Type: " + ChatColor.RED + formatName(material.name())
        ));

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
        itemMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Stack Size: " + ChatColor.RED + amount + "x",
                ChatColor.GRAY + "Spawner Type: " + ChatColor.RED + formatName(entityType.name())
        ));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack getEntityAsStackedItemStack(EntityType entityType, int amount) {
        EntityStackSettings stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getEntityStackSettings(entityType);
        Material spawnEggMaterial = stackSettings.getSpawnEggMaterial();
        if (spawnEggMaterial == null)
            return null;

        ItemStack itemStack = new ItemStack(spawnEggMaterial);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;

        itemMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Stack Size: " + ChatColor.RED + amount + "x",
                ChatColor.GRAY + "Entity Type: " + ChatColor.RED + formatName(entityType.name())
        ));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static int getStackedItemStackAmount(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || itemMeta.getLore() == null || itemMeta.getLore().isEmpty())
            return 1;

        String lore = ChatColor.stripColor(itemMeta.getLore().get(0));
        try {
            return Integer.parseInt(lore.substring("Stack Size: ".length(), lore.length() - 1));
        } catch (Exception ignored) { }

        return 1;
    }

    public static EntityType getStackedItemEntityType(ItemStack itemStack) {
        if (itemStack.getType() != Material.SPAWNER)
            return null;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return EntityType.PIG;

        // Use the lore to determine the type
        if (itemMeta.getLore() != null && itemMeta.getLore().size() >= 2) {
            String name = ChatColor.stripColor(itemMeta.getLore().get(1)).replace("Spawner Type: ", "");
            try {
                return EntityType.valueOf(name.toUpperCase().replaceAll(" ", "_"));
            } catch (Exception ignored) { }
        }

        // Use the name to deterine the type, name must be colored
        String name = ChatColor.stripColor(itemMeta.getDisplayName());
        if (!name.equals(itemMeta.getDisplayName())) {
            try {
                // This tries to support other spawner plugins by checking the item name
                name = name.toUpperCase();
                int spawnerIndex = name.indexOf("SPAWNER");
                String entityName = name.substring(0, spawnerIndex);
                return EntityType.valueOf(entityName.replaceAll(" ", "_"));
            } catch (Exception ignored) { }
        }

        return EntityType.PIG;
    }

    /**
     * A line of sight algorithm to check if two entities can see each other without obstruction
     *
     * @param entity1 The first entity
     * @param entity2 The second entity
     * @param accuracy How often should we check for obstructions? Smaller numbers = more checks (Recommended 0.75)
     * @param requireOccluding Should occluding blocks be required to count as a solid block?
     * @return true if the entities can see each other, otherwise false
     */
    public static boolean hasLineOfSight(Entity entity1, Entity entity2, double accuracy, boolean requireOccluding) {
        Location location1 = entity1.getLocation().clone();
        Location location2 = entity2.getLocation().clone();

        if (entity1 instanceof LivingEntity)
            location1.add(0, ((LivingEntity) entity1).getEyeHeight(), 0);
        if (entity2 instanceof LivingEntity)
            location2.add(0, ((LivingEntity) entity2).getEyeHeight(), 0);

        Vector vector1 = location1.toVector();
        Vector vector2 = location2.toVector();
        Vector direction = vector2.clone().subtract(vector1).normalize();
        double distance = vector1.distance(vector2);
        double numSteps = distance / accuracy;
        double stepSize = distance / numSteps;
        for (double i = 0; i < distance; i += stepSize) {
            Location location = location1.clone().add(direction.clone().multiply(i));
            Block block = location.getBlock();
            Material type = block.getType();
            if (type.isSolid() && (!requireOccluding || type.isOccluding()))
                return false;
        }

        return true;
    }

    public static List<EntityType> getAlphabeticalStackableEntityTypes() {
        if (cachedAlphabeticalEntityTypes != null)
            return cachedAlphabeticalEntityTypes;

        return cachedAlphabeticalEntityTypes = Stream.of(EntityType.values())
                .filter(EntityType::isAlive)
                .filter(EntityType::isSpawnable)
                .filter(x -> x != EntityType.PLAYER && x != EntityType.ARMOR_STAND)
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toList());
    }

    public static Set<EntityType> getStackableEntityTypes() {
        if (cachedStackableEntityTypes != null)
            return cachedStackableEntityTypes;

        return cachedStackableEntityTypes = Stream.of(EntityType.values())
                .filter(EntityType::isAlive)
                .filter(EntityType::isSpawnable)
                .filter(x -> x != EntityType.PLAYER && x != EntityType.ARMOR_STAND)
                .collect(Collectors.toSet());
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

    public static boolean containsConfigSpecialCharacters(String string) {
        for (char c : string.toCharArray()) {
            // Range taken from SnakeYAML's Emitter.java
            if (!(c == '\n' || (0x20 <= c && c <= 0x7E)) &&
                    (c == 0x85 || (c >= 0xA0 && c <= 0xD7FF)
                            || (c >= 0xE000 && c <= 0xFFFD)
                            || (c >= 0x10000 && c <= 0x10FFFF))) {
                return true;
            }
        }
        return false;
    }

}
