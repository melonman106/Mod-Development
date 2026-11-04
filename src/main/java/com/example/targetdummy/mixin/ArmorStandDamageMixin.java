package com.example.targetdummy.mixin;

import com.example.targetdummy.DamageNumberTracker;
import com.example.targetdummy.TargetDummyMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.Display;
import net.minecraft.world.entity.decoration.TextDisplay;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(ArmorStand.class)
public abstract class ArmorStandDamageMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void targetdummy$showNumber(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ArmorStand stand = (ArmorStand) (Object) this;

        if (!TargetDummyMod.isTargetDummy(stand)) return;
        if (!(stand.level() instanceof ServerLevel serverLevel)) return;

        spawnDamageNumber(serverLevel, stand.position(), amount);

        // Stop the dummy from losing health or breaking
        cir.setReturnValue(false);
    }

    private void spawnDamageNumber(ServerLevel world, Vec3 pos, float damage) {
        TextDisplay number = new TextDisplay(EntityType.TEXT_DISPLAY, world);

        number.moveTo(
                pos.x + (world.random.nextDouble() - 0.5D) * 0.2D,
                pos.y + 1.85D,
                pos.z + (world.random.nextDouble() - 0.5D) * 0.2D,
                0.0F,
                0.0F
        );

        number.setText(Component.literal("-" + formatDamage(damage)).withColor(0xFFFF55));
        number.setBillboardConstraints(Display.BillboardConstraints.CENTER);
        number.setNoGravity(true);
        number.setViewRange(TargetDummyMod.NUMBER_VIEW_RANGE);
        number.setShadow(true);
        number.setSeeThrough(true);
        number.setDefaultBackground(false);

        world.addFreshEntity(number);
        DamageNumberTracker.track(number, 18);
    }

    private String formatDamage(float damage) {
        String text = String.format(Locale.ROOT, "%.1f", damage);
        if (text.endsWith(".0")) {
            return text.substring(0, text.length() - 2);
        }
        return text;
    }
}
