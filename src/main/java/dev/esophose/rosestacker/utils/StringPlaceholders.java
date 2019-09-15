package dev.esophose.rosestacker.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringPlaceholders {

    private Map<String, String> placeholders;

    public StringPlaceholders() {
        this.placeholders = new HashMap<>();
    }

    public void addPlaceholder(String placeholder, String value) {
        this.placeholders.put(placeholder, value);
    }

    public String apply(String string) {
        for (String key : this.placeholders.keySet())
            string = string.replaceAll(Pattern.quote('%' + key + '%'), Matcher.quoteReplacement(this.placeholders.get(key)));
        return string;
    }

    public Map<String, String> getPlaceholders() {
        return Collections.unmodifiableMap(this.placeholders);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String placeholder, String value) {
        return new Builder(placeholder, value);
    }

    public static class Builder {

        private StringPlaceholders stringPlaceholders;

        private Builder() {
            this.stringPlaceholders = new StringPlaceholders();
        }

        private Builder(String placeholder, String value) {
            this();
            this.stringPlaceholders.addPlaceholder(placeholder, value);
        }

        public Builder addPlaceholder(String placeholder, String value) {
            this.stringPlaceholders.addPlaceholder(placeholder, value);
            return this;
        }

        public String apply(String string) {
            return this.stringPlaceholders.apply(string);
        }

        public StringPlaceholders build() {
            return this.stringPlaceholders;
        }

    }

}
