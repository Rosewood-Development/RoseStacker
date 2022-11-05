package dev.rosewood.rosestacker.command.type;

public class StackedBlockAmount {

    private final int amount;

    public StackedBlockAmount(int amount) {
        this.amount = amount;
    }

    public int get() {
        return this.amount;
    }

}
