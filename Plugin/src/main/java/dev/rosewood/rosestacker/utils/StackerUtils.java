package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.Lootable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public final class StackerUtils {

    public static final String MAX_SUPPORTED_VERSION = "1.16.4";
    public static final String MAX_SUPPORTED_LOCALE_VERSION = "1.16.4";

    public static final int ASSUMED_ENTITY_VISIBILITY_RANGE = 75 * 75;

    public static final DustOptions STACKABLE_DUST_OPTIONS = new DustOptions(Color.fromRGB(0x00FF00), 1.5F);
    public static final DustOptions UNSTACKABLE_DUST_OPTIONS = new DustOptions(Color.fromRGB(0xFF0000), 1.5F);

    private static final String UNSTACKABLE_METADATA_NAME = "unstackable";
    private static final String SPAWN_REASON_METADATA_NAME = "spawn_reason";
    private static final String NO_AI_METADATA_NAME = "no_ai";
    private static ItemStack cachedStackingTool;

    private static final Random RANDOM = new Random();
    private static List<EntityType> cachedAlphabeticalEntityTypes;
    private static Set<EntityType> cachedStackableEntityTypes;
    private static Map<EntityType, BoundingBox> cachedBoundingBoxes;

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
     * Formats a Material name from THIS_FORMAT to This Format
     *
     * @param material The material to format the name of
     * @return the reformatted string
     */
    public static String formatMaterialName(Material material) {
        if (material == Material.TNT)
            return "TNT"; // The one exception
        return formatName(material.name());
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
        if (amount == 1)
            return itemStack;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;

        BlockStackSettings stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getBlockStackSettings(material);
        String displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("block-stack-display", StringPlaceholders.builder("amount", amount)
                .addPlaceholder("name", stackSettings.getDisplayName()).build());

        itemMeta.setDisplayName(displayString);
        itemStack.setItemMeta(itemMeta);

        // Set stack size
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        itemStack = nmsHandler.setItemStackNBT(itemStack, "StackSize", amount);

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
        String displayString;
        if (amount == 1) {
            displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display-single", StringPlaceholders.builder("amount", amount)
                    .addPlaceholder("name", stackSettings.getDisplayName()).build());
        } else {
            displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display", StringPlaceholders.builder("amount", amount)
                    .addPlaceholder("name", stackSettings.getDisplayName()).build());
        }

        itemMeta.setDisplayName(displayString);

        // Set stack size and spawned entity type
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        itemStack = nmsHandler.setItemStackNBT(itemStack, "StackSize", amount);
        itemStack = nmsHandler.setItemStackNBT(itemStack, "EntityType", entityType.name());

        // Set the spawned type directly onto the spawner item for hopeful compatibility with other plugins
        BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
        CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
        creatureSpawner.setSpawnedType(entityType);
        blockStateMeta.setBlockState(creatureSpawner);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static ItemStack getEntityAsStackedItemStack(EntityType entityType, int amount) {
        EntityStackSettings stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getEntityStackSettings(entityType);
        Material spawnEggMaterial = stackSettings.getSpawnEggMaterial();
        if (spawnEggMaterial == null)
            return null;

        ItemStack itemStack = new ItemStack(spawnEggMaterial);
        if (amount == 1)
            return itemStack;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;

        String displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("entity-stack-display-spawn-egg", StringPlaceholders.builder("amount", amount)
                .addPlaceholder("name", stackSettings.getDisplayName()).build());

        itemMeta.setDisplayName(displayString);
        itemStack.setItemMeta(itemMeta);

        // Set stack size
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        itemStack = nmsHandler.setItemStackNBT(itemStack, "StackSize", amount);

        return itemStack;
    }

    public static int getStackedItemStackAmount(ItemStack itemStack) {
        // First, check the NBT
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        int stackSize = nmsHandler.getItemStackNBTInt(itemStack, "StackSize");
        if (stackSize > 0)
            return stackSize;

        // Fall back to the legacy lore checking
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

        // First, check the NBT
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        String entityTypeName = nmsHandler.getItemStackNBTString(itemStack, "EntityType");
        if (!entityTypeName.isEmpty()) {
            try {
                return EntityType.valueOf(entityTypeName);
            } catch (Exception ignored) { }
        }

        // Try checking the spawner data then?
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return EntityType.PIG;

        BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
        CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
        if (creatureSpawner.getSpawnedType() != EntityType.PIG)
            return creatureSpawner.getSpawnedType();

        // Fall back to the legacy lore checking
        if (itemMeta.getLore() != null && itemMeta.getLore().size() >= 2) {
            String name = ChatColor.stripColor(itemMeta.getLore().get(1)).replace(ItemLoreValue.SPAWNER_TYPE.getValueStripped(), "");
            try {
                return EntityType.valueOf(name.toUpperCase().replaceAll(" ", "_"));
            } catch (Exception ignored) { }
        }

        // Use the name to determine the type, name must be colored
        String name = ChatColor.stripColor(itemMeta.getDisplayName());
        if (!name.equals(itemMeta.getDisplayName())) {
            try {
                // This tries to support other spawner plugins by checking the item name
                name = name.toUpperCase();
                int spawnerIndex = name.indexOf("SPAWNER");
                String entityName = name.substring(0, spawnerIndex).trim();
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

    /**
     * Checks if a Player is looking at a dropped item
     *
     * @param player The Player
     * @param item The Item
     * @return true if the Player is looking at the Item, otherwise false
     */
    public static boolean isLookingAtItem(Player player, Item item) {
        Location playerLocation = player.getEyeLocation();
        Vector playerVision = playerLocation.getDirection();

        Vector playerVector = playerLocation.toVector();
        Vector itemLocation = item.getLocation().toVector().add(new Vector(0, 0.3, 0));
        Vector direction = playerVector.clone().subtract(itemLocation).normalize();

        Vector crossProduct = playerVision.getCrossProduct(direction);
        return crossProduct.lengthSquared() <= 0.01;
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
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EntityType.class)));
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
    public static void dropItemsToPlayer(Player player, Collection<ItemStack> itemStacks) {
        List<ItemStack> extraItems = new ArrayList<>();
        for (ItemStack itemStack : itemStacks)
            extraItems.addAll(player.getInventory().addItem(itemStack).values());

        if (!extraItems.isEmpty()) {
            Location location = player.getLocation().clone().subtract(0.5, 0, 0.5);
            RoseStacker.getInstance().getManager(StackManager.class).preStackItems(extraItems, location);
        }
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
                .filter(x -> !x.isInteractable() || x == Material.TNT)
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

    public static void setUnstackable(LivingEntity entity, boolean unstackable) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (unstackable) {
            if (NMSUtil.getVersionNumber() > 13) {
                entity.getPersistentDataContainer().set(new NamespacedKey(rosePlugin, UNSTACKABLE_METADATA_NAME), PersistentDataType.INTEGER, 1);
            } else {
                entity.setMetadata(UNSTACKABLE_METADATA_NAME, new FixedMetadataValue(rosePlugin, true));
            }
        } else {
            if (NMSUtil.getVersionNumber() > 13) {
                entity.getPersistentDataContainer().remove(new NamespacedKey(rosePlugin, UNSTACKABLE_METADATA_NAME));
            } else {
                entity.removeMetadata(UNSTACKABLE_METADATA_NAME, rosePlugin);
            }
        }
    }

    public static boolean isUnstackable(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            return entity.getPersistentDataContainer().has(new NamespacedKey(rosePlugin, UNSTACKABLE_METADATA_NAME), PersistentDataType.INTEGER);
        } else {
            return entity.hasMetadata(UNSTACKABLE_METADATA_NAME);
        }
    }

    /**
     * Sets the spawn reason for the given LivingEntity.
     * Does not overwrite an existing spawn reason.
     *
     * @param entity The entity to set the spawn reason of
     * @param spawnReason The spawn reason to set
     */
    public static void setEntitySpawnReason(LivingEntity entity, SpawnReason spawnReason) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(rosePlugin, SPAWN_REASON_METADATA_NAME);
            if (!dataContainer.has(key, PersistentDataType.STRING))
                dataContainer.set(key, PersistentDataType.STRING, spawnReason.name());
        } else {
            if (!entity.hasMetadata(SPAWN_REASON_METADATA_NAME))
                entity.setMetadata(SPAWN_REASON_METADATA_NAME, new FixedMetadataValue(rosePlugin, spawnReason.name()));
        }
    }

    /**
     * Gets the spawn reason of the given LivingEntity
     *
     * @param entity The entity to get the spawn reason of
     * @return The SpawnReason, or SpawnReason.CUSTOM if none is saved
     */
    public static SpawnReason getEntitySpawnReason(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            String reason = entity.getPersistentDataContainer().get(new NamespacedKey(rosePlugin, SPAWN_REASON_METADATA_NAME), PersistentDataType.STRING);
            SpawnReason spawnReason;
            if (reason != null) {
                try {
                    spawnReason = SpawnReason.valueOf(reason);
                } catch (Exception ex) {
                    spawnReason = SpawnReason.CUSTOM;
                }
            } else {
                spawnReason = SpawnReason.CUSTOM;
            }
            return spawnReason;
        } else {
            List<MetadataValue> metaValues = entity.getMetadata(SPAWN_REASON_METADATA_NAME);
            SpawnReason spawnReason = null;
            for (MetadataValue meta : metaValues) {
                try {
                    spawnReason = SpawnReason.valueOf(meta.asString());
                } catch (Exception ignored) { }
            }
            return spawnReason != null ? spawnReason : SpawnReason.CUSTOM;
        }
    }

    public static void removeEntityAi(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(rosePlugin, NO_AI_METADATA_NAME);
            if (!dataContainer.has(key, PersistentDataType.INTEGER))
                dataContainer.set(key, PersistentDataType.INTEGER, 1);
        } else {
            if (!entity.hasMetadata(NO_AI_METADATA_NAME))
                entity.setMetadata(NO_AI_METADATA_NAME, new FixedMetadataValue(rosePlugin, true));
        }

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        nmsHandler.removeEntityGoals(entity);
    }

    public static void applyDisabledAi(LivingEntity entity) {
        if (isAiDisabled(entity)) {
            NMSHandler nmsHandler = NMSAdapter.getHandler();
            nmsHandler.removeEntityGoals(entity);
        }
    }

    public static boolean isAiDisabled(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        boolean isDisabled;
        if (NMSUtil.getVersionNumber() > 13) {
            isDisabled = entity.getPersistentDataContainer().has(new NamespacedKey(rosePlugin, NO_AI_METADATA_NAME), PersistentDataType.INTEGER);
        } else {
            isDisabled = entity.hasMetadata(NO_AI_METADATA_NAME);
        }

        return isDisabled;
    }

    public static ItemStack getStackingTool() {
        if (cachedStackingTool != null)
            return cachedStackingTool;

        Material material = Material.matchMaterial(Setting.STACK_TOOL_MATERIAL.getString());
        if (material == null) {
            material = Material.STICK;
            RoseStacker.getInstance().getLogger().warning("Invalid material for stacking tool in config.yml!");
        }

        String name = HexUtils.colorify(Setting.STACK_TOOL_NAME.getString());
        List<String> lore = Setting.STACK_TOOL_LORE.getStringList().stream().map(HexUtils::colorify).collect(Collectors.toList());

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return item;

        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.values());
        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isStackingTool(ItemStack item) {
        return getStackingTool().isSimilar(item);
    }

    /**
     * Gets all blocks that an EntityType would intersect at a Location
     *
     * @param entityType The type of Entity
     * @param location The Location the Entity would be at
     * @return A List of Blocks the Entity intersects with
     */
    public static List<Block> getIntersectingBlocks(EntityType entityType, Location location) {
        BoundingBox bounds = getBoundingBox(entityType, location);
        List<Block> blocks = new ArrayList<>();
        World world = location.getWorld();
        if (world == null)
            return blocks;

        int minX = floorCoordinate(bounds.getMinX());
        int maxX = floorCoordinate(bounds.getMaxX());
        int minY = floorCoordinate(bounds.getMinY());
        int maxY = floorCoordinate(bounds.getMaxY());
        int minZ = floorCoordinate(bounds.getMinZ());
        int maxZ = floorCoordinate(bounds.getMaxZ());

        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++)
                    blocks.add(world.getBlockAt(x, y, z));

        return blocks;
    }

    /**
     * Gets the would-be bounding box of an entity at a location
     *
     * @param entityType The entity type the entity would be
     * @param location The location the entity would be at
     * @return A bounding box for the entity type at the location
     */
    public static BoundingBox getBoundingBox(EntityType entityType, Location location) {
        if (cachedBoundingBoxes == null)
            cachedBoundingBoxes = new HashMap<>();

        BoundingBox boundingBox = cachedBoundingBoxes.get(entityType);
        if (boundingBox == null) {
            boundingBox = NMSAdapter.getHandler().createEntityUnspawned(entityType, new Location(location.getWorld(), 0, 0, 0)).getBoundingBox();
            boundingBox.shift(-boundingBox.getWidthX() / 2, 0, -boundingBox.getWidthZ() / 2); // Center on the origin
            cachedBoundingBoxes.put(entityType, boundingBox);
        }

        boundingBox = boundingBox.clone();
        boundingBox.shift(location);
        return boundingBox;
    }

    private static int floorCoordinate(double value) {
        int floored = (int) value;
        return value < (double) floored ? floored - 1 : floored;
    }

    public static List<LivingEntity> deconstructStackedEntities(StackedEntity stackedEntity) {
        List<byte[]> nbtList = new LinkedList<>(stackedEntity.getStackedEntityNBT());
        List<LivingEntity> livingEntities = new ArrayList<>(nbtList.size());
        EntityType entityType = stackedEntity.getEntity().getType();
        Location location = stackedEntity.getLocation();

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        for (byte[] nbt : nbtList)
            livingEntities.add(nmsHandler.getNBTAsEntity(entityType, location, nbt));

        return livingEntities;
    }

    public static void reconstructStackedEntities(StackedEntity stackedEntity, List<? extends LivingEntity> livingEntities) {
        List<byte[]> nbtList = Collections.synchronizedList(new LinkedList<>());

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        for (LivingEntity livingEntity : livingEntities)
            nbtList.add(nmsHandler.getEntityAsNBT(livingEntity, Setting.ENTITY_SAVE_ATTRIBUTES.getBoolean()));

        stackedEntity.setStackedEntityNBT(nbtList);
    }

    public static void clearCache() {
        cachedAlphabeticalEntityTypes = null;
        cachedStackableEntityTypes = null;
        cachedBoundingBoxes = null;
        cachedStackingTool = null;
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
