package dev.onyxium.hynnotate.example;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.onyxium.hynnotate.annotations.CodecField;
import dev.onyxium.hynnotate.annotations.IncludeCodec;

import javax.annotation.Nullable;

@IncludeCodec
public class RewardComponent implements Component<EntityStore> {

    @CodecField // generates Name
    private String name;

    @CodecField("Amount")
    private Integer amount;

    public RewardComponent() {}

    public RewardComponent(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new RewardComponent(name, amount);
    }

//    Generated inside of Reward_Codec.java
//    public static final BuilderCodec<Reward> CODEC = BuilderCodec.builder(Reward.class, Reward::new)
//            .append(new KeyedCodec<>("Name", BuilderCodec.STRING), Reward::setName, Reward::getName).add()
//            .append(new KeyedCodec<>("Amount", BuilderCodec.INTEGER), Reward::setAmount, Reward::getAmount).add()
//            .build();
}
