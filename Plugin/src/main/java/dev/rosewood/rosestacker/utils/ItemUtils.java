package dev.rosewood.rosestacker.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public final class ItemUtils {

    private final static Map<String, ItemStack> skullCache = new HashMap<>();
    private static Method method_SkullMeta_setProfile;

    private static ItemStack cachedStackingTool;

    public static Material getWoolMaterial(DyeColor dyeColor) {
        if (dyeColor == null)
            return Material.WHITE_WOOL;
        return Material.matchMaterial(dyeColor.name() + "_WOOL");
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

    /**
     * Gets a custom player head from a base64 encoded texture
     *
     * @param texture The texture to apply to the player head
     * @return A player head with the custom texture applied
     */
    public static ItemStack getCustomSkull(String texture) {
        if (skullCache.containsKey(texture))
            return skullCache.get(texture);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        if (texture == null || texture.isEmpty())
            return skull;

        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta == null)
            return skull;

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", texture));

        try {
            if (method_SkullMeta_setProfile == null) {
                method_SkullMeta_setProfile = skullMeta.getClass().getDeclaredMethod("setProfile");
                method_SkullMeta_setProfile.setAccessible(true);
            }

            method_SkullMeta_setProfile.invoke(skullMeta, profile);
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }

        skull.setItemMeta(skullMeta);
        return skull;
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

        // Set the spawned type directly onto the spawner item for hopeful compatibility with other plugins
        BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
        CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
        creatureSpawner.setSpawnedType(entityType);
        blockStateMeta.setBlockState(creatureSpawner);

        itemStack.setItemMeta(itemMeta);

        // Set stack size and spawned entity type
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        itemStack = nmsHandler.setItemStackNBT(itemStack, "StackSize", amount);
        itemStack = nmsHandler.setItemStackNBT(itemStack, "EntityType", entityType.name());

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

    public static ItemStack getStackingTool() {
        if (cachedStackingTool != null)
            return cachedStackingTool;

        Material material = Material.matchMaterial(ConfigurationManager.Setting.STACK_TOOL_MATERIAL.getString());
        if (material == null) {
            material = Material.STICK;
            RoseStacker.getInstance().getLogger().warning("Invalid material for stacking tool in config.yml!");
        }

        String name = HexUtils.colorify(ConfigurationManager.Setting.STACK_TOOL_NAME.getString());
        List<String> lore = ConfigurationManager.Setting.STACK_TOOL_LORE.getStringList().stream().map(HexUtils::colorify).collect(Collectors.toList());

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

    public static void clearCache() {
        skullCache.clear();
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
