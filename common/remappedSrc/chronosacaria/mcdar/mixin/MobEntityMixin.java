package jinrui.mcdar.mixin;

import jinrui.mcdar.registries.StatusEffectInit;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobEntityMixin {
    @Inject(method = "isAiDisabled", at = @At("HEAD"), cancellable = true)
    public void onStunnedMob(CallbackInfoReturnable<Boolean> cir){
        if (((Mob) (Object) this).hasEffect(StatusEffectInit.STUNNED)){
            cir.setReturnValue(true);
        }
    }
}
