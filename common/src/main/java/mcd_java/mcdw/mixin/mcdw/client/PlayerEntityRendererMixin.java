/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw.client;

import mcd_java.mcdw.bases.McdwCrossbow;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.PlayerEntity.AbstractClientPlayer;
import net.minecraft.client.render.entity.PlayerEntity.PlayerRenderer;
import net.minecraft.util.Hand;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(
            method = "getArmPose(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;",
            at = @At(value = "TAIL"),
            cancellable = true
    )
    private static void mcdw$getArmPose(AbstractClientPlayer PlayerEntity, Hand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        ItemStack itemStack = PlayerEntity.getItemInHand(hand);

        if (!PlayerEntity.swinging
                && (itemStack.getItem() instanceof McdwCrossbow)
                && CrossbowItem.isCharged(itemStack)) {
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
        }
    }
}
