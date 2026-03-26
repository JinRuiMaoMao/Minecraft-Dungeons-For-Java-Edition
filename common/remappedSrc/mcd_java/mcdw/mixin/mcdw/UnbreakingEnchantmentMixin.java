/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw;

import mcd_java.mcdw.enums.SwordsID;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DigDurabilityEnchantment.class)
public class UnbreakingEnchantmentMixin {
    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    public void mcdw$isAcceptableItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(SwordsID.SWORD_BROKEN_SAWBLADE.getItem()) || stack.is(SwordsID.SWORD_MECHANIZED_SAWBLADE.getItem()))
            cir.setReturnValue(false);
    }
}
