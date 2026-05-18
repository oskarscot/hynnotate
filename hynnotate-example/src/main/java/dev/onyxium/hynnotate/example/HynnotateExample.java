package dev.onyxium.hynnotate.example;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

public class HynnotateExample extends JavaPlugin {

    public HynnotateExample(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        getLogger().atInfo().log("HynnotateExample plugin loaded.");
        getEntityStoreRegistry().registerComponent(RewardComponent.class, "RewardComponent", RewardComponent_Codec.CODEC);
    }
}
