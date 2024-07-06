package dev.rosewood.rosestacker.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.LocaleManager;
import java.util.List;

public class TranslationLocaleArgumentHandler extends ArgumentHandler<String> {

    private final RosePlugin rosePlugin;

    public TranslationLocaleArgumentHandler(RosePlugin rosePlugin) {
        super(String.class);

        this.rosePlugin = rosePlugin;
    }

    @Override
    public String handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();
        return this.rosePlugin.getManager(LocaleManager.class).getPossibleTranslationLocales().stream()
                .filter(x -> x.equalsIgnoreCase(input))
                .findFirst()
                .orElseThrow(() -> new HandledArgumentException("argument-handler-translationlocale", StringPlaceholders.of("input", input)));
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        return this.rosePlugin.getManager(LocaleManager.class).getPossibleTranslationLocales();
    }

}
