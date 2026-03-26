package mcd_java.mcda.mixin;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.api.CleanlinessHelper;
import mcd_java.mcda.items.ArmorSets;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mcd_java.mcda.effects.ArmorEffectID.SWEET_BERRY_BUSH_WALKING;

@Mixin(SweetBerryBushBlock.class)
public class SweetBerryBushBlockMixin {

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void mcda$canWalkThroughSweetBerryBushes(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(SWEET_BERRY_BUSH_WALKING))
            return;

        if (!(entity instanceof LivingEntity livingEntity))
            return;

        if (CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.FOX) || CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.ARCTIC_FOX)){
            ci.cancel();
        }
    }
}
