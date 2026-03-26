
package mcd_java.mixin;

import mcd_java.Mcda;
import mcd_java.api.ProjectileEffectHelper;
import mcd_java.registries.EnchantsRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mcd_java.enchants.EnchantID.DEFLECT;

@Mixin(AbstractArrow.class)
public abstract class PersistentProjectileEntityMixin {

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
    public void mcda$onDeflectHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(DEFLECT))
            return;

        AbstractArrow persistentProjectileEntity = (AbstractArrow) (Object) this;
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