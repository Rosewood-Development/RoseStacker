package dev.rosewood.rosestacker.hook.skyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SuperiorSkyblockProvider implements SkyblockProvider {

    @Override
    public boolean canBreakAndPlace(Player player, Location location) {
        Island island = SuperiorSkyblockAPI.getIslandAt(location);
        if (island == null)
            return true;

        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
        return island.hasPermission(superiorPlayer, IslandPrivilege.getByName("BREAK"))
                && island.hasPermission(superiorPlayer, IslandPrivilege.getByName("BUILD"));
    }

}
