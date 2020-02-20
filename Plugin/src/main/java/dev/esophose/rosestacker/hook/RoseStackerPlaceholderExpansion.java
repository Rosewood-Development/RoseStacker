package dev.esophose.rosestacker.hook;

import dev.esophose.rosestacker.RoseStacker;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class RoseStackerPlaceholderExpansion extends PlaceholderExpansion {

    private RoseStacker roseStacker;

    public RoseStackerPlaceholderExpansion(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
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
        return this.roseStacker.getDescription().getName().toLowerCase();
    }

    @Override
    public String getAuthor() {
        return this.roseStacker.getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion() {
        return this.roseStacker.getDescription().getVersion();
    }

}