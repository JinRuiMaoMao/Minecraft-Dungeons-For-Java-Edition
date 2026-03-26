/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.api.interfaces.IMcdwEnchantedArrow;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.api.util.ProjectileEffectHelper;
import mcd_java.mcdw.api.util.RangedAttackHelper;
import mcd_java.mcdw.bases.McdwCrossbow;
import mcd_java.mcdw.enums.CrossbowsID;
import mcd_java.mcdw.enums.EnchantmentsID;
import mcd_java.mcdw.registries.EnchantsRegistry;
import mcd_java.mcdw.registries.ItemsRegistry;
import mcd_java.mcdw.registries.StatusEffectsRegistry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @Unique
    private LivingEntity livingEntity;

    @Unique
    public void mcdw$setLivingEntity(LivingEntity livingEntity){
        this.livingEntity = livingEntity;
    }

    @Inject(method = "createArrow", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void mcdw$applyCrossbowEnchantmentLevel(World world, LivingEntity user, ItemStack crossbow, ItemStack arrow,
                                    CallbackInfoReturnable<AbstractArrow> cir, ArrowItem arrowItem,
                                    PersistentProjectileEntity ppe){
        if (CrossbowItem.isCharged(crossbow)) {
            CleanlinessHelper.addPPEEnchantments(crossbow, (IMcdwEnchantedArrow) ppe);
            // For Drag in Water for Nautilus Crossbow
            ((IMcdwEnchantedArrow)ppe).mcdw$setNautilusBoolean(crossbow.is(ItemsRegistry.CROSSBOW_ITEMS.get(CrossbowsID.CROSSBOW_NAUTICAL_CROSSBOW)));
            ((IMcdwEnchantedArrow)ppe).mcdw$setShadowBarbBoolean(crossbow.is(ItemsRegistry.CROSSBOW_ITEMS.get(CrossbowsID.CROSSBOW_VEILED_CROSSBOW)));


            if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.BONUS_SHOT).mcdw$getIsEnabled()) {
                int bonusShotLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.BONUS_SHOT), crossbow);
                if (bonusShotLevel > 0) {
                    float damageMultiplier = 0.1F + ((bonusShotLevel - 1) * 0.07F);

                    @SuppressWarnings("unused")
                    float arrowVelocity = RangedAttackHelper.getVanillaOrModdedCrossbowArrowVelocity(crossbow);
                    ProjectileEffectHelper.mcdw$spawnExtraArrows(user, user, 1, 10, damageMultiplier);
                }
            }
        }
    }


    @Inject(method = "onStoppedUsing", at = @At(value = "HEAD"))
    private void mcdw$livingEntityView(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        this.mcdw$setLivingEntity(user);
    }

    @ModifyArg(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;getPullProgress(ILnet/minecraft/item/ItemStack;)F"), index = 0)
    private int mcdw$acceleratedPullProgress(int useTicks) {
        ItemStack crossbowStack = livingEntity.getUseItem();

        if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.ACCELERATE).mcdw$getIsEnabled()) {
            int accelerateLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.ACCELERATE), crossbowStack);
            if (accelerateLevel > 0) {
                StatusEffectInstance accelerateInstance = livingEntity.getEffect(StatusEffectsRegistry.ACCELERATE);
                int consecutiveShots = accelerateInstance != null ? accelerateInstance.getAmplifier() + 1 : 0;

                useTicks = (int) (useTicks * (1f + (Mth.clamp(consecutiveShots * (6.0f + 2.0f * accelerateLevel), 0f, 100f) / 100f)));

                if ((float)useTicks / (float)CrossbowItem.getChargeDuration(crossbowStack) >= 1) {
                    StatusEffectInstance accelerateUpdateInstance =
                            new StatusEffectInstance(StatusEffectsRegistry.ACCELERATE, 60, consecutiveShots, false, false, true);
                    livingEntity.addStatusEffect(accelerateUpdateInstance);
                }
            }
        }
        return useTicks;
    }

    @ModifyArgs(method = "shootAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;shoot(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;FZFFF)V"))
    private static void mcdw$crossbowRangeHandler(Args args) {
        float velocity = args.get(7);
        ItemStack crossbowStack = args.get(3);
        if (crossbowStack.getItem() instanceof McdwCrossbow mcdwCrossbow) {
            velocity *= mcdwCrossbow.getDefaultProjectileRange() / 8f;
        }

        args.set(7, velocity);
    }

    @Inject(method = "getPullTime", at = @At(value = "RETURN"), cancellable = true)
    private static void mcdw$crossbowPullTimeHandler(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int pullTime = cir.getReturnValue();
        if (stack.getItem() instanceof McdwCrossbow mcdwCrossbow) {
            pullTime += mcdwCrossbow.getDrawSpeed() - 25;
        }
        cir.setReturnValue(Math.max(0, pullTime));
    }
}
