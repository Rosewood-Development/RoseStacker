package dev.rosewood.rosestacker.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentParser;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentHandler;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentInfo;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.LocaleManager;
import java.util.List;

public class TranslationLocaleArgumentHandler extends RoseCommandArgumentHandler<TranslationLocaleArgumentHandler.TranslationLocale> {

    public TranslationLocaleArgumentHandler(RosePlugin rosePlugin) {
        super(rosePlugin, TranslationLocale.class);
    }

    @Override
    protected TranslationLocale handleInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) throws HandledArgumentException {
        String input = argumentParser.next();
        return this.rosePlugin.getManager(LocaleManager.class).getPossibleTranslationLocales().stream()
                .filter(x -> x.equalsIgnoreCase(input))
                .map(TranslationLocale::new)
                .findFirst()
                .orElseThrow(() -> new HandledArgumentException("argument-handler-translationlocale", StringPlaceholders.single("input", input)));
    }

    @Override
    protected List<String> suggestInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        return this.rosePlugin.getManager(LocaleManager.class).getPossibleTranslationLocales();
    }

    public record TranslationLocale(String name) { }

}
