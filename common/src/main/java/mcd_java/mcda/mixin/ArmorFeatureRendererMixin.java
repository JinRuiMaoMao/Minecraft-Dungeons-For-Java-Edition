package mcd_java.mcda.mixin;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.api.CleanlinessHelper;
import mcd_java.mcda.effects.ArmorEffects;
import mcd_java.mcda.items.ArmorSets;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.render.MultiBufferSource;
import net.minecraft.client.render.entity.layers.HumanoidArmorLayer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mcd_java.mcda.effects.ArmorEffectID.INVISIBILITY;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(HumanoidArmorLayer.class)
public class ArmorFeatureRendererMixin {

    // Hide Thief Armour on Sneak
    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    public void mcda$renderArmorOverride(PoseStack matrices, MultiBufferSource vertexConsumers,
                                 LivingEntity livingEntity, EquipmentSlot equipmentSlot, int i, HumanoidModel<LivingEntity> bipedEntityModel, CallbackInfo info) {
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(INVISIBILITY))
            return;

        if (livingEntity instanceof PlayerEntity && livingEntity.isShiftKeyDown()){
            if (CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.THIEF)
                    || (ArmorEffects.ARMOR_EFFECT_ID_LIST.get(ArmorEffects.applyMysteryArmorEffect(livingEntity, ArmorSets.MYSTERY)) == INVISIBILITY)
                    || (ArmorEffects.PURPLE_ARMOR_EFFECT_ID_LIST.get(ArmorEffects.applyMysteryArmorEffect(livingEntity, ArmorSets.PURPLE_MYSTERY)) == INVISIBILITY)){
                info.cancel();
            }
        }
    }
}
