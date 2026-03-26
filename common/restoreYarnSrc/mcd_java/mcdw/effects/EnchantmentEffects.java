/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.effects;

import mcd_java.mcdw.api.interfaces.IMcdwEnchantedArrow;
import mcd_java.mcdw.api.util.*;
import mcd_java.mcdw.enchants.goals.WildRageAttackGoal;
import mcd_java.mcdw.enums.BowsID;
import mcd_java.mcdw.enums.EnchantmentsID;
import mcd_java.mcdw.mixin.mcdw.CreeperEntityAccessor;
import mcd_java.mcdw.mixin.mcdw.MobEntityAccessor;
import mcd_java.mcdw.registries.EnchantsRegistry;
import mcd_java.mcdw.registries.SoundEventsRegistry;
import mcd_java.mcdw.registries.StatusEffectsRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

import static mcd_java.mcdw.Mcdw.CONFIG;

@SuppressWarnings("DataFlowIssue")
public class EnchantmentEffects {

    public static int mcdw$getEnchantmentLevel(Enchantment enchantment, LivingEntity enchantedEntity, boolean isOffHandStack) {
        if (FabricLoader.getInstance().isModLoaded("bettercombat")) {
            // Better Combat can figure out if the hit was done by offhand
            return EnchantmentHelper.getEnchantmentLevel(enchantment, enchantedEntity);
        } else {
            // We know if the hit was done by offhand
            return EnchantmentHelper.getItemEnchantmentLevel(enchantment, isOffHandStack ? enchantedEntity.getOffhandItem() : enchantedEntity.getMainHandItem());
        }
    }

    /* ExperienceOrbEntityMixin */
    //mcdw$ModifyExperience
    public static int soulDevourerExperience(Player playerEntity, int amount) {
        int mainHandLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.SOUL_DEVOURER), playerEntity.getMainHandItem());
        int offHandLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.SOUL_DEVOURER), playerEntity.getOffhandItem());

        int soulDevourerLevel = mainHandLevel + offHandLevel;

        if (soulDevourerLevel > 0)
            return Math.round((float) amount * (1 + ((float) soulDevourerLevel /
                    CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                            .get(EnchantmentsID.SOUL_DEVOURER).mcdw$getOffset())));
        return amount;
    }

    public static int animaConduitExperience(Player playerEntity, int amount, boolean isOffHandStack) {
        int animaLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.ANIMA_CONDUIT), playerEntity, isOffHandStack);

        if (animaLevel > 0) {
            float missingHealth = playerEntity.getMaxHealth() - playerEntity.getHealth();
            if (missingHealth > 0) {
                float i = Math.min(AbilityHelper.getAnimaRepairAmount(amount, animaLevel), missingHealth);
                playerEntity.heal(i *
                        (CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                                        .get(EnchantmentsID.ANIMA_CONDUIT).mcdw$getOffset())/100f);
                amount -= (int) (i * 5);
                return Math.max(amount, 0);
            }
        }
        return amount;
    }

    /* LivingEntityMixin */
    //mcdw$damageModifiers
    public static float huntersPromiseDamage(Player owner, ServerLevel serverWorld) {
        if (CONFIG.mcdwNewStatsConfig.bowStats.get(BowsID.BOW_HUNTERS_PROMISE).isEnabled) {
            if (owner.getMainHandItem().is(BowsID.BOW_HUNTERS_PROMISE.getItem())) {
                UUID petOwnerUUID = owner.getUUID();

                if (petOwnerUUID != null) {
                    if (serverWorld.getEntity(petOwnerUUID) instanceof LivingEntity) {
                        return 0.5F;
                    }
                }
            }
        }
        return 0f;
    }

    //mcdw$onDeath
    public static void applyProspector(LivingEntity prospectingEntity, LivingEntity victim, boolean isOffHandStack) {
        int prospectorLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.PROSPECTOR), prospectingEntity, isOffHandStack);

        if (prospectorLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.PROSPECTOR).mcdw$getProcChance() * prospectorLevel)) {
                if (victim instanceof Enemy){
                    CleanlinessHelper.mcdw$dropItem(victim, Items.EMERALD);
                }
            }
        }
    }

    public static void applyRushdown(LivingEntity rushingEntity, boolean isOffHandStack) {
        int rushdownLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.RUSHDOWN), rushingEntity, isOffHandStack);

        if (rushdownLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.RUSHDOWN).mcdw$getProcChance())) {
                MobEffectInstance rushdown = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100 * rushdownLevel, 2,
                        false, false);
                rushingEntity.addEffect(rushdown);
            }
        }
    }

    public static void applySoulSiphon(Player siphoningEntity, boolean isOffHandStack) {
        int soulLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.SOUL_SIPHON), siphoningEntity, isOffHandStack);

        if (soulLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.SOUL_SIPHON).mcdw$getProcChance())) {
                siphoningEntity.giveExperiencePoints(3 * soulLevel);
            }
        }
    }

    public static void applyShadowShotShadowForm(LivingEntity shadowShotEntity, AbstractArrow ppe, int duration){
        int shadowShotLevel = ((IMcdwEnchantedArrow)ppe).mcdw$getShadowShotLevel();
        if (shadowShotLevel > 0) {
            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.SHADOW_SHOT).mcdw$getProcChance())) {
                shadowShotEntity.addEffect(new MobEffectInstance(StatusEffectsRegistry.SHADOW_FORM, duration, 0, false, true, true));
            }
        }
    }

    /* LivingEntityPlayerEntityMixin */
    //mcdw$damageModifiers
    public static float ambushDamage(LivingEntity ambushingEntity, LivingEntity ambushee, boolean isOffHandStack) {
        int ambushLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.AMBUSH), ambushingEntity, isOffHandStack);
        if (ambushLevel > 0) {

            if (ambushingEntity.isInvisible() && ambushingEntity.isShiftKeyDown()) {

                CleanlinessHelper.playCenteredSound(ambushee, SoundEvents.POINTED_DRIPSTONE_LAND, 0.5F, 1.0F);
                return 0.15f * ambushLevel;
            }
        }
        return 0f;
    }

    public static float criticalHitDamage(LivingEntity crittingEntity, LivingEntity target, boolean isOffHandStack) {
        int criticalHitLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.CRITICAL_HIT), crittingEntity, isOffHandStack);

        if (criticalHitLevel > 0) {

            if (CleanlinessHelper.percentToOccur(10 + (CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.CRITICAL_HIT).mcdw$getProcChance() * criticalHitLevel))) {
                if (!AbilityHelper.entityCanCrit(crittingEntity)) {

                    CleanlinessHelper.playCenteredSound(target, SoundEvents.PLAYER_ATTACK_CRIT, 0.5F, 1.0F);
                    return 0.5f;
                }
            }
        }
        return 0f;
    }

    public static float voidStrikeDamage(LivingEntity voidEntity, LivingEntity target, boolean isOffHandStack) {
        int voidlevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.VOID_STRIKE), voidEntity, isOffHandStack);

        if (voidlevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.VOID_STRIKE).mcdw$getProcChance() + (5 * voidlevel))) {
                CleanlinessHelper.playCenteredSound(target, SoundEvents.ENDERMAN_TELEPORT, 0.5F, 1.0F);
                return (2f * voidlevel) - 1f; // -1f accounts for change to storedAmount calc
            }
        }
        return 0f;
    }

    public static float painCycleDamage(LivingEntity painEntity, boolean isOffHandStack) {
        int painCycleLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.PAIN_CYCLE), painEntity, isOffHandStack);

        if (painCycleLevel > 0) {
            MobEffectInstance painCycleInstance = painEntity.getEffect(StatusEffectsRegistry.PAIN_CYCLE);
            int i = painCycleInstance != null ? painCycleInstance.getAmplifier() + 1 : 0;
            if (i < 5) {
                MobEffectInstance painCycleUpdate = new MobEffectInstance(StatusEffectsRegistry.PAIN_CYCLE, 120000, i, false, false, true);
                painEntity.removeEffect(StatusEffectsRegistry.PAIN_CYCLE);
                painEntity.addEffect(painCycleUpdate);
                painEntity.hurt(painEntity.level().damageSources().magic(), 1);
            } else {
                painEntity.removeEffect(StatusEffectsRegistry.PAIN_CYCLE);
                return painCycleLevel + 1;
            }
        }
        return 0;
    }

    public static float enigmaResonatorDamage(Player resonatingEntity, LivingEntity target, boolean isOffHandStack) {
        int resonatorLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.ENIGMA_RESONATOR), resonatingEntity, isOffHandStack);
        return calcEnigmaResonatorDamage(resonatingEntity, target, resonatorLevel);
    }

    public static float enigmaShotDamage(LivingEntity resonatingEntity, LivingEntity target, AbstractArrow ppe) {
        if (!(resonatingEntity instanceof Player player))
            return 0f;
        IMcdwEnchantedArrow enchantedArrow = (IMcdwEnchantedArrow) ppe;
        int resonatorLevel = enchantedArrow.mcdw$getEnigmaResonatorLevel();
        return calcEnigmaResonatorDamage(player, target, resonatorLevel);
    }

    private static float calcEnigmaResonatorDamage(Player resonatingEntity, LivingEntity target, int resonatorLevel) {
        if (resonatorLevel > 0) {
            int numSouls = resonatingEntity.experienceLevel;
            if (numSouls > 0) {

                CleanlinessHelper.playCenteredSound(target, SoundEvents.SOUL_ESCAPE, 0.5F, 1.0F);
                float extraDamageMultiplier =
                        (float) (Math.log(numSouls * resonatorLevel + 20)) /
                                CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                                        .get(EnchantmentsID.ENIGMA_RESONATOR).mcdw$getOffset();

                return Math.max(extraDamageMultiplier - 1, 0f);
            }
        }
        return 0f;
    }



    public static float growingDamage(LivingEntity growingEntity, LivingEntity target, AbstractArrow ppe) {
        int growingLevel = ((IMcdwEnchantedArrow)ppe).mcdw$getGrowingLevel();
        if (growingLevel > 0) {

            CleanlinessHelper.playCenteredSound(target, SoundEvents.ENDERMAN_TELEPORT, 0.5F, 1.0F);
            float damageModifier = 0.03F * growingEntity.distanceTo(target) * growingLevel;
            return Mth.clamp(damageModifier, 0f, growingLevel);
        }
        return 0f;
    }

    public static float voidShotDamage(LivingEntity target, AbstractArrow ppe) {
        int voidlevel = ((IMcdwEnchantedArrow)ppe).mcdw$getVoidShotLevel();
        if (voidlevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.VOID_SHOT).mcdw$getProcChance() + (5 * voidlevel))) {
                CleanlinessHelper.playCenteredSound(target, SoundEvents.ENDERMAN_TELEPORT, 0.5F, 1.0F);
                return voidlevel;
            }
        }
        return 0f;
    }

    public static float committedDamage(LivingEntity committedEntity, LivingEntity target, boolean isOffHandStack) {
        int committedLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.COMMITTED), committedEntity, isOffHandStack);

        if (committedLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                    .get(EnchantmentsID.COMMITTED).mcdw$getProcChance())) {

                CleanlinessHelper.playCenteredSound(target, SoundEvents.GENERIC_EXPLODE, 0.5F, 1.0F);

                float getTargetRemainingHealth = Mth.clamp(target.getHealth() / target.getMaxHealth(), 0, 1);
                float attributeDamage = (float) committedEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float committedMultiplier = 0.2F * committedLevel;

                float getExtraDamage = attributeDamage * (1 - getTargetRemainingHealth) * committedMultiplier;
                return Math.max(getExtraDamage, 0f);
            }
        }
        return 0f;
    }

    public static float dynamoDamage (LivingEntity dynamoEntity, boolean isOffHandStack) {
        int dynamoLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.DYNAMO), dynamoEntity, isOffHandStack);
        return calcDynamoDamage(dynamoEntity, dynamoLevel);
    }

    public static float dynamoShotDamage (LivingEntity dynamoEntity, AbstractArrow ppe) {
        int dynamoLevel = ((IMcdwEnchantedArrow)ppe).mcdw$getDynamoLevel();
        return calcDynamoDamage(dynamoEntity, dynamoLevel);
    }

    private static float calcDynamoDamage(LivingEntity dynamoEntity, int dynamoLevel) {
        if (dynamoLevel > 0 && dynamoEntity.hasEffect(StatusEffectsRegistry.DYNAMO)) {
            MobEffectInstance dynamoInstance = dynamoEntity.getEffect(StatusEffectsRegistry.DYNAMO);
            if (dynamoInstance != null) {
                int dynamoAmplifier = dynamoInstance.getAmplifier() + 1;
                float dynamoLevelModifier = (dynamoLevel - 1) * 0.25f + 1;
                float getDynamoDamage = (float) (dynamoLevelModifier * (dynamoAmplifier * 0.1));
                dynamoEntity.removeEffect(StatusEffectsRegistry.DYNAMO);

                return Math.max(getDynamoDamage, 0f);
            }
        }
        return 0f;
    }

    public static float shadowFormDamage (LivingEntity shadowShotEntity) {
        if (shadowShotEntity.hasEffect(StatusEffectsRegistry.SHADOW_FORM)) {
            shadowShotEntity.removeEffect(StatusEffectsRegistry.SHADOW_FORM);
            return 7f;
        }
        return 0f;
    }

    public static float shadowFormShotDamage (LivingEntity shadowShotEntity, AbstractArrow ppe) {
        boolean shadowBarbBoolean = ((IMcdwEnchantedArrow)ppe).mcdw$getShadowBarbBoolean();
        if (shadowShotEntity.hasEffect(StatusEffectsRegistry.SHADOW_FORM)) {
            if (!shadowBarbBoolean) {
                shadowShotEntity.removeEffect(StatusEffectsRegistry.SHADOW_FORM);
                //TODO TRY TO FIGURE OUT HOW TO REMOVE INVISIBILITY
            }
            return 7f;
        }
        return 0f;
    }

    public static float overchargeDamage(AbstractArrow ppe) {
        int overchargeAmount = ((IMcdwEnchantedArrow)ppe).mcdw$getOvercharge();
        return Math.max(overchargeAmount, 0);
    }

    //mcdw$onApplyDamageHead
    public static void applyFreezing(LivingEntity freezerEntity, LivingEntity target, boolean isOffHandStack) {
        int freezingLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.FREEZING), freezerEntity, isOffHandStack);
        if (freezingLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.FREEZING).mcdw$getProcChance() + (10 * freezingLevel))) {
                AbilityHelper.causeFreezing(target, 100);
            }
        }
    }

    public static void applyPoisoning(LivingEntity poisoningEntity, LivingEntity target, boolean isOffHandStack) {
        int poisoningLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.JUNGLE_POISON), poisoningEntity, isOffHandStack);
        if (poisoningLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.JUNGLE_POISON).mcdw$getProcChance())) {
                MobEffectInstance poison = new MobEffectInstance(MobEffects.POISON, 60, poisoningLevel - 1);
                target.addEffect(poison);
            }
        }
    }

    public static void applyPoisonCloud(LivingEntity poisoningEntity, LivingEntity target, boolean isOffHandStack) {
        int poisoningLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.POISON_CLOUD), poisoningEntity, isOffHandStack);
        if (poisoningLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.POISON_CLOUD).mcdw$getProcChance())) {
                AOECloudHelper.spawnAreaEffectCloudEntityWithAttributes(
                        poisoningEntity,
                        target,
                        5.0f,
                        10,
                        60,
                        MobEffects.POISON,
                        60,
                        poisoningLevel - 1,
                        true,
                        true,
                        true,
                        false
                );
            }
        }
    }

    public static void applyRadianceCloud(LivingEntity radiantEntity, boolean isOffHandStack) {
        int radianceLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.RADIANCE), radiantEntity, isOffHandStack);

        if (radianceLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                    .get(EnchantmentsID.RADIANCE).mcdw$getProcChance())) {
                AOECloudHelper.spawnAreaEffectCloudEntityWithAttributes(
                        radiantEntity,
                        radiantEntity,
                        5.0f,
                        10,
                        60,
                        MobEffects.REGENERATION,
                        100,
                        radianceLevel - 1,
                        true,
                        false,
                        false,
                        true
                );
            }
        }
    }

    public static void applyShockwave(LivingEntity shockwaveEntity, LivingEntity target, float amount, boolean isOffHandStack) {
        int shockwaveLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.SHOCKWAVE), shockwaveEntity, isOffHandStack);

        if (shockwaveLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                    .get(EnchantmentsID.SHOCKWAVE).mcdw$getProcChance() + (15 * shockwaveLevel))) {
                causeShockwaveAttack(shockwaveEntity, target,
                        3.0f, amount);

                CleanlinessHelper.playCenteredSound(target,
                        SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER,
                        0.5f, 1.0f);
            }
        }
    }

    public static void causeShockwaveAttack(LivingEntity user, LivingEntity target, float distance, float amount) {
        AOEHelper.getEntitiesByConfig(user, distance).stream()
                .filter(nearbyEntity -> nearbyEntity != target)
                .forEach(nearbyEntity -> nearbyEntity.hurt(
                        nearbyEntity.level().damageSources().generic(),
                        amount * 0.25f)
                );
    }

    public static void causeSmitingAttack(LivingEntity user, LivingEntity target, float distance, float amount) {
        AOEHelper.getEntitiesByConfig(user, distance).stream()
                .filter(nearbyEntity -> nearbyEntity != target && nearbyEntity.isInvertedHealAndHarm())
                .forEach(nearbyEntity -> nearbyEntity.hurt(nearbyEntity.level().damageSources().magic(), amount * 1.25F));
    }

    public static void applyStunning(LivingEntity stunningEntity, LivingEntity target, boolean isOffHandStack) {
        int stunningLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.STUNNING), stunningEntity, isOffHandStack);
        if (stunningLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                    .get(EnchantmentsID.STUNNING).mcdw$getProcChance() + (15 * stunningLevel))) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 10));
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 1));
            }
        }
    }

    public static void applyThundering(LivingEntity thunderingEntity, float amount, boolean isOffHandStack) {
        int thunderingLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.THUNDERING), thunderingEntity, isOffHandStack);

        if (thunderingLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                    .get(EnchantmentsID.THUNDERING).mcdw$getProcChance())) {
                electrocuteNearbyEnemies(thunderingEntity,
                        5 * thunderingLevel, amount,
                        Integer.MAX_VALUE);
            }
        }
    }

    public static void createVisualLightningBoltOnEntity(Entity target) {
        EntityDataAccessor<Boolean> charged = CreeperEntityAccessor.getCHARGED();
        Level world = target.getCommandSenderWorld();
        LightningBolt lightningEntity = EntityType.LIGHTNING_BOLT.create(world);

        if (lightningEntity != null) {
            lightningEntity.moveTo(target.getX(), target.getY(), target.getZ());
            lightningEntity.setVisualOnly(true);
            if (target instanceof Creeper creeperEntity) {
                creeperEntity.getEntityData().set(charged, true);
            }
            world.addFreshEntity(lightningEntity);
        }
    }

    public static void electrocute(LivingEntity victim, float damageAmount) {
        createVisualLightningBoltOnEntity(victim);
        victim.hurt(victim.level().damageSources().lightningBolt(), damageAmount);
    }

    public static void electrocuteNearbyEnemies(LivingEntity user, float distance, float damageAmount, int limit) {
        boolean foundTarget = false;
        for (LivingEntity nearbyEntity : AOEHelper.getEntitiesByConfig(user, distance)) {
            electrocute(nearbyEntity, damageAmount);
            foundTarget = true;
            limit--;
            if (limit <= 0) break;
        }
        if (foundTarget) {
            CleanlinessHelper.playCenteredSound(user, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1f, 1f);
            CleanlinessHelper.playCenteredSound(user, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 1f, 1f);
        }
    }

    public static void applyWeakeningCloud(LivingEntity weakeningEntity, LivingEntity target, boolean isOffHandStack) {
        int weakeningLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.WEAKENING), weakeningEntity, isOffHandStack);

        if (weakeningLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                    .get(EnchantmentsID.WEAKENING).mcdw$getProcChance())) {
                AOECloudHelper.spawnAreaEffectCloudEntityWithAttributes(
                        weakeningEntity,
                        target,
                        5.0f,
                        10,
                        60,
                        MobEffects.WEAKNESS,
                        100,
                        weakeningLevel - 1,
                        true,
                        true,
                        true,
                        false
                );
            }
        }
    }

    public static void applySwirling(LivingEntity swirlingEntity, LivingEntity target, float amount, boolean isOffHandStack) {
        int swirlingLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.SWIRLING), swirlingEntity, isOffHandStack);

        if (swirlingLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                    .get(EnchantmentsID.SWIRLING).mcdw$getProcChance() + (15 * swirlingLevel))) {
                causeSwirlingAttack(swirlingEntity, swirlingEntity,
                        1.5f, amount);

                CleanlinessHelper.playCenteredSound(target, SoundEvents.PLAYER_ATTACK_SWEEP, 0.5F, 1.0F);
            }
        }
    }

    public static void causeSwirlingAttack(LivingEntity user, LivingEntity target, float distance, float amount) {
        AOEHelper.getEntitiesByConfig(target, user, distance)
                .forEach(nearbyEntity -> nearbyEntity.hurt(nearbyEntity.level().damageSources().generic(), amount * 0.5F));
    }

    public static void applyChains(LivingEntity chainingEntity, LivingEntity target, boolean isOffHandStack) {
        int chainsLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.CHAINS), chainingEntity, isOffHandStack);

        if (chainsLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                    .get(EnchantmentsID.CHAINS).mcdw$getProcChance()))
                chainNearbyEntities(chainingEntity, target, 1.5F * chainsLevel, chainsLevel);
        }
    }

    public static void chainNearbyEntities(LivingEntity user, LivingEntity target, float distance, int timeMultiplier) {
        MobEffectInstance chained = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100 * timeMultiplier, 100);

        target.addEffect(chained);

        AOEHelper.getEntitiesByConfig(user, distance).stream()
                .filter(nearbyEntity -> nearbyEntity != target)
                .forEach(nearbyEntity -> {
                    pullTowards(nearbyEntity, target);
                    nearbyEntity.addEffect(chained);
                });
    }

    public static void applyGravity(LivingEntity gravityEntity, LivingEntity target, boolean isOffHandStack) {
        int gravityLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.GRAVITY), gravityEntity, isOffHandStack);

        if (gravityLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                    .get(EnchantmentsID.GRAVITY).mcdw$getProcChance())) {
                pullInNearbyEntities(gravityEntity, target,
                        (gravityLevel + 1) * 3);
            }
        }
    }

    public static void pullTowards(Entity self, Entity target) {
        if (self instanceof Player && ((Player) self).getAbilities().instabuild) return;

        double motionX = Mth.clamp((target.getX() - self.getX()) * 0.15f, -5, 5);
        double motionZ = Mth.clamp((target.getZ() - self.getZ()) * 0.15f, -5, 5);
        Vec3 vec3d = new Vec3(motionX, 0, motionZ);

        self.setDeltaMovement(vec3d);
    }

    public static void pullInNearbyEntities(LivingEntity user, LivingEntity target, float distance) {
        AOEHelper.getEntitiesByConfig(user, distance).stream()
                .filter(nearbyEntity -> nearbyEntity != target)
                .forEach(nearbyEntity -> pullTowards(nearbyEntity, target));
    }



    //mcdw$onApplyDamageTail
    public static void echoDamage(LivingEntity echoEntity, LivingEntity target, float amount, boolean isOffHandStack) {
        int echoLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.ECHO), echoEntity, isOffHandStack);

        if (echoLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                    .get(EnchantmentsID.ECHO).mcdw$getProcChance() + (15 * echoLevel))) {
                causeEchoAttack(echoEntity, target,
                        3.0f,
                        echoLevel, amount);
                CleanlinessHelper.playCenteredSound(echoEntity, SoundEventsRegistry.ECHO_SOUND_EVENT, 0.5F, 1.0F);
            }
        }
    }

    public static void causeEchoAttack(LivingEntity user, LivingEntity target, float distance, int echoLevel, float amount) {
        for (LivingEntity nearbyEntity : AOEHelper.getEntitiesByConfig(user, distance)) {
            if (nearbyEntity != target) {
                nearbyEntity.hurt(nearbyEntity.level().damageSources().generic(), amount);

                echoLevel--;
                if (echoLevel <= 0) break;
            }
        }
    }

    //mcdw$onDeath
    public static void explodingDamage(LivingEntity exploderEntity, LivingEntity target, boolean isOffHandStack) {
        int explodingLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.EXPLODING), exploderEntity, isOffHandStack);
        if (explodingLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                    .get(EnchantmentsID.EXPLODING).mcdw$getProcChance())) {

                CleanlinessHelper.playCenteredSound(target, SoundEvents.GENERIC_EXPLODE, 0.5F, 1.0F);
                AOECloudHelper.spawnExplosionCloud(exploderEntity, target, 3.0F);

                float explodingDamage = target.getMaxHealth() * 0.2f * explodingLevel;
                causeExplosionAttack(exploderEntity, target, explodingDamage, 3.0F);
            }
        }
    }

    public static void causeExplosionAttack(LivingEntity user, LivingEntity target, float damageAmount, float distance) {
        AOEHelper.getEntitiesByConfig(target, user, distance)
                .forEach(nearbyEntity -> nearbyEntity.hurt(nearbyEntity.level().damageSources().explosion(target, user), damageAmount));
    }

    public static void applyRampaging(LivingEntity rampagingEntity, boolean isOffHandStack) {
        int rampagingLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.RAMPAGING), rampagingEntity, isOffHandStack);
        if (rampagingLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                    .get(EnchantmentsID.RAMPAGING).mcdw$getProcChance())) {
                MobEffectInstance rampage = new MobEffectInstance(MobEffects.DIG_SPEED, rampagingLevel * 100, 2,
                        false, false);
                rampagingEntity.addEffect(rampage);
            }
        }
    }

    public static void applyGuardingStrike(LivingEntity guardingEntity, boolean isOffHandStack) {
        int guardingLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.GUARDING_STRIKE), guardingEntity, isOffHandStack);
        if (guardingLevel > 0) {

            MobEffectInstance shield = new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 + (20 * guardingLevel), 2);
            guardingEntity.addEffect(shield);
        }
    }

    public static void applyLeeching(LivingEntity leechingEntity, LivingEntity target, boolean isOffHandStack) {
        int leechingLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.LEECHING), leechingEntity, isOffHandStack);

        if (leechingLevel > 0) {
            if (leechingEntity.getHealth() < leechingEntity.getMaxHealth()) {
                float healthRegained = (0.2F + 0.2F * leechingLevel) * target.getMaxHealth();
                leechingEntity.heal(healthRegained *
                        (CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                                        .get(EnchantmentsID.LEECHING).mcdw$getOffset())/100f);
            }
        }
    }

    public static void applyRefreshment(Player refreshingEntity, boolean isOffHandStack){
        int refreshmentLevel = mcdw$getEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.REFRESHMENT), refreshingEntity, isOffHandStack);

        if (refreshmentLevel > 0) {
            InventoryHelper.mcdw$systematicReplacePotions(refreshingEntity, Items.GLASS_BOTTLE, Potions.HEALING, refreshmentLevel);
        }
    }

    /* PersistentProjectileEntityMixin */
    // mcdw$onEntityHitTail
    public static void applyChainReaction(LivingEntity shooter, LivingEntity target, AbstractArrow ppe) {
        int chainReactionLevel = ((IMcdwEnchantedArrow) ppe).mcdw$getChainReactionLevel();
        if (chainReactionLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.CHAIN_REACTION).mcdw$getProcChance() * chainReactionLevel)){
                ProjectileEffectHelper.mcdw$fireChainReactionProjectileFromTarget(target.getCommandSenderWorld(), target, shooter,
                        3.15F,1.0F);
            }
        }
    }

    public static void applyCharge(LivingEntity chargingEntity, AbstractArrow ppe) {
        int chargeLevel = ((IMcdwEnchantedArrow) ppe).mcdw$getChargeLevel();
        if (chargeLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.CHARGE).mcdw$getProcChance()))
                chargingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, chargeLevel * 20,
                        4));
        }
    }

    public static void applyCobwebShotEntity(LivingEntity target, AbstractArrow ppe) {
        if (((IMcdwEnchantedArrow)ppe).mcdw$getCobwebShotLevel() > 0) {
            Level targetWorld = target.getCommandSenderWorld();
            BlockPos targetPos = target.blockPosition();

            if (targetWorld.getBlockState(targetPos) == Blocks.AIR.defaultBlockState())
                targetWorld.setBlockAndUpdate(targetPos, Blocks.COBWEB.defaultBlockState());
        }
    }

    public static void applyFuseShot(LivingEntity shooter, LivingEntity target, AbstractArrow ppe) {
        int fuseShotLevel = ((IMcdwEnchantedArrow) ppe).mcdw$getFuseShotLevel();
        if (fuseShotLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.FUSE_SHOT).mcdw$getProcChance() + (15 * fuseShotLevel))) {
                CleanlinessHelper.playCenteredSound(target, SoundEvents.GENERIC_EXPLODE, 0.5F, 1.0F);

                AOECloudHelper.spawnExplosionCloud(shooter, target, 3.0F);
                float f = (float) ppe.getDeltaMovement().length();
                int fuseShotDamage = Mth.ceil(Mth.clamp((double) f * ppe.getBaseDamage(), 0.0D, 2.147483647E9D));
                causeExplosionAttack(shooter, target, fuseShotDamage, 3.0F);
            }
        }
    }

    public static void applyFreezingShot(LivingEntity target, AbstractArrow ppe) {
        int freezingLevel = ((IMcdwEnchantedArrow) ppe).mcdw$getFreezingLevel();
        if (freezingLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.FREEZING).mcdw$getProcChance() + (10 * freezingLevel))) {
                AbilityHelper.causeFreezing(target, 100);
            }
        }
    }

    public static void applyGravityShot(LivingEntity shooter, LivingEntity target, AbstractArrow ppe) {
        int gravityLevel = ((IMcdwEnchantedArrow) ppe).mcdw$getGravityLevel();
        if (gravityLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.GRAVITY).mcdw$getProcChance())) {
                pullInNearbyEntities(shooter, target, (gravityLevel + 1) * 3);
            }
        }
    }

    public static void applyLevitationShot(LivingEntity target, AbstractArrow ppe) {
        int levitationShotLevel = ((IMcdwEnchantedArrow) ppe).mcdw$getLevitationShotLevel();
        if (levitationShotLevel > 0) {

            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200 * levitationShotLevel));
        }
    }

    public static void applyPhantomsMark(LivingEntity target, AbstractArrow ppe) {
        int phantomsMarkLevel = ((IMcdwEnchantedArrow) ppe).mcdw$getPhantomsMarkLevel();
        if (phantomsMarkLevel > 0) {

            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100 * phantomsMarkLevel));
        }
    }

    public static void applyPoisonCloudShot(LivingEntity shooter, LivingEntity target, AbstractArrow ppe) {
        int poisonCloudLevel = ((IMcdwEnchantedArrow) ppe).mcdw$getPoisonCloudLevel();
        if (poisonCloudLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.POISON_CLOUD).mcdw$getProcChance())) {
                AOECloudHelper.spawnAreaEffectCloudEntityWithAttributes(
                        shooter,
                        target,
                        5.0f,
                        10,
                        60,
                        MobEffects.POISON,
                        60,
                        poisonCloudLevel - 1,
                        true,
                        true,
                        false,
                        false
                );
            }
        }
    }

    public static void applyRadianceShot(LivingEntity shooter, LivingEntity target, AbstractArrow ppe) {
        int radianceLevel = ((IMcdwEnchantedArrow) ppe).mcdw$getRadianceLevel();
        if (radianceLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.RADIANCE).mcdw$getProcChance()))
                AOECloudHelper.spawnRegenCloudAtPos(shooter, true, target.blockPosition(), radianceLevel - 1);
        }
    }

    public static void applyRicochet(LivingEntity shooter, LivingEntity target, AbstractArrow ppe) {
        int ricochetLevel = ((IMcdwEnchantedArrow) ppe).mcdw$getRicochetLevel();
        if (ricochetLevel > 0) {

            float damageMultiplier = 0.03F + (ricochetLevel * 0.07F);
            if (ppe.getDeltaMovement().length() > 0.7F)
                ProjectileEffectHelper.mcdw$spawnExtraArrows(shooter, target, 1, 10, damageMultiplier);
        }
    }

    public static void applyReplenish(Player shooter, AbstractArrow ppe) {
        int replenishLevel = ((IMcdwEnchantedArrow) ppe).mcdw$getReplenishLevel();
        if (replenishLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.REPLENISH).mcdw$getProcChance() + (7 * replenishLevel))) {
                CleanlinessHelper.mcdw$dropItem(shooter, Items.ARROW);
            }
        }
    }

    public static void applyTempoTheft(LivingEntity tempoEntity, LivingEntity target, AbstractArrow ppe) {
        int tempoTheftLevel = ((IMcdwEnchantedArrow) ppe).mcdw$getTempoTheftLevel();
        if (tempoTheftLevel > 0) {

            AbilityHelper.stealSpeedFromTarget(tempoEntity, target, tempoTheftLevel);
        }
    }

    public static void applyThunderingShot(LivingEntity shooter, AbstractArrow ppe){
        int thunderingLevel = ((IMcdwEnchantedArrow)ppe).mcdw$getThunderingLevel();

        if (thunderingLevel > 0) {

            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.THUNDERING).mcdw$getProcChance())) {
                electrocuteNearbyEnemies(shooter,
                        5 * thunderingLevel, 5,
                        Integer.MAX_VALUE);
            }
        }
    }

    public static void applyWildRage(Mob ragingEntity, AbstractArrow ppe) {
        int wildRageLevel = ((IMcdwEnchantedArrow)ppe).mcdw$getWildRageLevel();
        if (wildRageLevel > 0) {
            if (CleanlinessHelper.percentToOccur(CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.WILD_RAGE).mcdw$getProcChance() + (10 * wildRageLevel))) {
                sendIntoWildRage(ragingEntity);
            }
        }
    }

    // mcdw$onBlockHitTail
    public static void applyCobwebShotBlock(BlockHitResult blockHitResult, AbstractArrow ppe) {
        if (((IMcdwEnchantedArrow)ppe).mcdw$getCobwebShotLevel() > 0) {
            Level ppeWorld = ppe.getCommandSenderWorld();
            Direction side = blockHitResult.getDirection();

            if (ppeWorld.getBlockState(blockHitResult.getBlockPos().relative(side)) == Blocks.AIR.defaultBlockState())
                ppeWorld.setBlockAndUpdate(blockHitResult.getBlockPos().relative(side), Blocks.COBWEB.defaultBlockState());
        }
    }

    public static void applyRadianceShotBlock(BlockHitResult blockHitResult, LivingEntity shooter, AbstractArrow ppe) {
        int radianceLevel = ((IMcdwEnchantedArrow)ppe).mcdw$getRadianceLevel();
        if (radianceLevel > 0) {

            AOECloudHelper.spawnRegenCloudAtPos(shooter, true, blockHitResult.getBlockPos(), radianceLevel - 1);
        }
    }

    // mcdw$onJumpEffects

    public static void activateBurstBowstringOnJump(LivingEntity jumpingEntity) {
        int burstBowstringLevel =
                Math.max(EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.BURST_BOWSTRING), jumpingEntity.getMainHandItem()),
                        EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.BURST_BOWSTRING), jumpingEntity.getOffhandItem()));

        if (burstBowstringLevel > 0) {
            if (jumpingEntity instanceof Player attackingPlayer) {
                int availableArrows = Math.min(InventoryHelper.mcdw$countItem(attackingPlayer, Items.ARROW), burstBowstringLevel);
                if (availableArrows < 1) return; //Avoid area lookup

                ProjectileEffectHelper.mcdw$spawnExtraArrows(jumpingEntity, jumpingEntity, availableArrows, 16, 0.4F);
            }
        }
    }
    public static void handleAddDynamoEffect(Player playerEntity) {
        ItemStack mainHandStack = playerEntity.getMainHandItem();
        ItemStack offHandStack = playerEntity.getOffhandItem();
        if (Math.max(EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.DYNAMO), mainHandStack),
                EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.DYNAMO), offHandStack)) > 0) {
            MobEffectInstance dynamoInstance = playerEntity.getEffect(StatusEffectsRegistry.DYNAMO);
            int i = 1;
            if (dynamoInstance != null) {
                i += dynamoInstance.getAmplifier();
            } else {
                --i;
            }
            i = Mth.clamp(i, 0, (int) CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG
                    .get(EnchantmentsID.DYNAMO).mcdw$getOffset());
            MobEffectInstance dynamoUpdateInstance = new MobEffectInstance(StatusEffectsRegistry.DYNAMO, 120000, i, false, false, true);
            playerEntity.addEffect(dynamoUpdateInstance);
        }
    }

    // Goal Effects
    public static void sendIntoWildRage(Mob mobEntity) {
        ((MobEntityAccessor)mobEntity).targetSelector().addGoal(0, new WildRageAttackGoal(mobEntity));
    }
}
