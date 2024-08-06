package dev.rosewood.rosestacker.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.spawner.SpawnerType;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.ApiStatus;

public final class ItemUtils {

    private final static Map<String, ItemStack> skullCache = new HashMap<>();
    private static Field field_SkullMeta_profile;
    private static ItemStack cachedStackingTool;

    public static Material getWoolMaterial(DyeColor dyeColor) {
        if (dyeColor == null)
            return Material.WHITE_WOOL;
        return Material.matchMaterial(dyeColor.name() + "_WOOL");
    }

    public static void takeItems(int amount, Player player, EquipmentSlot handType) {
        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        ItemStack itemStack = handType == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
        int newAmount = itemStack.getAmount() - amount;
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

        if (!extraItems.isEmpty())
            RoseStacker.getInstance().getManager(StackManager.class).preStackItems(extraItems, player.getLocation());
    }

    public static void damageTool(ItemStack itemStack) {
        Damageable damageable = (Damageable) itemStack.getItemMeta();
        if (damageable == null)
            return;

        damageable.setDamage(damageable.getDamage() + 1);
        itemStack.setItemMeta((ItemMeta) damageable);
    }

    /**
     * Gets a custom player head from a base64 encoded texture
     *
     * @param texture The texture to apply to the player head
     * @return A player head with the custom texture applied
     */
    public static ItemStack getCustomSkull(String texture) {
        if (skullCache.containsKey(texture))
            return skullCache.get(texture).clone();

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        if (texture == null || texture.isEmpty())
            return skull;

        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta == null)
            return skull;

        if (NMSUtil.getVersionNumber() >= 18) {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();

            String decodedTextureJson = new String(Base64.getDecoder().decode(texture));
            String decodedTextureUrl = decodedTextureJson.substring(28, decodedTextureJson.length() - 4);

            try {
                textures.setSkin(new URL(decodedTextureUrl));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            skullMeta.setOwnerProfile(profile);
        } else {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", texture));

            try {
                if (field_SkullMeta_profile == null) {
                    field_SkullMeta_profile = skullMeta.getClass().getDeclaredField("profile");
                    field_SkullMeta_profile.setAccessible(true);
                }

                field_SkullMeta_profile.set(skullMeta, profile);
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace();
            }
        }

        skull.setItemMeta(skullMeta);

        skullCache.put(texture, skull);
        return skull.clone();
    }

    public static ItemStack getBlockAsStackedItemStack(Material material, int amount) {
        ItemStack itemStack = new ItemStack(material);
        if (amount == 1)
            return itemStack;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;

        BlockStackSettings stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getBlockStackSettings(material);
        StringPlaceholders placeholders = StringPlaceholders.builder("amount", StackerUtils.formatNumber(amount)).add("name", stackSettings.getDisplayName()).build();
        String displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("block-stack-display", placeholders);

        itemMeta.setDisplayName(displayString);

        // Set the lore, if defined
        List<String> lore = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessages("stack-item-lore-block", placeholders);
        if (!lore.isEmpty())
            itemMeta.setLore(lore);

        itemStack.setItemMeta(itemMeta);

        // Set stack size
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        itemStack = nmsHandler.setItemStackNBT(itemStack, "StackSize", amount);

        return itemStack;
    }

    public static boolean isSpawnEgg(Material material) {
        return material.name().endsWith("_SPAWN_EGG");
    }

    @ApiStatus.Obsolete
    public static ItemStack getSpawnerAsStackedItemStack(EntityType entityType, int amount) {
        return getSpawnerAsStackedItemStack(SpawnerType.of(entityType), amount);
    }

    public static ItemStack getSpawnerAsStackedItemStack(SpawnerType spawnerType, int amount) {
        ItemStack itemStack = new ItemStack(Material.SPAWNER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;

        SpawnerStackSettings stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getSpawnerStackSettings(spawnerType);
        StringPlaceholders placeholders = StringPlaceholders.builder("amount", StackerUtils.formatNumber(amount)).add("name", stackSettings.getDisplayName()).build();
        String displayString;
        if (amount == 1) {
            displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display-single", placeholders);
        } else {
            displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display", placeholders);
        }

        itemMeta.setDisplayName(displayString);

        if (SettingKey.SPAWNER_HIDE_VANILLA_ITEM_LORE.get())
            itemMeta.addItemFlags(VersionUtils.HIDE_ADDITIONAL_TOOLTIP);

        // Set the lore
        LocaleManager localeManager = RoseStacker.getInstance().getManager(LocaleManager.class);
        List<String> globalLore, typeLore;
        if (amount == 1) {
            globalLore = localeManager.getLocaleMessages("stack-item-lore-spawner", placeholders);
            typeLore = stackSettings.getItemLoreSingular(placeholders);
        } else {
            globalLore = localeManager.getLocaleMessages("stack-item-lore-spawner-plural", placeholders);
            typeLore = stackSettings.getItemLorePlural(placeholders);
        }

        List<String> lore = new ArrayList<>(globalLore.size() + typeLore.size());
        if (SettingKey.MISC_SPAWNER_LORE_DISPLAY_GLOBAL_LORE_FIRST.get()) {
            lore.addAll(globalLore);
            lore.addAll(typeLore);
        } else {
            lore.addAll(typeLore);
            lore.addAll(globalLore);
        }

        if (!lore.isEmpty())
            itemMeta.setLore(lore);

        if (!spawnerType.isEmpty()) {
            // Set the spawned type directly onto the spawner item for hopeful compatibility with other plugins
            BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
            CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
            creatureSpawner.setSpawnedType(spawnerType.getOrThrow());
            blockStateMeta.setBlockState(creatureSpawner);
        }

        itemStack.setItemMeta(itemMeta);

        // Set stack size
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        itemStack = nmsHandler.setItemStackNBT(itemStack, "StackSize", amount);

        return itemStack;
    }

    public static ItemStack getEntityAsStackedItemStack(EntityType entityType, int amount) {
        EntityStackSettings stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getEntityStackSettings(entityType);
        Material spawnEggMaterial = stackSettings.getEntityTypeData().spawnEggMaterial();
        if (spawnEggMaterial == null)
            return null;

        ItemStack itemStack = new ItemStack(spawnEggMaterial);
        if (amount == 1)
            return itemStack;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;

        StringPlaceholders placeholders = StringPlaceholders.builder("amount", StackerUtils.formatNumber(amount)).add("name", stackSettings.getDisplayName()).build();
        String displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("entity-stack-display-spawn-egg", placeholders);

        itemMeta.setDisplayName(displayString);

        // Set the lore, if defined
        List<String> lore = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessages("stack-item-lore-entity", placeholders);
        if (!lore.isEmpty())
            itemMeta.setLore(lore);

        itemStack.setItemMeta(itemMeta);

        // Set stack size
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        itemStack = nmsHandler.setItemStackNBT(itemStack, "StackSize", amount);

        return itemStack;
    }

    public static int getStackedItemStackAmount(ItemStack itemStack) {
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        int stackSize = nmsHandler.getItemStackNBTInt(itemStack, "StackSize");
        return Math.max(stackSize, 1);
    }

    public static boolean hasStoredStackSize(ItemStack itemStack) {
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        return nmsHandler.getItemStackNBTInt(itemStack, "StackSize") > 0;
    }

    public static EntityType getStackedItemEntityType(ItemStack itemStack) {
        if (itemStack.getType() != Material.SPAWNER)
            return null;

        // First, check the spawner block state entity type
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof BlockStateMeta blockStateMeta && blockStateMeta.hasBlockState()) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
            return creatureSpawner.getSpawnedType();
        }

        // Try formats from other plugins/servers

        // Purpur servers
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        String entityTypeName = nmsHandler.getItemStackNBTString(itemStack, "Purpur.mob_type");

        // SilkSpawners
        if (entityTypeName.isEmpty())
            entityTypeName = nmsHandler.getItemStackNBTStringFromCompound(itemStack, "SilkSpawners", "entity");

        // EpicSpawners Pre-v7
        if (entityTypeName.isEmpty())
            entityTypeName = nmsHandler.getItemStackNBTString(itemStack, "type").toUpperCase().replace(' ', '_');

        // EpicSpawners Post-v7
        if (entityTypeName.isEmpty())
            entityTypeName = nmsHandler.getItemStackNBTString(itemStack, "data");

        // MineableSpawners
        if (entityTypeName.isEmpty())
            entityTypeName = nmsHandler.getItemStackNBTString(itemStack, "ms_mob");

        if (!entityTypeName.isEmpty()) {
            try {
                NamespacedKey entityTypeKey = NamespacedKey.fromString(entityTypeName);
                for (EntityType entityType : EntityType.values())
                    if (entityType != EntityType.UNKNOWN && entityType.getKey().equals(entityTypeKey) || entityTypeName.equalsIgnoreCase(entityType.name()))
                        return EntityType.valueOf(entityTypeName);
            } catch (Exception ignored) { }
        }

        // Check if we're allowing name and lore checks
        if (!SettingKey.SPAWNER_ITEM_CHECK_DISPLAY_NAME.get())
            return null;

        // Use the name to determine the type, must be colored
        String name = ChatColor.stripColor(itemMeta.getDisplayName());
        if (!name.equals(itemMeta.getDisplayName())) {
            EntityType entityType = getEntityTypeFromName(name);
            if (entityType != null)
                return entityType;
        }

        // Try the lore
        List<String> lore = itemMeta.getLore();
        if (lore != null) {
            for (String line : lore) {
                name = ChatColor.stripColor(line);
                if (!name.equals(line)) {
                    EntityType entityType = getEntityTypeFromName(name);
                    if (entityType != null)
                        return entityType;
                }
            }
        }

        return null;
    }

    private static EntityType getEntityTypeFromName(String name) {
        try {
            name = name.toUpperCase();
            int spawnerIndex = name.indexOf("SPAWNER");
            if (spawnerIndex != -1)
                name = name.substring(0, spawnerIndex).trim();
            return EntityType.valueOf(name.replaceAll(" ", "_"));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static SpawnerType getStackedItemSpawnerType(ItemStack itemStack) {
        EntityType entityType = getStackedItemEntityType(itemStack);
        return entityType == null ? SpawnerType.empty() : SpawnerType.of(entityType);
    }

    public static ItemStack getStackingTool() {
        if (cachedStackingTool != null)
            return cachedStackingTool;

        Material material = Material.matchMaterial(SettingKey.STACK_TOOL_MATERIAL.get());
        if (material == null) {
            material = Material.STICK;
            RoseStacker.getInstance().getLogger().warning("Invalid material for stacking tool in config.yml!");
        }

        String name = HexUtils.colorify(SettingKey.STACK_TOOL_NAME.get());
        List<String> lore = SettingKey.STACK_TOOL_LORE.get().stream().map(HexUtils::colorify).toList();

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return item;

        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, VersionUtils.HIDE_ADDITIONAL_TOOLTIP);
        meta.addEnchant(VersionUtils.INFINITY, 1, true);

        item.setItemMeta(meta);
        cachedStackingTool = item;
        return item;
    }

    public static boolean isStackingTool(ItemStack item) {
        return getStackingTool().isSimilar(item);
    }

    public static List<ItemStack> getMultipliedItemStacks(Collection<ItemStack> itemStacks, double multiplier, boolean reduce) {
        // Reduce and multiply counts
        Map<ItemStack, Integer> counts = reduceItemsByCounts(itemStacks).entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (int) (entry.getValue() * multiplier)));

        // Split items back into normal stacks by their max stack size
        List<ItemStack> items = new ArrayList<>();
        for (Map.Entry<ItemStack, Integer> entry : counts.entrySet()) {
            ItemStack itemStack = entry.getKey();
            int amount = entry.getValue();
            if (reduce) {
                ItemStack clone = itemStack.clone();
                clone.setAmount(amount);
                items.add(clone);
            } else {
                items.addAll(splitItemStack(itemStack, amount));
            }
        }

        return items;
    }

    /**
     * Creates copies of an ItemStack totalling to the specified amount.
     * Returns a Collection of ItemStacks with each stack having a max stack size of the ItemStack Material.
     *
     * @param itemStack the ItemStack to clone
     * @param amount the amount of items
     * @return the cloned items totalling to the specified amount
     */
    public static Collection<? extends ItemStack> splitItemStack(ItemStack itemStack, int amount) {
        List<ItemStack> items = new ArrayList<>();
        int maxStackSize = itemStack.getMaxStackSize();
        while (amount > 0) {
            ItemStack clone = itemStack.clone();
            if (amount > maxStackSize) {
                clone.setAmount(maxStackSize);
                items.add(clone);
                amount -= maxStackSize;
            } else {
                clone.setAmount(amount);
                items.add(clone);
                amount = 0;
            }
        }
        return items;
    }

    /**
     * Reduces a collection of items of various counts into a single count
     *
     * @param items the items
     * @return the reduced items reduced by counts
     */
    public static Map<ItemStack, Integer> reduceItemsByCounts(Collection<ItemStack> items) {
        Map<ItemStack, Integer> itemStackAmounts = new HashMap<>();
        for (ItemStack itemStack : items) {
            if (itemStack == null || itemStack.getType() == Material.AIR)
                continue;

            Optional<Map.Entry<ItemStack, Integer>> similar = itemStackAmounts.entrySet().stream().filter(x -> x.getKey().isSimilar(itemStack)).findFirst();
            if (similar.isPresent()) {
                similar.get().setValue(similar.get().getValue() + itemStack.getAmount());
            } else {
                ItemStack clone = itemStack.clone();
                clone.setAmount(1);
                itemStackAmounts.put(clone, itemStack.getAmount());
            }
        }
        return itemStackAmounts;
    }

    public static void clearCache() {
        skullCache.clear();
        cachedStackingTool = null;
    }

}
