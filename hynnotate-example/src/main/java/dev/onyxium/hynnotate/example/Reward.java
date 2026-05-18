package dev.onyxium.hynnotate.example;

import dev.onyxium.hynnotate.annotations.CodecField;
import dev.onyxium.hynnotate.annotations.IncludeCodec;

@IncludeCodec
public class Reward {

    @CodecField // generates Name
    private String name;

    @CodecField("Amount")
    private int amount;

    public Reward() {}

    public Reward(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }
}
