package dev.rosewood.rosestacker.locale;

import java.util.Map;

public interface Locale {

    /**
     * @return the name for this locale file
     */
    String getLocaleName();

    /**
     * @return the name of the translator for this language
     */
    String getTranslatorName();

    /**
     * @return the default locale message strings in a key -> value pair
     */
    Map<String, Object> getDefaultLocaleValues();

}
