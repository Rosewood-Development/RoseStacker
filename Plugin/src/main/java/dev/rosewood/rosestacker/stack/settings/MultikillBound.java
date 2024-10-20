package dev.rosewood.rosestacker.stack.settings;

import dev.rosewood.rosestacker.RoseStacker;

public record MultikillBound(int value, boolean percentage) {

    public int getValue(int stackSize) {
        int killAmount;
        if (this.percentage) {
            double multiplier = this.value / 100.0;
            killAmount = (int) Math.round(stackSize * multiplier);
        } else {
            killAmount = this.value;
        }
        return Math.max(1, killAmount);
    }

    public static MultikillBound parse(String stringValue) {
        try {
            boolean percentage;
            if (stringValue.endsWith("%")) {
                stringValue = stringValue.substring(0, stringValue.length() - 1);
                percentage = true;
            } else {
                percentage = false;
            }
            int value = Integer.parseInt(stringValue);
            return new MultikillBound(value, percentage);
        } catch (NumberFormatException e) {
            RoseStacker.getInstance().getLogger().warning("Invalid multikill-amount bound: '" + stringValue + "', defaulting to 1");
            return new MultikillBound(1, false);
        }
    }

}
