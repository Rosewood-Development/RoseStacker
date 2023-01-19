package dev.rosewood.rosestacker.stack;

import org.bukkit.entity.Player;

public interface Viewable {

    void openGui(Player player);

    void kickOutGuiViewers();

}
