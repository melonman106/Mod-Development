package com.example.targetdummy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class TargetDummyMod implements ModInitializer {
    public static final String DUMMY_NAME = "Target Dummy";
    public static final float NUMBER_VIEW_RANGE = 8.0F;

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(DamageNumberTracker::tick);
    }

    public static boolean isTargetDummy(net.minecraft.world.entity.decoration.ArmorStand stand) {
        return stand.getCustomName() != null
                && DUMMY_NAME.equals(stand.getCustomName().getString());
    }
}
