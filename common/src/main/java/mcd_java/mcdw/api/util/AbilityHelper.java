/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.api.util;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.enchants.goals.GoalUtils;
import mcd_java.mcdw.enums.SettingsID;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.horse.AbstractHorse;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;

public class AbilityHelper {

    public static void stealSpeedFromTarget(LivingEntity user, LivingEntity target, int amplifier){
        StatusEffectInstance speed = new StatusEffectInstance(StatusEffects.MOVEMENT_SPEED, 80, amplifier);
        StatusEffectInstance slowness = new StatusEffectInstance(StatusEffects.SLOWNESS, 80, amplifier);
        user.addStatusEffect(speed);
        target.addStatusEffect(slowness);
    }

    public static void causeFreezing(LivingEntity target, int amplifier){
        StatusEffectInstance freezing = new StatusEffectInstance(StatusEffects.SLOWNESS, 60, amplifier);
        StatusEffectInstance miningFatigue = new StatusEffectInstance(StatusEffects.DIG_SLOWDOWN, 60, amplifier);
        target.addStatusEffect(freezing);
        target.addStatusEffect(miningFatigue);
    }

    public static boolean isPetOf(LivingEntity owner, LivingEntity animal){
        if (animal instanceof TameableEntity pet)
            return pet.getOwner() == owner;
        else if (animal instanceof AbstractHorse horseBaseEntity)
            return GoalUtils.getOwner(horseBaseEntity) == owner;
        else
            return false;
    }

    public static boolean isTrueAlly(LivingEntity self, LivingEntity foreignEntity) {
        return self.isAlliedTo(foreignEntity)
                || isPetOf(self, foreignEntity)
                || isVillagerTyped(foreignEntity);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPotentialAlly(LivingEntity foreignEntity) {
        return isPet(foreignEntity)
                || isVillagerTyped(foreignEntity)
                || foreignEntity instanceof PlayerEntity;
    }


    private static boolean isPet(LivingEntity animal) {
        if (animal instanceof TameableEntity pet)
            return pet.getOwner() != null;
        else if (animal instanceof AbstractHorse horseBaseEntity)
            return GoalUtils.getOwner(horseBaseEntity) != null;
        return false;
    }

    private static boolean isVillagerTyped(LivingEntity nearbyEntity) {
        return (nearbyEntity instanceof VillagerEntity) || (nearbyEntity instanceof IronGolemEntity);
    }

    public static boolean isAoeTarget(LivingEntity self, LivingEntity foreignEntity) {
        return foreignEntity != self
                && foreignEntity.isAlive()
                && isAffectedByAoe(foreignEntity)
                && self.hasLineOfSight(foreignEntity);
    }

    public static boolean isAoeTarget(LivingEntity center, LivingEntity owner, LivingEntity foreignEntity) {
        return foreignEntity != owner
                && foreignEntity.isAlive()
                && isAffectedByAoe(foreignEntity)
                && center.hasLineOfSight(foreignEntity);
    }

    private static boolean isAffectedByAoe(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            if (PlayerEntity.isCreative()) return false;
            return !Mcdw.CONFIG.mcdwEnchantmentSettingsConfig.ENABLE_ENCHANTMENT_SETTINGS.get(SettingsID.AREA_OF_EFFECT_ENCHANTS_DONT_AFFECT_PLAYERS);
        }
        return true;
    }

    public static float getAnimaRepairAmount(float experience, int level) {
        experience *= (float) (0.2 * level);
        return experience;
    }

    public static boolean entityCanCrit(LivingEntity livingEntity) {
        return !livingEntity.onClimbable()
                && !livingEntity.isInWater()
                && !livingEntity.onGround()
                && !livingEntity.isSprinting()
                && !livingEntity.isPassenger()
                && !livingEntity.hasEffect(StatusEffects.BLINDNESS)
                && livingEntity.fallDistance > 0;
    }
}