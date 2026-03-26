package jinrui.mcdar.mixin;

import jinrui.mcdar.registries.StatusEffectInit;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void onUsingBowWhilstStunned(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir){
        if (user.hasEffect(StatusEffectInit.STUNNED)){
            cir.setReturnValue(InteractionResultHolder.fail((ItemStack) (Object) this));
        }
    }
}
