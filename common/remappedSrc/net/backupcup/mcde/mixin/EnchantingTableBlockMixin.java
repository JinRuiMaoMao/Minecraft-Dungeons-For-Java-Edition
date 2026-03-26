package net.backupcup.mcde.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.backupcup.mcde.MCDEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(EnchantmentTableBlock.class)
public class EnchantingTableBlockMixin {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void mcde$restrictUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (MCDEnchantments.getConfig().isUsingEnchantingTableAllowed()) {
            return;
        }
        if (!player.isCreative()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
