/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw;

import mcd_java.mcdw.enums.SwordsID;
import mcd_java.mcdw.registries.ItemsRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.SmithingMenu;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingMenu.class)
public class SmithingScreenHandlerMixin {

    @Inject(method = "onTakeOutput", at = @At(value = "TAIL"))
    public void mcdw$onTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (stack.getItem() == ItemsRegistry.SWORD_ITEMS.get(SwordsID.SWORD_MECHANIZED_SAWBLADE))
            stack.enchant(Enchantments.FIRE_ASPECT, 1);
    }
}
