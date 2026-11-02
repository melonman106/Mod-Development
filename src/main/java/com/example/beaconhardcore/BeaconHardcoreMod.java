package com.example.beaconhardcore;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

public class BeaconHardcoreMod implements ModInitializer {
    public static final String DOOMED_TAG = "beacon_doomed";
    public static final String BEACON_OWNER_NBT = "BeaconOwner";
    public static final String BEACON_USE_PERMISSION = "beaconhardcore.beacon";
    public static final String DOOMED_PERMISSION = "beaconhardcore.doomed";

    @Override
    public void onInitialize() {

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("beacon").executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayer();

                if (!hasLuckPermsPermission(player, BEACON_USE_PERMISSION)) {
                    player.sendMessage(
                            Text.literal("You do not have permission to use /beacon.")
                                    .formatted(Formatting.RED),
                            false
                    );
                    return 0;
                }

                ItemStack beacon = new ItemStack(Items.BEACON);
                beacon.getOrCreateNbt().putUuid(BEACON_OWNER_NBT, player.getUuid());

                if (!player.getInventory().insertStack(beacon)) {
                    player.dropItem(beacon, false);
                }

                player.sendMessage(
                        Text.literal("You received your special beacon.")
                                .formatted(Formatting.GREEN),
                        false
                );
                return 1;
            }));
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld serverWorld)) return;
            if (!state.isOf(Blocks.BEACON)) return;

            UUID ownerUuid = BeaconOwnershipManager.getOwner(serverWorld, pos);
            if (ownerUuid == null) return;

            BeaconOwnershipManager.removeOwner(serverWorld, pos);

            MinecraftServer server = serverWorld.getServer();
            ServerPlayerEntity ownerPlayer = server.getPlayerManager().getPlayer(ownerUuid);

            String ownerName = ownerPlayer != null ? ownerPlayer.getName().getString() : ownerUuid.toString();

            markDoomed(ownerUuid);

            if (ownerPlayer != null) {
                showDoomedTitle(server, ownerPlayer, ownerName);
                ownerPlayer.sendMessage(
                        Text.literal("Your beacon was broken. You will become a spectator when you next die.")
                                .formatted(Formatting.RED),
                        false
                );
            }

            server.getPlayerManager().broadcast(
                    Text.literal(ownerName + "'s beacon was broken! " + ownerName + " is now doomed!")
                            .formatted(Formatting.RED),
                    false
            );
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (isDoomed(newPlayer.getUuid())) {
                newPlayer.changeGameMode(GameMode.SPECTATOR);
                newPlayer.sendMessage(
                        Text.literal("You have died and are now in Spectator mode.")
                                .formatted(Formatting.RED),
                        false
                );
            }
        });
    }

    private boolean hasLuckPermsPermission(ServerPlayerEntity player, String permission) {
        if (!FabricLoader.getInstance().isModLoaded("luckperms")) {
            return false;
        }

        User user = getLuckPermsUser(player.getUuid());
        if (user == null) return false;

        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    private void markDoomed(UUID playerUuid) {
        if (!FabricLoader.getInstance().isModLoaded("luckperms")) {
            return;
        }

        User user = getLuckPermsUser(playerUuid);
        if (user == null) return;

        user.data().add(Node.builder(DOOMED_PERMISSION).value(true).build());
        LuckPermsProvider.get().getUserManager().saveUser(user);
    }

    private boolean isDoomed(UUID playerUuid) {
        if (!FabricLoader.getInstance().isModLoaded("luckperms")) {
            return false;
        }

        User user = getLuckPermsUser(playerUuid);
        if (user == null) return false;

        return user.getCachedData().getPermissionData().checkPermission(DOOMED_PERMISSION).asBoolean();
    }

    private User getLuckPermsUser(UUID playerUuid) {
        try {
            User user = LuckPermsProvider.get().getUserManager().getUser(playerUuid);
            if (user != null) return user;

            return LuckPermsProvider.get().getUserManager().loadUser(playerUuid).join();
        } catch (Exception e) {
            return null;
        }
    }

    private void showDoomedTitle(MinecraftServer server, ServerPlayerEntity player, String ownerName) {
        String titleJson = "{\"text\":\"" + escapeJson(ownerName + " has be doomed!") + "\",\"color\":\"red\",\"bold\":true}";
        String subtitleJson = "{\"text\":\"Your beacon was broken.\",\"color\":\"red\"}";

        server.getCommandManager().executeWithPrefix(
                server.getCommandSource().withLevel(4),
                "title " + player.getName().getString() + " times 10 70 20"
        );

        server.getCommandManager().executeWithPrefix(
                server.getCommandSource().withLevel(4),
                "title " + player.getName().getString() + " title " + titleJson
        );

        server.getCommandManager().executeWithPrefix(
                server.getCommandSource().withLevel(4),
                "title " + player.getName().getString() + " subtitle " + subtitleJson
        );
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
