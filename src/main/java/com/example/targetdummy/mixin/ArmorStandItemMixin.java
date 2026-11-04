package com.example.targetdummy.mixin;

import com.example.targetdummy.TargetDummyMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandItem.class)
public abstract class ArmorStandItemMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void targetdummy$placeDummy(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = context.getItemInHand();

        if (!stack.hasCustomHoverName()) return;
        if (!TargetDummyMod.DUMMY_NAME.equals(stack.getHoverName().getString())) return;
        if (context.getLevel().isClientSide()) return;
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) return;

        BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());

        ArmorStand dummy = new ArmorStand(EntityType.ARMOR_STAND, serverLevel);
        dummy.moveTo(
                placePos.getX() + 0.5D,
                placePos.getY(),
                placePos.getZ() + 0.5D,
                0.0F,
                0.0F
        );
        dummy.setCustomName(Component.literal(TargetDummyMod.DUMMY_NAME));
        dummy.setCustomNameVisible(true);
        dummy.setInvulnerable(true);
        dummy.setNoGravity(false);
        dummy.setPersistenceRequired();

        serverLevel.addFreshEntity(dummy);

        if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
            stack.shrink(1);
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
