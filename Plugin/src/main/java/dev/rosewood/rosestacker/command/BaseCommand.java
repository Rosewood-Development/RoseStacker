package dev.rosewood.rosestacker.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.HelpCommand;
import dev.rosewood.rosegarden.command.PrimaryCommand;
import dev.rosewood.rosegarden.command.ReloadCommand;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandInfo;

public class BaseCommand extends PrimaryCommand {

    public BaseCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("rs")
                .aliases("rosestacker", "stacker")
                .arguments(ArgumentsDefinition.builder()
                        .optionalSub(
                                new HelpCommand(this.rosePlugin, this),
                                new ReloadCommand(this.rosePlugin),
                                new ClearallCommand(this.rosePlugin),
                                new GiveCommand(this.rosePlugin),
                                new StackToolCommand(this.rosePlugin),
                                new StatsCommand(this.rosePlugin),
                                new TranslateCommand(this.rosePlugin)
                        ))
                .build();
    }

}
