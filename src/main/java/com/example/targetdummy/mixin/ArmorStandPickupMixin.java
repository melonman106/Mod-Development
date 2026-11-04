package com.example.targetdummy.mixin;

import com.example.targetdummy.TargetDummyMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemScatterer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class ArmorStandPickupMixin {

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    private void targetdummy$pickup(Player player, Vec3 hitPos, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ArmorStand stand = (ArmorStand) (Object) this;

        if (!TargetDummyMod.isTargetDummy(stand)) return;
        if (player.level().isClientSide()) return;
        if (!player.isCrouching()) return;

        // Drop all armour / equipment first
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equipped = stand.getItemBySlot(slot);
            if (!equipped.isEmpty()) {
                ItemScatterer.spawn(player.level(), stand.getX(), stand.getY(), stand.getZ(), equipped);
                stand.setItemSlot(slot, ItemStack.EMPTY);
            }
        }

        // Give back the target dummy item with the same name
        ItemStack dummyItem = new ItemStack(Items.ARMOR_STAND);
        dummyItem.setHoverName(Component.literal(TargetDummyMod.DUMMY_NAME));

        if (!player.getInventory().add(dummyItem)) {
            player.spawnAtLocation(dummyItem);
        }

        stand.discard();
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
