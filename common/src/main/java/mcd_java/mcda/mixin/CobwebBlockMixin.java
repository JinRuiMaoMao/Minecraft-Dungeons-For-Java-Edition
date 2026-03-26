package mcd_java.mcda.mixin;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.api.CleanlinessHelper;
import mcd_java.mcda.effects.ArmorEffects;
import mcd_java.mcda.items.ArmorSets;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mcd_java.mcda.effects.ArmorEffectID.WEB_WALKING;

@Mixin(WebBlock.class)
public class CobwebBlockMixin {
    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void mcda$canWalkThroughCobwebs(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(WEB_WALKING))
            return;

        if (!(entity instanceof LivingEntity livingEntity))
            return;

        if (CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.SPIDER)
                || (ArmorEffects.ARMOR_EFFECT_ID_LIST.get(ArmorEffects.applyMysteryArmorEffect(livingEntity, ArmorSets.MYSTERY)) == WEB_WALKING)
                || (ArmorEffects.PURPLE_ARMOR_EFFECT_ID_LIST.get(ArmorEffects.applyMysteryArmorEffect(livingEntity, ArmorSets.PURPLE_MYSTERY)) == WEB_WALKING)){
            ci.cancel();
        }
    }
}
