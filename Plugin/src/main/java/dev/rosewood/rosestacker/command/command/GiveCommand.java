package dev.rosewood.rosestacker.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.RoseSubCommand;
import dev.rosewood.rosegarden.command.framework.annotation.Inject;
import dev.rosewood.rosegarden.command.framework.annotation.Optional;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.command.type.StackedBlockAmount;
import dev.rosewood.rosestacker.command.type.StackedBlockMaterial;
import dev.rosewood.rosestacker.command.type.StackedEntityAmount;
import dev.rosewood.rosestacker.command.type.StackedEntityType;
import dev.rosewood.rosestacker.command.type.StackedSpawnerAmount;
import dev.rosewood.rosestacker.command.type.StackedSpawnerType;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.Arrays;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCommand extends RoseCommand {

    public GiveCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent, BlockGiveCommand.class, SpawnerGiveCommand.class, EntityGiveCommand.class);
    }

    @RoseExecutable
    public void execute(CommandContext context, @Optional RoseSubCommand type) {
        this.rosePlugin.getManager(LocaleManager.class).sendMessage(context.getSender(), "command-give-usage");
    }

    @Override
    protected String getDefaultName() {
        return "give";
    }

    @Override
    public String getDescriptionKey() {
        return "command-give-description";
    }

    @Override
    public String getRequiredPermission() {
        return "rosestacker.give";
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

    public static class BlockGiveCommand extends RoseSubCommand {

        public BlockGiveCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
            super(rosePlugin, parent);
        }

        @RoseExecutable
        public void execute(@Inject CommandContext context, Player target, StackedBlockMaterial material, StackedBlockAmount stackSize, @Optional Integer amount) {
            LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
            BlockStackSettings stackSettings = this.rosePlugin.getManager(StackSettingManager.class).getBlockStackSettings(material.get());
            if (stackSettings == null || !stackSettings.isStackingEnabled()) {
                localeManager.sendMessage(context.getSender(), "command-give-unstackable");
                return;
            }

            if (stackSize.get() > stackSettings.getMaxStackSize()) {
                localeManager.sendMessage(context.getSender(), "command-give-too-large");
                return;
            }

            if (amount == null || amount < 1)
                amount = 1;

            ItemStack item = ItemUtils.getBlockAsStackedItemStack(material.get(), stackSize.get());
            giveDuplicates(target, item, amount);

            String displayString = localeManager.getLocaleMessage("block-stack-display", StringPlaceholders.builder("amount", StackerUtils.formatNumber(stackSize.get()))
                    .addPlaceholder("name", stackSettings.getDisplayName()).build());

            StringPlaceholders placeholders = StringPlaceholders.builder("player", target.getName())
                    .addPlaceholder("amount", StackerUtils.formatNumber(amount))
                    .addPlaceholder("display", displayString)
                    .build();

            if (amount == 1) {
                localeManager.sendMessage(context.getSender(), "command-give-given", placeholders);
            } else {
                localeManager.sendMessage(context.getSender(), "command-give-given-multiple", placeholders);
            }
        }

        @Override
        protected String getDefaultName() {
            return "block";
        }

    }

    public static class SpawnerGiveCommand extends RoseSubCommand {

        public SpawnerGiveCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
            super(rosePlugin, parent);
        }

        @RoseExecutable
        public void execute(@Inject CommandContext context, Player target, StackedSpawnerType spawnerType, StackedSpawnerAmount stackSize, @Optional Integer amount) {
            LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
            SpawnerStackSettings stackSettings = this.rosePlugin.getManager(StackSettingManager.class).getSpawnerStackSettings(spawnerType.get());
            if (stackSettings == null || !stackSettings.isStackingEnabled()) {
                localeManager.sendMessage(context.getSender(), "command-give-unstackable");
                return;
            }

            if (stackSize.get() > stackSettings.getMaxStackSize()) {
                localeManager.sendMessage(context.getSender(), "command-give-too-large");
                return;
            }

            if (amount == null || amount < 1)
                amount = 1;

            ItemStack item = ItemUtils.getSpawnerAsStackedItemStack(spawnerType.get(), stackSize.get());
            giveDuplicates(target, item, amount);

            String displayString;
            if (stackSize.get() == 1) {
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display-single", StringPlaceholders.builder("amount", StackerUtils.formatNumber(stackSize.get()))
                        .addPlaceholder("name", stackSettings.getDisplayName()).build());
            } else {
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display", StringPlaceholders.builder("amount", StackerUtils.formatNumber(stackSize.get()))
                        .addPlaceholder("name", stackSettings.getDisplayName()).build());
            }

            StringPlaceholders placeholders = StringPlaceholders.builder("player", target.getName())
                    .addPlaceholder("amount", StackerUtils.formatNumber(amount))
                    .addPlaceholder("display", displayString)
                    .build();

            if (amount == 1) {
                localeManager.sendMessage(context.getSender(), "command-give-given", placeholders);
            } else {
                localeManager.sendMessage(context.getSender(), "command-give-given-multiple", placeholders);
            }
        }

        @Override
        protected String getDefaultName() {
            return "spawner";
        }

    }

    public static class EntityGiveCommand extends RoseSubCommand {

        public EntityGiveCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
            super(rosePlugin, parent);
        }

        @RoseExecutable
        public void execute(@Inject CommandContext context, Player target, StackedEntityType entityType, StackedEntityAmount stackSize, @Optional Integer amount) {
            LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
            EntityStackSettings stackSettings = this.rosePlugin.getManager(StackSettingManager.class).getEntityStackSettings(entityType.get());
            if (stackSettings == null || !stackSettings.isStackingEnabled()) {
                localeManager.sendMessage(context.getSender(), "command-give-unstackable");
                return;
            }

            if (stackSize.get() > stackSettings.getMaxStackSize()) {
                localeManager.sendMessage(context.getSender(), "command-give-too-large");
                return;
            }

            if (amount == null || amount < 1)
                amount = 1;

            ItemStack item = ItemUtils.getEntityAsStackedItemStack(entityType.get(), stackSize.get());
            if (item == null) {
                this.rosePlugin.getManager(LocaleManager.class).sendMessage(context.getSender(), "command-give-usage");
                return;
            }

            giveDuplicates(target, item, amount);

            String displayString = localeManager.getLocaleMessage("entity-stack-display-spawn-egg", StringPlaceholders.builder("amount", StackerUtils.formatNumber(stackSize.get()))
                    .addPlaceholder("name", stackSettings.getDisplayName()).build());

            StringPlaceholders placeholders = StringPlaceholders.builder("player", target.getName())
                    .addPlaceholder("amount", StackerUtils.formatNumber(amount))
                    .addPlaceholder("display", displayString)
                    .build();

            if (amount == 1) {
                localeManager.sendMessage(context.getSender(), "command-give-given", placeholders);
            } else {
                localeManager.sendMessage(context.getSender(), "command-give-given-multiple", placeholders);
            }
        }

        @Override
        protected String getDefaultName() {
            return "entity";
        }

    }

}
