package mcd_java.mixin;

import mcd_java.Mcda;
import mcd_java.api.CleanlinessHelper;
import mcd_java.items.ArmorSets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mcd_java.effects.ArmorEffectID.SWEET_BERRY_BUSH_WALKING;

@Mixin(SweetBerryBushBlock.class)
public class SweetBerryBushBlockMixin {

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void mcda$canWalkThroughSweetBerryBushes(BlockState state, Level world, BlockPos pos, Entity entity, CallbackInfo ci){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(SWEET_BERRY_BUSH_WALKING))
            return;

        if (!(entity instanceof LivingEntity livingEntity))
            return;

        if (CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.FOX) || CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.ARCTIC_FOX)){
            ci.cancel();
        }
    }
}
