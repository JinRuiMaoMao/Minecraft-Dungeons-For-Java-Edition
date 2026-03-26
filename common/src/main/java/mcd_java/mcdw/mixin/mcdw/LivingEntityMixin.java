/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.api.util.AOEHelper;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.damagesources.OffHandDamageSource;
import mcd_java.mcdw.effects.EnchantmentEffects;
import mcd_java.mcdw.enchants.summons.IBeeSummoning;
import mcd_java.mcdw.enchants.summons.entity.SummonedBeeEntity;
import mcd_java.mcdw.enums.EnchantmentsID;
import mcd_java.mcdw.enums.ItemsID;
import mcd_java.mcdw.enums.SettingsID;
import mcd_java.mcdw.enums.SwordsID;
import mcd_java.mcdw.registries.EnchantsRegistry;
import mcd_java.mcdw.registries.EntityAttributesRegistry;
import mcd_java.mcdw.registries.ItemsRegistry;
import mcd_java.mcdw.registries.SummonedEntityRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@SuppressWarnings("ConstantLootNumberProvider")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Unique
    public final EntityType<SummonedBeeEntity> mcdw$summoned_bee =
            SummonedEntityRegistry.SUMMONED_BEE_ENTITY;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyVariable(method = "damage", at = @At(value = "HEAD"), argsOnly = true)
    public float mcdw$damageModifiers(float amount, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attackingEntity))
            return amount;

        if (amount > 0) {
            float storedAmount = amount * Mcdw.CONFIG.mcdwEnchantmentsConfig.directDamageEnchantmentMultiplier;
            if (attackingEntity instanceof TameableEntity petSource
                    && petSource.getWorld() instanceof ServerWorld serverWorld
                    && petSource.getOwner() instanceof PlayerEntity owner) {

                amount += storedAmount * EnchantmentEffects.huntersPromiseDamage(owner, serverWorld);
            }
        }

        return amount;
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void mcdw$onDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity victim = (LivingEntity) (Object) this;
        boolean isOffHandAttack = source instanceof OffHandDamageSource;

        if (source.getEntity() instanceof LivingEntity attackingEntity) {

            if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.PROSPECTOR).mcdw$getIsEnabled())
                EnchantmentEffects.applyProspector(attackingEntity, victim, isOffHandAttack);
            if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.RUSHDOWN).mcdw$getIsEnabled())
                EnchantmentEffects.applyRushdown(attackingEntity, isOffHandAttack);
        }

        if (source.getEntity() instanceof PlayerEntity attackingPlayer) {

            if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.SOUL_SIPHON).mcdw$getIsEnabled())
                EnchantmentEffects.applySoulSiphon(attackingPlayer, isOffHandAttack);
        }
    }

    @Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
    public void mcdw$applySmitingEnchantmentDamage(DamageSource source, float amount, CallbackInfo info) {
        if(!(source.getEntity() instanceof LivingEntity user))
            return;

        LivingEntity target = (LivingEntity) (Object) this;

        if(target instanceof PlayerEntity) return;

        if (source.getDirectEntity() instanceof LivingEntity) {
            if (amount > 0) {
                ItemStack mainHandStack = user.getMainHandItem();
                ItemStack offHandStack = user.getOffhandItem();

                if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.SMITING).mcdw$getIsEnabled()) {
                    mcdw$applySmite(amount, user, target, mainHandStack);
                    mcdw$applySmite(amount, user, target, offHandStack);
                }
            }
        }
    }

    @Unique
    private void mcdw$applySmite(float amount, LivingEntity user, LivingEntity target, ItemStack itemStack) {
        if (itemStack != null && (EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.SMITING), itemStack) > 0
                && !(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SMITE, itemStack) > 0))) {
            int smitingLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.SMITING), itemStack);
            if (target.isInvertedHealAndHarm()) {
                EnchantmentEffects.causeSmitingAttack(user, target,
                        3.0f * smitingLevel, amount);
            }
        }
    }

    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void mcdw$onAttack(DamageSource source, float amount, CallbackInfo ci) {
        var attacker = source.getEntity();
        var target = (LivingEntity) ((Object)this);
        if (target.isInvulnerableTo(source)) {
            return;
        }

        if(!(attacker instanceof PlayerEntity attackingPlayer))
            return;

        if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.BUSY_BEE).mcdw$getIsEnabled()
                && ((IBeeSummoning)attackingPlayer).isReadyForBeeSummon(attackingPlayer.tickCount)) {
            ItemStack mainHandStack = attackingPlayer.getMainHandItem();
            ItemStack offHandStack = attackingPlayer.getOffhandItem();
            if (mainHandStack.getItem() == ItemsRegistry.SWORD_ITEMS.get(SwordsID.SWORD_BEESTINGER) && offHandStack.getItem() == ItemsRegistry.MCDW_ITEMS.get(ItemsID.ITEM_BEE_STINGER)) {
                offHandStack.shrink(1);
                SummonedBeeEntity summonedBeeEntity_1 = mcdw$summoned_bee.create(attackingPlayer.getWorld());
                if (summonedBeeEntity_1 != null) {
                    summonedBeeEntity_1.setSummoner(attackingPlayer);
                    summonedBeeEntity_1.moveTo(attackingPlayer.getX(), attackingPlayer.getY() + 1, attackingPlayer.getZ(), 0, 0);
                    attackingPlayer.getWorld().spawnEntity(summonedBeeEntity_1);
                }
            }
        }
    }

    @Inject(method = "consumeItem", at = @At("HEAD"))
    public void mcdw$applyDippingPoisonPotionConsumption(CallbackInfo ci) {
        if(!((Object) this instanceof PlayerEntity user))
            return;

        ItemStack poisonTippedArrow = PotionUtil.setPotion(new ItemStack(Items.TIPPED_ARROW, 8), Potions.POISON);

        if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.DIPPING_POISON).mcdw$getIsEnabled()) {
            if (!(user.getMainHandItem().getItem() instanceof PotionItem))
                return;

            if (user.getOffhandItem() != null
                    && (EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.DIPPING_POISON), user.getOffhandItem()) > 0)
            ) {
                int dippingPoisonLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.DIPPING_POISON), user.getOffhandItem());
                if (dippingPoisonLevel > 0) {
                    List<StatusEffectInstance> potionEffects = PotionUtil.getMobEffects(user.getMainHandItem());
                    if (!(potionEffects.get(0).getEffect() == StatusEffects.HEAL)) {
                        return;
                    }
                    if (potionEffects.get(0).getEffect() == StatusEffects.HEAL) {
                        CleanlinessHelper.mcdw$dropItem(user, poisonTippedArrow);
                    }
                }

            }
        }
    }

    @Inject(method = "jump", at = @At("HEAD"))
    public void mcdw$onJumpEffects(CallbackInfo ci){
        if (!((Object) this instanceof ServerPlayerEntity playerEntity))
            return;

        if (playerEntity != null) {
            if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.BURST_BOWSTRING).mcdw$getIsEnabled())
                EnchantmentEffects.activateBurstBowstringOnJump(playerEntity);
            if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.DYNAMO).mcdw$getIsEnabled())
                EnchantmentEffects.handleAddDynamoEffect(playerEntity);
        }
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V"))
    public void mcdw$applySharedPainDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (source.getDirectEntity() instanceof PlayerEntity player) {
            int sharedPainLevel = EnchantmentEffects.mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.SHARED_PAIN), PlayerEntity, false);
            if (sharedPainLevel <= 0) return;
            if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.SHARED_PAIN).mcdw$getIsEnabled()) {
                if ((Object) this instanceof LivingEntity target) {
                    float targetHealth = target.getHealth() - amount;
                    if (targetHealth < 0) {
                        float overkillDamage = Math.abs(targetHealth);
                        List<LivingEntity> nearbyEntities = AOEHelper.getEntitiesByConfig(target, 6);
                        if (nearbyEntities.isEmpty()) {
                            if (Mcdw.CONFIG.mcdwEnchantmentSettingsConfig.ENABLE_ENCHANTMENT_SETTINGS.get(SettingsID.SHARED_PAIN_CAN_DAMAGE_USER)) {
                                PlayerEntity.hurt(PlayerEntity.getWorld().damageSources().magic(), overkillDamage);
                            }
                        } else {
                            nearbyEntities.sort(Comparator.comparingDouble(livingEntity -> livingEntity.distanceToSqr(target)));
                            nearbyEntities.get(0).hurt(nearbyEntities.get(0).getWorld().damageSources().magic(), overkillDamage);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "createLivingAttributes", require = 1, allow = 1, at = @At("RETURN"))
    private static void mcdw$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue().add(EntityAttributesRegistry.REACH).add(EntityAttributesRegistry.ATTACK_RANGE);
    }
}
