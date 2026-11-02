package com.example.beaconhardcore;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BeaconOwnershipManager {
    private static final Map<String, UUID> OWNERS = new ConcurrentHashMap<>();

    private BeaconOwnershipManager() {}

    private static String key(ServerWorld world, BlockPos pos) {
        return world.getRegistryKey().getValue() + ":" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public static void setOwner(ServerWorld world, BlockPos pos, UUID owner) {
        OWNERS.put(key(world, pos), owner);
    }

    public static UUID getOwner(ServerWorld world, BlockPos pos) {
        return OWNERS.get(key(world, pos));
    }

    public static void removeOwner(ServerWorld world, BlockPos pos) {
        OWNERS.remove(key(world, pos));
    }
}
