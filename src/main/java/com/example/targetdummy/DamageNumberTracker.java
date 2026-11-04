package com.example.targetdummy;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DamageNumberTracker {
    private static final Map<UUID, Integer> LIFETIMES = new ConcurrentHashMap<>();

    private DamageNumberTracker() {
    }

    public static void track(Entity entity, int ticks) {
        LIFETIMES.put(entity.getUUID(), ticks);
    }

    public static void tick(MinecraftServer server) {
        List<UUID> removeLater = new ArrayList<>();

        Iterator<Map.Entry<UUID, Integer>> iterator = LIFETIMES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int remaining = entry.getValue() - 1;

            if (remaining <= 0) {
                UUID uuid = entry.getKey();

                for (ServerLevel level : server.getAllLevels()) {
                    Entity entity = level.getEntity(uuid);
                    if (entity != null) {
                        entity.discard();
                        break;
                    }
                }

                removeLater.add(uuid);
            } else {
                entry.setValue(remaining);
            }
        }

        for (UUID uuid : removeLater) {
            LIFETIMES.remove(uuid);
        }
    }
}
