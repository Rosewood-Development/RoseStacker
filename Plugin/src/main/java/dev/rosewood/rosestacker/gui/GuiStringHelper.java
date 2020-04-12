package dev.rosewood.rosestacker.gui;

import java.util.Collections;
import java.util.List;

public class GuiStringHelper {

    private List<String> message;

    public GuiStringHelper(List<String> message) {
        this.message = message;
    }

    public String getName() {
        return this.message.get(0);
    }

    public List<String> getLore() {
        if (this.message.size() == 1)
            return Collections.emptyList();
        return this.message.subList(1, this.message.size());
    }

}
