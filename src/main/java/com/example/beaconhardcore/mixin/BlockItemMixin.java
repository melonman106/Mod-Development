package com.example.beaconhardcore.mixin;

import com.example.beaconhardcore.BeaconHardcoreMod;
import com.example.beaconhardcore.BeaconOwnershipManager;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(method = "place", at = @At("RETURN"))
    private void beaconhardcore$storeOwner(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (!cir.getReturnValue().isAccepted()) return;
        if (context.getWorld().isClient()) return;

        ItemStack stack = context.getStack();
        if (!stack.isOf(Items.BEACON)) return;
        if (stack.getNbt() == null || !stack.getNbt().containsUuid(BeaconHardcoreMod.BEACON_OWNER_NBT)) return;

        if (context.getWorld() instanceof ServerWorld serverWorld) {
            BlockPos placePos = context.getBlockPos();
            BeaconOwnershipManager.setOwner(
                    serverWorld,
                    placePos,
                    stack.getNbt().getUuid(BeaconHardcoreMod.BEACON_OWNER_NBT)
            );
        }
    }
}
