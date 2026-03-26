/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw.client;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.enums.EnchantmentsID;
import mcd_java.mcdw.registries.EnchantsRegistry;
import mcd_java.mcdw.registries.StatusEffectsRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.render.item.ItemProperties;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("InjectCouldBeOverwrite")
@Environment(EnvType.CLIENT)
@Mixin(ItemProperties.class)
public class ModelPredicateProviderRegistryMixin {

    // This Inject allows for the visuals of Accelerate and Overcharge to work on Vanilla Bows.
    // This @link ModelPredicateProviderRegistry#method_27890 is the Bow "pull" predicate
    @Inject(method = "method_27890(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/entity/LivingEntity;I)F", at = @At("HEAD"), cancellable = true)
    private static void mcdw$blackMagicFuckery(ItemStack stack, ClientWorld world, LivingEntity entity, int seed, CallbackInfoReturnable<Float> cir){
        cir.cancel();
        if (entity == null) {
            cir.setReturnValue(0.0F);
        } else {
            int useTicks = stack.getUseDuration() - entity.getUseItemRemainingTicks();
            if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.ACCELERATE).mcdw$getIsEnabled()) {
                int accelerateLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.ACCELERATE), stack);
                if (accelerateLevel > 0) {
                    StatusEffectInstance accelerateInstance = entity.getEffect(StatusEffectsRegistry.ACCELERATE);
                    int consecutiveShots = accelerateInstance != null ? accelerateInstance.getAmplifier() + 1 : 0;
                    useTicks = (int) (useTicks * (1f + (Mth.clamp(consecutiveShots * (6.0f + 2.0f * accelerateLevel), 0f, 100f) / 100f)));
                }
            }
            if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.OVERCHARGE).mcdw$getIsEnabled()) {
                int overchargeLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.OVERCHARGE), stack);
                if (overchargeLevel > 0) {
                    int overcharge = Math.min((useTicks / 20) - 1, overchargeLevel);
                    useTicks = overcharge == overchargeLevel ? useTicks : (useTicks % 20);
                }
            }
            cir.setReturnValue(entity.getUseItem() != stack ? 0.0F :
                    (float)(useTicks) / 20);
        }
    }

    // This Inject allows for the visuals of Accelerate to work on Vanilla Crossbows.
    // This @link ModelPredicateProviderRegistry#method_27888 is the Crossbow "pull" predicate
    @Inject(method = "method_27888(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/entity/LivingEntity;I)F", at = @At("HEAD"), cancellable = true)
    private static void mcdw$blackMagicFuckeryII(ItemStack stack, ClientWorld world, LivingEntity entity, int seed, CallbackInfoReturnable<Float> cir){
        cir.cancel();
        if (entity == null) {
            cir.setReturnValue(0.0F);
        } else {
            int useTicks = stack.getUseDuration() - entity.getUseItemRemainingTicks();
            if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.ACCELERATE).mcdw$getIsEnabled()) {
                int accelerateLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.ACCELERATE), stack);
                if (accelerateLevel > 0) {
                    StatusEffectInstance accelerateInstance = entity.getEffect(StatusEffectsRegistry.ACCELERATE);
                    int consecutiveShots = accelerateInstance != null ? accelerateInstance.getAmplifier() + 1 : 0;

                    useTicks = (int) (useTicks * (1f + (Mth.clamp(consecutiveShots * (6.0f + 2.0f * accelerateLevel), 0f, 100f) / 100f)));
                }
            }
            cir.setReturnValue(CrossbowItem.isCharged(stack) ? 0.0F :
                    (float) (useTicks) / CrossbowItem.getChargeDuration(stack));
        }
    }
}
