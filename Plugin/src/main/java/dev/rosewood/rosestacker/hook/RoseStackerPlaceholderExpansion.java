package dev.rosewood.rosestacker.hook;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.utils.StackerUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class RoseStackerPlaceholderExpansion extends PlaceholderExpansion {

    private final RosePlugin plugin;

    public RoseStackerPlaceholderExpansion(RosePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        if (player == null)
            return null;

        LocaleManager localeManager = this.plugin.getManager(LocaleManager.class);

        return switch (placeholder) {
            case "spawner_silktouch_chance" ->
                    localeManager.getLocaleMessage("silktouch-chance-placeholder", StringPlaceholders.single("chance", Math.round(StackerUtils.getSilkTouchChanceRaw(player))));
            default -> null;
        };
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return this.plugin.getDescription().getName().toLowerCase();
    }

    @Override
    public String getAuthor() {
        return this.plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

}
