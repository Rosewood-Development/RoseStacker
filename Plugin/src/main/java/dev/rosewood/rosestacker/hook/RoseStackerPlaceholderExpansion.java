package dev.rosewood.rosestacker.hook;

import dev.rosewood.rosegarden.RosePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class RoseStackerPlaceholderExpansion extends PlaceholderExpansion {

    private RosePlugin rosePlugin;

    public RoseStackerPlaceholderExpansion(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @Override
    public String onPlaceholderRequest(Player p, String placeholder) {
        if (p == null)
            return null;

        // We got nothing here yet :(

        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return this.rosePlugin.getDescription().getName().toLowerCase();
    }

    @Override
    public String getAuthor() {
        return this.rosePlugin.getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion() {
        return this.rosePlugin.getDescription().getVersion();
    }

}