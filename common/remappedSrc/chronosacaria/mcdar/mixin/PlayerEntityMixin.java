package jinrui.mcdar.mixin;

import jinrui.mcdar.registries.StatusEffectInit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerEntityMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void onPlayerAttackWhilstStunnedTarget(Entity target, CallbackInfo ci) {
        if (((Player) (Object) this).hasEffect(StatusEffectInit.STUNNED)){
            ci.cancel();
        }
    }

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    public void onPlayerMovementWhilstStunnedTarget(CallbackInfo ci) {
        if (((Player) (Object) this).hasEffect(StatusEffectInit.STUNNED)){
            ci.cancel();
        }
    }
}
