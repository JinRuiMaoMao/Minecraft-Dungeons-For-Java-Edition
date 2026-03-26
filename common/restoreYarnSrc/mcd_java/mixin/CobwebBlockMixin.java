package mcd_java.mixin;

import mcd_java.Mcda;
import mcd_java.api.CleanlinessHelper;
import mcd_java.effects.ArmorEffects;
import mcd_java.items.ArmorSets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mcd_java.effects.ArmorEffectID.WEB_WALKING;

@Mixin(WebBlock.class)
public class CobwebBlockMixin {
    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void mcda$canWalkThroughCobwebs(BlockState state, Level world, BlockPos pos, Entity entity, CallbackInfo ci){
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
