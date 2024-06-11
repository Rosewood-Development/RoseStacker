package dev.rosewood.rosestacker.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.command.argument.StackerArgumentHandlers;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.spawner.SpawnerType;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCommand extends BaseRoseCommand {

    public GiveCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("give")
                .descriptionKey("command-give-description")
                .permission("rosestacker.give")
                .arguments(ArgumentsDefinition.builder()
                        .requiredSub("type",
                                new BlockGiveCommand(this.rosePlugin),
                                new EntityGiveCommand(this.rosePlugin),
                                new SpawnerGiveCommand(this.rosePlugin)
                        ))
                .build();
    }

    /**
     * Gives a Player duplicates of a single item.
     * Prioritizes giving to the Player inventory and will drop extras on the ground.
     *
     * @param player The Player to give duplicates to
     * @param item The ItemStack to give
     * @param amount The amount of the ItemStack to give
     */
    private static void giveDuplicates(Player player, ItemStack item, int amount) {
        ItemStack[] items = new ItemStack[amount];
        Arrays.fill(items, item);
        ItemUtils.dropItemsToPlayer(player, Arrays.asList(items));
    }

    public static class BlockGiveCommand extends BaseRoseCommand {

        public BlockGiveCommand(RosePlugin rosePlugin) {
            super(rosePlugin);
        }

        @RoseExecutable
        public void execute(CommandContext context, Player target, Material material, Integer stackSize, Integer amount) {
            LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
            BlockStackSettings stackSettings = this.rosePlugin.getManager(StackSettingManager.class).getBlockStackSettings(material);
            if (stackSettings == null || !stackSettings.isStackingEnabled()) {
                localeManager.sendCommandMessage(context.getSender(), "command-give-unstackable");
                return;
            }

            if (stackSize > stackSettings.getMaxStackSize()) {
                localeManager.sendCommandMessage(context.getSender(), "command-give-too-large");
                return;
            }

            if (amount == null || amount < 1)
                amount = 1;

            ItemStack item = ItemUtils.getBlockAsStackedItemStack(material, stackSize);
            giveDuplicates(target, item, amount);

            String displayString = localeManager.getLocaleMessage("block-stack-display", StringPlaceholders.builder("amount", StackerUtils.formatNumber(stackSize))
                    .add("name", stackSettings.getDisplayName()).build());

            StringPlaceholders placeholders = StringPlaceholders.builder("player", target.getName())
                    .add("amount", StackerUtils.formatNumber(amount))
                    .add("display", displayString)
                    .build();

            if (amount == 1) {
                localeManager.sendCommandMessage(context.getSender(), "command-give-given", placeholders);
            } else {
                localeManager.sendCommandMessage(context.getSender(), "command-give-given-multiple", placeholders);
            }
        }

        @Override
        protected CommandInfo createCommandInfo() {
            return CommandInfo.builder("block")
                    .arguments(ArgumentsDefinition.builder()
                            .required("player", ArgumentHandlers.PLAYER)
                            .required("material", StackerArgumentHandlers.STACKED_BLOCK_TYPE)
                            .required("stackSize", StackerArgumentHandlers.STACKED_BLOCK_AMOUNT)
                            .optional("amount", ArgumentHandlers.INTEGER)
                            .build())
                    .build();
        }

    }

    public static class SpawnerGiveCommand extends BaseRoseCommand {

        public SpawnerGiveCommand(RosePlugin rosePlugin) {
            super(rosePlugin);
        }

        @RoseExecutable
        public void execute(CommandContext context, Player target, SpawnerType spawnerType, Integer stackSize, Integer amount) {
            LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
            SpawnerStackSettings stackSettings = this.rosePlugin.getManager(StackSettingManager.class).getSpawnerStackSettings(spawnerType);
            if (stackSettings == null || !stackSettings.isStackingEnabled()) {
                localeManager.sendCommandMessage(context.getSender(), "command-give-unstackable");
                return;
            }

            if (stackSize > stackSettings.getMaxStackSize()) {
                localeManager.sendCommandMessage(context.getSender(), "command-give-too-large");
                return;
            }

            if (amount == null || amount < 1)
                amount = 1;

            ItemStack item = ItemUtils.getSpawnerAsStackedItemStack(spawnerType, stackSize);
            giveDuplicates(target, item, amount);

            String displayString;
            if (stackSize == 1) {
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display-single", StringPlaceholders.builder("amount", StackerUtils.formatNumber(stackSize))
                        .add("name", stackSettings.getDisplayName()).build());
            } else {
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display", StringPlaceholders.builder("amount", StackerUtils.formatNumber(stackSize))
                        .add("name", stackSettings.getDisplayName()).build());
            }

            StringPlaceholders placeholders = StringPlaceholders.builder("player", target.getName())
                    .add("amount", StackerUtils.formatNumber(amount))
                    .add("display", displayString)
                    .build();

            if (amount == 1) {
                localeManager.sendCommandMessage(context.getSender(), "command-give-given", placeholders);
            } else {
                localeManager.sendCommandMessage(context.getSender(), "command-give-given-multiple", placeholders);
            }
        }

        @Override
        protected CommandInfo createCommandInfo() {
            return CommandInfo.builder("spawner")
                    .arguments(ArgumentsDefinition.builder()
                            .required("player", ArgumentHandlers.PLAYER)
                            .required("spawnerType", StackerArgumentHandlers.STACKED_SPAWNER_TYPE)
                            .required("stackSize", StackerArgumentHandlers.STACKED_SPAWNER_AMOUNT)
                            .optional("amount", ArgumentHandlers.INTEGER)
                            .build())
                    .build();
        }

    }

    public static class EntityGiveCommand extends BaseRoseCommand {

        public EntityGiveCommand(RosePlugin rosePlugin) {
            super(rosePlugin);
        }

        @RoseExecutable
        public void execute(CommandContext context, Player target, EntityType entityType, Integer stackSize, Integer amount) {
            LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
            EntityStackSettings stackSettings = this.rosePlugin.getManager(StackSettingManager.class).getEntityStackSettings(entityType);
            if (stackSettings == null || !stackSettings.isStackingEnabled()) {
                localeManager.sendCommandMessage(context.getSender(), "command-give-unstackable");
                return;
            }

            if (stackSize > stackSettings.getMaxStackSize()) {
                localeManager.sendCommandMessage(context.getSender(), "command-give-too-large");
                return;
            }

            if (amount == null || amount < 1)
                amount = 1;

            ItemStack item = ItemUtils.getEntityAsStackedItemStack(entityType, stackSize);
            if (item == null) {
                localeManager.sendCommandMessage(context.getSender(), "command-give-usage");
                return;
            }

            giveDuplicates(target, item, amount);

            String displayString = localeManager.getLocaleMessage("entity-stack-display-spawn-egg", StringPlaceholders.builder("amount", StackerUtils.formatNumber(stackSize))
                    .add("name", stackSettings.getDisplayName()).build());

            StringPlaceholders placeholders = StringPlaceholders.builder("player", target.getName())
                    .add("amount", StackerUtils.formatNumber(amount))
                    .add("display", displayString)
                    .build();

            if (amount == 1) {
                localeManager.sendCommandMessage(context.getSender(), "command-give-given", placeholders);
            } else {
                localeManager.sendCommandMessage(context.getSender(), "command-give-given-multiple", placeholders);
            }
        }

        @Override
        protected CommandInfo createCommandInfo() {
            return CommandInfo.builder("entity")
                    .arguments(ArgumentsDefinition.builder()
                            .required("player", ArgumentHandlers.PLAYER)
                            .required("material", StackerArgumentHandlers.STACKED_ENTITY_TYPE)
                            .required("entityType", StackerArgumentHandlers.STACKED_ENTITY_AMOUNT)
                            .optional("amount", ArgumentHandlers.INTEGER)
                            .build())
                    .build();
        }

    }

}
