package jinrui.mcdar.mixin;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.ProjectileEffectHelper;
import jinrui.mcdar.enums.QuiverArtifactID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BowItem.class)
public abstract class BowItemMixin {

    @Inject(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), locals =
            LocalCapture.CAPTURE_FAILHARD)
    public void onFlamingQuiverArrowLoosing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks,
                                            CallbackInfo ci, Player playerEntity, boolean bl,
                                            ItemStack itemStack, int i, float f, boolean bl2, ArrowItem arrowItem, AbstractArrow persistentProjectileEntity){
        if (Mcdar.CONFIG.mcdarArtifactsStatsConfig.QUIVER_ARTIFACT_STATS.get(QuiverArtifactID.FLAMING_QUIVER).mcdar$getIsEnabled()){
            if (playerEntity.getOffhandItem().is(QuiverArtifactID.FLAMING_QUIVER.mcdar$getItem())) {
                float effectTimer = playerEntity.getCooldowns().getCooldownPercent(playerEntity.getOffhandItem().getItem(), 0);
                if (effectTimer > 0) {
                    ProjectileEffectHelper.flamingQuiverArrow(persistentProjectileEntity);
                }
            }
        }
    }
}
