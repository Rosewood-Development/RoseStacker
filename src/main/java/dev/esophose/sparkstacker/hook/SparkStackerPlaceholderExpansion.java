package dev.esophose.sparkstacker.hook;

import dev.esophose.sparkstacker.SparkStacker;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class SparkStackerPlaceholderExpansion extends PlaceholderExpansion {

    private SparkStacker sparkStacker;

    public SparkStackerPlaceholderExpansion(SparkStacker sparkStacker) {
        this.sparkStacker = sparkStacker;
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
        return this.sparkStacker.getDescription().getName().toLowerCase();
    }

    @Override
    public String getAuthor() {
        return this.sparkStacker.getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion() {
        return this.sparkStacker.getDescription().getVersion();
    }

}