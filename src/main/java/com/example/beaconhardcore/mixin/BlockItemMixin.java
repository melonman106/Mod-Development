package com.example.beaconhardcore.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
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
        if (!stack.isOf(Items.BEACON)) return;NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
if (customData == null) return;

NbtCompound data = customData.copyNbt();
if (!data.containsUuid(BeaconHardcoreMod.BEACON_OWNER_NBT)) return;
        

        if (context.getWorld() instanceof ServerWorld serverWorld) {
            BlockPos placePos = context.getBlockPos();
            BeaconOwnershipManager.setOwner(
                    serverWorld,
                    placePos,
                    data.getUuid(BeaconHardcoreMod.BEACON_OWNER_NBT)

            );
        }
    }
}
