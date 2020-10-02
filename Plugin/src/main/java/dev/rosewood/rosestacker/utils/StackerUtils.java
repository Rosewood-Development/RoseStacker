package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.Lootable;
import org.bukkit.util.Vector;

public final class StackerUtils {

    public static final String MAX_SUPPORTED_VERSION = "1.16.3";
    public static final String MAX_SUPPORTED_LOCALE_VERSION = "1.16.2";

    public static final int ASSUMED_ENTITY_VISIBILITY_RANGE = 75 * 75;

    private static final Random RANDOM = new Random();
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
            if (lootable.getLootTable() == null)
                return Collections.emptySet();

            LootContext lootContext = new LootContext.Builder(lootedLocation)
                    .lootedEntity(entity)
                    .killer(killer)
                    .build();

            return lootable.getLootTable().populateLoot(RANDOM, lootContext);
        }

        return Collections.emptySet();
    }

    public static ItemStack getBlockAsStackedItemStack(Material material, int amount) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;

        BlockStackSettings stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getBlockStackSettings(material);
        String displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("block-stack-display", StringPlaceholders.builder("amount", amount)
                .addPlaceholder("name", stackSettings.getDisplayName()).build());

        itemMeta.setDisplayName(displayString);
        itemMeta.setLore(Arrays.asList(
                ItemLoreValue.STACK_SIZE.getValue(amount + "x"),
                ItemLoreValue.BLOCK_TYPE.getValue(formatName(material.name()))
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

        SpawnerStackSettings stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getSpawnerStackSettings(entityType);
        String displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display", StringPlaceholders.builder("amount", amount)
                .addPlaceholder("name", stackSettings.getDisplayName()).build());

        itemMeta.setDisplayName(displayString);
        itemMeta.setLore(Arrays.asList(
                ItemLoreValue.STACK_SIZE.getValue(amount + "x"),
                ItemLoreValue.SPAWNER_TYPE.getValue(formatName(entityType.name()))
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

        String displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("entity-stack-display-spawn-egg", StringPlaceholders.builder("amount", amount)
                .addPlaceholder("name", stackSettings.getDisplayName()).build());

        itemMeta.setDisplayName(displayString);
        itemMeta.setLore(Arrays.asList(
                ItemLoreValue.STACK_SIZE.getValue(amount + "x"),
                ItemLoreValue.ENTITY_TYPE.getValue(formatName(entityType.name()))
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
            return Integer.parseInt(lore.substring(ItemLoreValue.STACK_SIZE.getValueStripped().length(), lore.length() - 1));
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
            String name = ChatColor.stripColor(itemMeta.getLore().get(1)).replace(ItemLoreValue.SPAWNER_TYPE.getValueStripped(), "");
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
        return RANDOM.nextDouble() <= chance;
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

        int experience = RANDOM.nextInt(upperBound - lowerBound + 1) + lowerBound;

        while (experience > step) {
            world.spawn(location.clone().add(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5), ExperienceOrb.class, x -> x.setExperience(step));
            experience -= step;
        }

        if (experience > 0) {
            int fExperience = experience;
            world.spawn(location.clone().add(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5), ExperienceOrb.class, x -> x.setExperience(fExperience));
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
        RoseStacker.getInstance().getManager(StackManager.class).preStackItems(extraItems, location);
    }

    public static void damageTool(ItemStack itemStack) {
        Damageable damageable = (Damageable) itemStack.getItemMeta();
        if (damageable == null)
            return;

        damageable.setDamage(damageable.getDamage() + 1);
        itemStack.setItemMeta((ItemMeta) damageable);
    }

    public static Material getWoolMaterial(DyeColor dyeColor) {
        if (dyeColor == null)
            return Material.WHITE_WOOL;
        return Material.matchMaterial(dyeColor.name() + "_WOOL");
    }

    /**
     * @return a stream of all block materials that can be considered to be used for stacked blocks
     */
    public static List<Material> getPossibleStackableBlockMaterials() {
        Inventory inventory = Bukkit.createInventory(null, 9);
        return Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .filter(Material::isSolid)
                .filter(x -> !x.isInteractable())
                .filter(x -> !x.hasGravity())
                .filter(x -> !Tag.CORAL_PLANTS.isTagged(x))
                .filter(x -> !Tag.SLABS.isTagged(x))
                .filter(x -> !Tag.BANNERS.isTagged(x))
                .filter(x -> !x.name().endsWith("_WALL")) // Tags for these don't exist in older versions
                .filter(x -> !x.name().endsWith("_PRESSURE_PLATE"))
                .filter(x -> {
            inventory.setItem(0, new ItemStack(x));
            return inventory.getItem(0) != null && x != Material.SPAWNER;
        }).sorted(Comparator.comparing(Enum::name)).collect(Collectors.toList());
    }

    private enum ItemLoreValue {
        STACK_SIZE,
        ENTITY_TYPE,
        BLOCK_TYPE,
        SPAWNER_TYPE;

        public String getValue(Object placeholderValue) {
            LocaleManager localeManager = RoseStacker.getInstance().getManager(LocaleManager.class);
            return localeManager.getLocaleMessage("stack-item-lore-" + this.getKey()) + placeholderValue;
        }

        public String getValueStripped() {
            return ChatColor.stripColor(this.getValue(""));
        }

        private String getKey() {
            return this.name().toLowerCase().replace("_", "-");
        }
    }

}
