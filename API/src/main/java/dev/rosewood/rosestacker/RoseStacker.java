package dev.rosewood.rosestacker;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.AbstractCommandManager;
import dev.rosewood.rosegarden.manager.AbstractConfigurationManager;
import dev.rosewood.rosegarden.manager.AbstractDataManager;
import dev.rosewood.rosegarden.manager.AbstractLocaleManager;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.manager.ManagerLogic;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public abstract class RoseStacker extends RosePlugin {

    public RoseStacker(int spigotId, int bStatsId, Class<? extends AbstractConfigurationManager> configurationManagerClass, Class<? extends AbstractDataManager> dataManagerClass, Class<? extends AbstractLocaleManager> localeManagerClass, Class<? extends AbstractCommandManager> commandManagerClass) {
        super(spigotId, bStatsId, configurationManagerClass, dataManagerClass, localeManagerClass, commandManagerClass);
    }

    /**
     * @return the value of executing {@link RoseStackerAPI#getPluginInstance()}
     */
    @NotNull
    public static RoseStacker getInstance() {
        return RoseStackerAPI.getInstance().getPluginInstance();
    }

    /**
     * @return the value of executing {@link RoseStackerAPI#getInstance()}
     */
    @NotNull
    public static RoseStackerAPI getAPI() {
        return RoseStackerAPI.getInstance();
    }

    @ApiStatus.Internal
    public abstract <T extends ManagerLogic> T getManagerInternal(Class<T> managerClass);

}
