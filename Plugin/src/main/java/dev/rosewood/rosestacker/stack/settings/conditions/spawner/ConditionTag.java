package dev.rosewood.rosestacker.stack.settings.conditions.spawner;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Block;

/**
 * Represents a tag for a spawner condition
 * Tags are formatted as follows: 'tag:value,value2,value3' where the number of values is determined on a per-tag basis
 * Values may not be required by the tag at all, in which case the colon is omitted
 */
public abstract class ConditionTag {

    private final String tag;
    private final boolean perSpawn;

    /**
     * @param tag The tag, including both prefix and values
     * @param perSpawn true if this condition must be met for each spawn, false if it only has to be met for the spawner
     * @throws IllegalArgumentException if the tag is not valid
     */
    public ConditionTag(String tag, boolean perSpawn) {
        if (tag == null || tag.trim().isEmpty())
            throw new IllegalArgumentException("Empty or null tag");

        this.tag = tag;
        this.perSpawn = perSpawn;

        String prefix;
        String[] values;
        int index = tag.indexOf(':');
        if (index == -1) {
            prefix = tag;
            values = new String[0];
        } else {
            String[] pieces = tag.split(":", 2);
            prefix = pieces[0];
            values = pieces[1].split(",");
            for (int i = 0; i < values.length; i++)
                values[i] = values[i].trim();
        }

        if (!this.parseValues(values))
            throw new IllegalArgumentException(String.format("Invalid tag arguments for %s", prefix));
    }

    /**
     * Checks if the spawn block meets this tag's condition
     *
     * @param stackedSpawner The spawner that will be spawning the entity
     * @param spawnBlock The block the entity will be spawned in
     * @return true if the condition is met, otherwise false
     */
    public abstract boolean check(StackedSpawner stackedSpawner, Block spawnBlock);

    /**
     * Parses the value portion of the tag
     *
     * @param values The values portion of the tag to parse
     * @return true if the tag is valid, otherwise false
     */
    public abstract boolean parseValues(String[] values);

    /**
     * Gets the values of this condition ready to be printed to the user
     *
     * @param localeManager The LocaleManager instance
     * @return the values of this condition ready to be printed to the user
     */
    protected abstract List<String> getInfoMessageValues(LocaleManager localeManager);

    /**
     * Gets the info message to be displayed in the spawner GUI
     *
     * @param localeManager The LocaleManager instance
     * @return A list of lore lines to be displayed in the spawner GUI
     */
    public List<String> getInfoMessage(LocaleManager localeManager) {
        List<String> messages = new ArrayList<>();

        String prefix = ConditionTags.getPrefix(this.getClass());
        List<String> values = this.getInfoMessageValues(localeManager);

        String info = localeManager.getLocaleMessage("spawner-condition-" + prefix + "-info");
        if (values.size() == 0) {
            messages.add(localeManager.getLocaleMessage("spawner-condition-info", StringPlaceholders.of("condition", info)));
        } else if (values.size() == 1) {
            StringPlaceholders placeholders = StringPlaceholders.builder("condition", info).add("value", values.get(0)).build();
            messages.add(localeManager.getLocaleMessage("spawner-condition-single", placeholders));
        } else {
            messages.add(localeManager.getLocaleMessage("spawner-condition-list", StringPlaceholders.of("condition", info)));
            for (String value : values) {
                StringPlaceholders placeholders = StringPlaceholders.builder("condition", info).add("message", value).build();
                messages.add(localeManager.getLocaleMessage("spawner-condition-list-item", placeholders));
            }
        }

        return messages;
    }

    /**
     * @return true if this condition must be met for each spawn, false if it only has to be met for the spawner
     */
    public boolean isRequiredPerSpawn() {
        return this.perSpawn;
    }

    @Override
    public String toString() {
        return this.tag;
    }

}
