package mcd_java.mixin;

import mcd_java.Mcda;
import mcd_java.api.CleanlinessHelper;
import mcd_java.effects.ArmorEffects;
import mcd_java.items.ArmorSets;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mcd_java.effects.ArmorEffectID.INVISIBILITY;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(HumanoidArmorLayer.class)
public class ArmorFeatureRendererMixin {

    // Hide Thief Armour on Sneak
    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    public void mcda$renderArmorOverride(PoseStack matrices, MultiBufferSource vertexConsumers,
                                 LivingEntity livingEntity, EquipmentSlot equipmentSlot, int i, HumanoidModel<LivingEntity> bipedEntityModel, CallbackInfo info) {
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(INVISIBILITY))
            return;

        if (livingEntity instanceof Player && livingEntity.isShiftKeyDown()){
            if (CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.THIEF)
                    || (ArmorEffects.ARMOR_EFFECT_ID_LIST.get(ArmorEffects.applyMysteryArmorEffect(livingEntity, ArmorSets.MYSTERY)) == INVISIBILITY)
                    || (ArmorEffects.PURPLE_ARMOR_EFFECT_ID_LIST.get(ArmorEffects.applyMysteryArmorEffect(livingEntity, ArmorSets.PURPLE_MYSTERY)) == INVISIBILITY)){
                info.cancel();
            }
        }
    }
}
