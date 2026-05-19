package dev.onyxium.hynnotate.example;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.onyxium.hynnotate.annotations.CodecField;
import dev.onyxium.hynnotate.annotations.CodecWith;
import dev.onyxium.hynnotate.annotations.IncludeCodec;

import javax.annotation.Nullable;
import java.util.UUID;

@IncludeCodec
public class RewardComponent implements Component<EntityStore> {

    @CodecField // generates Name
    private String name;

    @CodecField("Amount")
    private Integer amount;

    @CodecField("UUID")
    @CodecWith("BuilderCodec.UUID_STRING")
    private UUID uuid;

    public RewardComponent() {}

    public RewardComponent(String name, int amount) {
        this.name = name;
        this.amount = amount;
        this.uuid = UUID.randomUUID();
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

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new RewardComponent(name, amount);
    }
}
