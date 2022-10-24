package dev.rosewood.rosestacker.command.type;

public class StackedEntityAmount {

    private final int amount;

    public StackedEntityAmount(int amount) {
        this.amount = amount;
    }

    public int get() {
        return this.amount;
    }

}
