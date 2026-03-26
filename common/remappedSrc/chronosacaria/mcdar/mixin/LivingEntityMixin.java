package jinrui.mcdar.mixin;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.effects.ArtifactEffects;
import jinrui.mcdar.effects.EnchantmentEffects;
import jinrui.mcdar.enchants.EnchantmentsID;
import jinrui.mcdar.enums.DamagingArtifactID;
import jinrui.mcdar.registries.StatusEffectInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ConstantConditions")
@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
    public void onSoulProtectionDeath(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (livingEntity.hasEffect(StatusEffectInit.SOUL_PROTECTION)) {
            livingEntity.setHealth(1.0F);
            livingEntity.removeAllEffects();
            livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 900, 1));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));

            cir.setReturnValue(true);
        }
    }

    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;)V", at = @At("HEAD"), cancellable = true)
    public void onAttackWhilstStunnedNoTarget(InteractionHand hand, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (livingEntity.hasEffect(StatusEffectInit.STUNNED)) {
            ci.cancel();
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onPowershakerExplodingKill(DamageSource source, CallbackInfo ci) {
        if (!(source.getEntity() instanceof Player player)) return;

        LivingEntity target = (LivingEntity) (Object) this;

        if (Mcdar.CONFIG.mcdarArtifactsStatsConfig.DAMAGING_ARTIFACT_STATS.get(DamagingArtifactID.POWERSHAKER).mcdar$getIsEnabled())
            ArtifactEffects.activatePowerShaker(player, target);
    }

    @ModifyVariable(method = "damage", at = @At(value = "HEAD"), argsOnly = true)
    public float mcdar$damageModifiers(float amount, DamageSource source) {
        if (source.getDirectEntity() instanceof OwnableEntity summonedEntity) {
            if (source.getDirectEntity().level() instanceof ServerLevel serverWorld) {

                if (Mcdar.CONFIG.mcdarEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.BEAST_BOSS).mcdar$getIsEnabled())
                    amount *= EnchantmentEffects.beastBossDamage(summonedEntity, serverWorld);

            }
        }
        return amount;
    }

    @Inject(method = "consumeItem", at = @At("HEAD"))
    public void mcdar$onConsume(CallbackInfo ci) {

        if (!((Object) this instanceof Player player)) return;

        if (player.isAlive() && player.level() instanceof ServerLevel) {

            if (Mcdar.CONFIG.mcdarEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.BEAST_BURST).mcdar$getIsEnabled())
                EnchantmentEffects.activateBeastBurst(player);
            if (Mcdar.CONFIG.mcdarEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.BEAST_SURGE).mcdar$getIsEnabled())
                EnchantmentEffects.activateBeastSurge(player);
        }
    }
}