package jinrui.mcdar.mixin;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.ProjectileEffectHelper;
import jinrui.mcdar.enums.QuiverArtifactID;
import jinrui.mcdar.registries.StatusEffectInit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractArrow.class)
public class PersistentProjectileEntityMixin {

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
    public void mcdar$entityHitPpe(EntityHitResult entityHitResult, CallbackInfo ci){
        AbstractArrow ppe = ((AbstractArrow) (Object) this);

        if (entityHitResult.getEntity() instanceof LivingEntity le) {
            if (ppe.getOwner() instanceof Player shooter) {
                ItemStack offhand = shooter.getOffhandItem();

                // * Quivers * //
                if (CleanlinessHelper.isCoolingDown(shooter, offhand.getItem())) {

                    if (Mcdar.CONFIG.mcdarArtifactsStatsConfig.QUIVER_ARTIFACT_STATS.get(QuiverArtifactID.THUNDERING_QUIVER).mcdar$getIsEnabled())
                        if (offhand.is(QuiverArtifactID.THUNDERING_QUIVER.mcdar$getItem()))
                            AOEHelper.electrocute(le, (float) ppe.getBaseDamage());
                    if (Mcdar.CONFIG.mcdarArtifactsStatsConfig.QUIVER_ARTIFACT_STATS.get(QuiverArtifactID.TORMENT_QUIVER).mcdar$getIsEnabled())
                        if (offhand.is(QuiverArtifactID.TORMENT_QUIVER.mcdar$getItem()))
                            ppe.setKnockback(1);

                }
            }
            // * Shielding Status Effect * //
            if (le.hasEffect(StatusEffectInit.SHIELDING)){
                if (ci.isCancellable())
                    ci.cancel();
                ProjectileEffectHelper.ricochetArrowLikeShield(ppe);
            }
        }
    }

    @Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
    public void onTormentingArrowBlockImpact(BlockHitResult blockHitResult, CallbackInfo ci){
        AbstractArrow ppe = (AbstractArrow) (Object) this;

        if (Mcdar.CONFIG.mcdarArtifactsStatsConfig.QUIVER_ARTIFACT_STATS.get(QuiverArtifactID.TORMENT_QUIVER).mcdar$getIsEnabled()) {
            if (ppe.getOwner() instanceof Player shooter) {
                ItemStack offhand = shooter.getOffhandItem();

                if (offhand.is(QuiverArtifactID.TORMENT_QUIVER.mcdar$getItem())) {
                    if (CleanlinessHelper.isCoolingDown(shooter, offhand.getItem())) {
                        if (ci.isCancellable()) {
                            ci.cancel();
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "getDragInWater", at = @At("RETURN"), cancellable = true)
    public void onHarpoonArrowFire(CallbackInfoReturnable<Float> cir) {
        AbstractArrow ppe = (AbstractArrow) (Object) this;

        if (Mcdar.CONFIG.mcdarArtifactsStatsConfig.QUIVER_ARTIFACT_STATS.get(QuiverArtifactID.HARPOON_QUIVER).mcdar$getIsEnabled()) {
            if (ppe.getOwner() instanceof Player shooter) {
                ItemStack offhand = shooter.getOffhandItem();

                if (offhand.is(QuiverArtifactID.HARPOON_QUIVER.mcdar$getItem())) {
                    if (CleanlinessHelper.isCoolingDown(shooter, offhand.getItem())) {
                        if (ppe.isInWater()) {
                            float normDrag = cir.getReturnValueF();
                            float v = (cir.getReturnValue() == null ? 0.6F : normDrag) * 1.542f;
                            cir.setReturnValue(v);
                        }
                    }
                }
            }
        }
    }
}
