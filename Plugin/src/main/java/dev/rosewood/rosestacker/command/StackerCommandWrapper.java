package dev.rosewood.rosestacker.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StackerCommandWrapper extends RoseCommandWrapper {

    public StackerCommandWrapper(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    public String getDefaultName() {
        return "rs";
    }

    @Override
    public List<String> getDefaultAliases() {
        return Arrays.asList("rosestacker", "stacker");
    }

    @Override
    public List<String> getCommandPackages() {
        return Collections.singletonList("dev.rosewood.rosestacker.command.command");
    }

    @Override
    public boolean includeBaseCommand() {
        return true;
    }

    @Override
    public boolean includeHelpCommand() {
        return true;
    }

    @Override
    public boolean includeReloadCommand() {
        return true;
    }

}
