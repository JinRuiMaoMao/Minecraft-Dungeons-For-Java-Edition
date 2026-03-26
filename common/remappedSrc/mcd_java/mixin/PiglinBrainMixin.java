
package mcd_java.mixin;

import mcd_java.Mcda;
import mcd_java.api.CleanlinessHelper;
import mcd_java.items.ArmorSets;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mcd_java.effects.ArmorEffectID.PIGLIN_FOOLING;

@Mixin(PiglinAi.class)
public abstract class PiglinBrainMixin {

    @Inject(method = "wearsGoldArmor", at = @At(value = "RETURN"), cancellable = true)
    private static void mcda$onPiglinSelectPlayerToAttack(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(PIGLIN_FOOLING))
            return;
        if (livingEntity instanceof Player)
            if (CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.GOLDEN_PIGLIN))
                cir.setReturnValue(true);
    }
}

