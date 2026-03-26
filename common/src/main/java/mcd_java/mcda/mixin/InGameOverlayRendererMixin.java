package mcd_java.mcda.mixin;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.api.CleanlinessHelper;
import mcd_java.mcda.effects.ArmorEffects;
import mcd_java.mcda.items.ArmorSets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.ScreenEffectRenderer;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mcd_java.mcda.effects.ArmorEffectID.FIRE_RESISTANCE;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(ScreenEffectRenderer.class)
public class InGameOverlayRendererMixin {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void mcda$renderFireOverlayOverride(Minecraft minecraftClient, PoseStack matrixStack,
                                                 CallbackInfo ci) {
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(FIRE_RESISTANCE))
            return;

        PlayerEntity playerEntity = Minecraft.getInstance().PlayerEntity;

        if (playerEntity.isAlive()) {
            if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.SPROUT)
                    || CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.LIVING_VINES)
                    || (ArmorEffects.ARMOR_EFFECT_ID_LIST.get(ArmorEffects.applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == FIRE_RESISTANCE)
                    || (ArmorEffects.RED_ARMOR_EFFECT_ID_LIST.get(ArmorEffects.applyMysteryArmorEffect(playerEntity, ArmorSets.RED_MYSTERY)) == FIRE_RESISTANCE)) {
                ci.cancel();
            }
        }
    }
}