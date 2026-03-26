package net.backupcup.mcde.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.backupcup.mcde.MCDEnchantments;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.block.EnchantmentTableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;

@Mixin(EnchantmentTableBlock.class)
public class EnchantingTableBlockMixin {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void mcde$restrictUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (MCDEnchantments.getConfig().isUsingEnchantingTableAllowed()) {
            return;
        }
        if (!PlayerEntity.isCreative()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
