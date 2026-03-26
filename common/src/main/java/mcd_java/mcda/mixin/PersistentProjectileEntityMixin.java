
package mcd_java.mcda.mixin;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.api.ProjectileEffectHelper;
import mcd_java.mcda.registries.EnchantsRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mcd_java.mcda.enchants.EnchantID.DEFLECT;

@Mixin(AbstractArrow.class)
public abstract class PersistentProjectileEntityMixin {

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
    public void mcda$onDeflectHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(DEFLECT))
            return;

        PersistentProjectileEntity persistentProjectileEntity = (AbstractArrow) (Object) this;
        if (!(persistentProjectileEntity.getOwner() instanceof LivingEntity)) return;
        if (!((entityHitResult.getEntity()) instanceof LivingEntity)) return;

        Entity victim = entityHitResult.getEntity();
        int deflectLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(DEFLECT), (LivingEntity) victim);
        if (deflectLevel == 0) return;

        double originalDamage = persistentProjectileEntity.getBaseDamage();
        double deflectChance = deflectLevel * 0.2F;
        float deflectRand = ((LivingEntity)victim).getRandom().nextFloat();
        if (EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(DEFLECT), (LivingEntity) victim) > 0){
            if (deflectRand <= deflectChance) {
                persistentProjectileEntity.setBaseDamage(originalDamage * 0.5D);
                if (ci.isCancellable()) {
                    ci.cancel();
                }
                ProjectileEffectHelper.ricochetArrowLikeShield(persistentProjectileEntity);
            }
        }
    }
}