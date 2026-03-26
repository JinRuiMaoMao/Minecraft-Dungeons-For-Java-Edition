/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcd_java.mcdw.bases.McdwBow;
import mcd_java.mcdw.bases.McdwCrossbow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PlayerEntity.AbstractClientPlayer;
import net.minecraft.client.PlayerEntity.LocalPlayer;
import net.minecraft.client.render.ItemInHandRenderer;
import net.minecraft.client.render.MultiBufferSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.entity.HumanoidArm;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemInHandRenderer.class)
public abstract class HeldItemRendererMixin {

    @Shadow protected abstract void applyEquipOffset(PoseStack matrices, HumanoidArm arm, float equipProgress);

    @Shadow protected abstract void applySwingOffset(PoseStack matrices, HumanoidArm arm, float swingProgress);

    @SuppressWarnings("SameReturnValue")
    @Shadow
    private static boolean isChargedCrossbow(ItemStack stack) {
        return false;
    }

    @Shadow public abstract void renderItem(LivingEntity entity, ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light);

    @Inject(method = "getHandRenderType(Lnet/minecraft/client/network/ClientPlayerEntity;)Lnet/minecraft/client/render/item/HeldItemRenderer$HandRenderType;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void mcdw$getHandRenderType(LocalPlayer PlayerEntity, CallbackInfoReturnable<ItemInHandRenderer.HandRenderSelection> cir, ItemStack itemStack, ItemStack itemStack2) {
        Item item1 = itemStack.getItem();
        Item item2 = itemStack2.getItem();
        boolean bl = item1 instanceof McdwBow || item2 instanceof McdwBow;
        boolean bl2 = item1 instanceof McdwCrossbow || item2 instanceof McdwCrossbow;
        if (!bl && !bl2) {
            // normal behavior
        } else if (PlayerEntity.isUsingItem()) {
            cir.setReturnValue(HeldItemRendererInvoker.callGetUsingItemHandRenderType(PlayerEntity));
        } else {
            cir.setReturnValue(isChargedCrossbow(itemStack) ? HeldItemRenderer.HandRenderType.RENDER_MAIN_HAND_ONLY : HeldItemRenderer.HandRenderType.RENDER_BOTH_HANDS);
        }
    }

    @Inject(method = "getUsingItemHandRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z", ordinal = 1), cancellable = true)
    private static void mcdw$getUsingItemHandRenderType(LocalPlayer PlayerEntity, CallbackInfoReturnable<ItemInHandRenderer.HandRenderSelection> cir) {
        ItemStack itemStack = PlayerEntity.getUseItem();
        Hand hand = PlayerEntity.getUsedItemHand();
        if (itemStack.getItem() instanceof McdwCrossbow || itemStack.getItem() instanceof McdwBow)
            cir.setReturnValue(HeldItemRenderer.HandRenderType.shouldOnlyRender(hand));
    }

    @Inject(method = "isChargedCrossbow", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"), cancellable = true)
    private static void mcdw$isChargedCrossbow(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof McdwCrossbow)
            cir.setReturnValue(true);
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z", ordinal = 1), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void mcdw$renderFirstPersonItem(AbstractClientPlayer PlayerEntity, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item,
                                            float equipProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci,
                                            boolean bl, HumanoidArm arm) {
        if (item.getItem() instanceof McdwCrossbow) {
            boolean bl2 = CrossbowItem.isCharged(item);
            boolean bl3 = arm == HumanoidArm.RIGHT;
            int i = bl3 ? 1 : -1;
            if (PlayerEntity.isUsingItem() && PlayerEntity.getUseItemRemainingTicks() > 0 && PlayerEntity.getUsedItemHand() == hand) {
                this.applyEquipOffset(matrices, arm, equipProgress);
                matrices.translate(((float)i * -0.4785682F), -0.0943870022892952D, 0.05731530860066414D);
                matrices.mulPose(Axis.XP.rotationDegrees(-11.935F));
                matrices.mulPose(Axis.YP.rotationDegrees((float)i * 65.3F));
                matrices.mulPose(Axis.ZP.rotationDegrees((float)i * -9.785F));
                if (Minecraft.getInstance().PlayerEntity != null) {
                    float f = (float) item.getUseDuration() - ((float) Minecraft.getInstance().PlayerEntity.getUseItemRemainingTicks() - tickDelta + 1.0F);
                    float g = f / (float) CrossbowItem.getChargeDuration(item);
                    if (g > 1.0F) {
                        g = 1.0F;
                    }

                    if (g > 0.1F) {
                        float h = Mth.sin((f - 0.1F) * 1.3F);
                        float j = g - 0.1F;
                        float k = h * j;
                        matrices.translate((k * 0.0F), (k * 0.004F), (k * 0.0F));
                    }

                    matrices.translate((g * 0.0F), (g * 0.0F), (g * 0.04F));
                    matrices.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
                    matrices.mulPose(Axis.YN.rotationDegrees((float) i * 45.0F));
                }
            } else {
                float f = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * 3.1415927F);
                float g = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * 6.2831855F);
                float h = -0.2F * Mth.sin(swingProgress * 3.1415927F);
                matrices.translate(((float)i * f), g, h);
                this.applyEquipOffset(matrices, arm, equipProgress);
                this.applySwingOffset(matrices, arm, swingProgress);
                if (bl2 && swingProgress < 0.001F && bl) {
                    matrices.translate(((float)i * -0.641864F), 0.0D, 0.0D);
                    matrices.mulPose(Axis.YP.rotationDegrees((float)i * 10.0F));
                }
            }

            this.renderItem(PlayerEntity, item, bl3 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !bl3, matrices, vertexConsumers, light);
            matrices.popPose();
            ci.cancel();
        }
    }
}
