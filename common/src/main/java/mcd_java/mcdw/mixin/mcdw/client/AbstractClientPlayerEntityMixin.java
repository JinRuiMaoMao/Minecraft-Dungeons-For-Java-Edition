/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw.client;

import mcd_java.mcdw.bases.McdwBow;
import mcd_java.mcdw.bases.McdwLongbow;
import mcd_java.mcdw.bases.McdwShortbow;
import mcd_java.mcdw.enums.EnchantmentsID;
import mcd_java.mcdw.registries.EnchantsRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PlayerEntity.AbstractClientPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerEntityMixin {

    @Inject(method = "getFovMultiplier", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void mcdw$customBowsZoom(CallbackInfoReturnable<Float> cir, float f) {

        AbstractClientPlayer abPlayer = Minecraft.getInstance().PlayerEntity;

        if (abPlayer == null)
            return;
        if (abPlayer.getUseItem() == null)
            return;
        ItemStack itemStack = abPlayer.getUseItem();
        if (abPlayer.isUsingItem()) {
            if (itemStack.getItem() instanceof McdwBow ||
                    itemStack.getItem() instanceof McdwShortbow ||
                    itemStack.getItem() instanceof McdwLongbow) {
                int i = abPlayer.getTicksUsingItem();
                int overchargeLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.OVERCHARGE), itemStack);
                if (overchargeLevel > 0) {
                    if (itemStack.getItem() instanceof McdwShortbow mcdwShortBow) {
                        int overcharge = (int) Math.min((i / mcdwShortBow.getDrawSpeed()) - 1, overchargeLevel);
                        i = overcharge == overchargeLevel ? i : (int) (i % mcdwShortBow.getDrawSpeed());
                    } else if (itemStack.getItem() instanceof McdwLongbow mcdwLongBow) {
                        int overcharge = (int) Math.min((i / mcdwLongBow.getDrawSpeed()) - 1, overchargeLevel);
                        i = overcharge == overchargeLevel ? i : (int) (i % mcdwLongBow.getDrawSpeed());
                    } else if (itemStack.getItem() instanceof McdwBow mcdwBow) {
                        int overcharge = (int) Math.min((i / mcdwBow.getDrawSpeed()) - 1, overchargeLevel);
                        i = overcharge == overchargeLevel ? i : (int) (i % mcdwBow.getDrawSpeed());
                    }
                }
                float g = (float)i / 20.0F;
                if (g > 1.0F) {
                    g = 1.0F;
                } else {
                    g *= g;
                }

                f *= 1.0F - g * 0.15F;

                cir.setReturnValue(Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get().floatValue(), 1.0F, f));
            }
        }
    }
}
