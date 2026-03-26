/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.api.util;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.enchants.goals.GoalUtils;
import mcd_java.mcdw.enums.SettingsID;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public class AbilityHelper {

    public static void stealSpeedFromTarget(LivingEntity user, LivingEntity target, int amplifier){
        MobEffectInstance speed = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, amplifier);
        MobEffectInstance slowness = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, amplifier);
        user.addEffect(speed);
        target.addEffect(slowness);
    }

    public static void causeFreezing(LivingEntity target, int amplifier){
        MobEffectInstance freezing = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, amplifier);
        MobEffectInstance miningFatigue = new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, amplifier);
        target.addEffect(freezing);
        target.addEffect(miningFatigue);
    }

    public static boolean isPetOf(LivingEntity owner, LivingEntity animal){
        if (animal instanceof TamableAnimal pet)
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
                || foreignEntity instanceof Player;
    }


    private static boolean isPet(LivingEntity animal) {
        if (animal instanceof TamableAnimal pet)
            return pet.getOwner() != null;
        else if (animal instanceof AbstractHorse horseBaseEntity)
            return GoalUtils.getOwner(horseBaseEntity) != null;
        return false;
    }

    private static boolean isVillagerTyped(LivingEntity nearbyEntity) {
        return (nearbyEntity instanceof Villager) || (nearbyEntity instanceof IronGolem);
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
        if (entity instanceof Player player) {
            if (player.isCreative()) return false;
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
                && !livingEntity.hasEffect(MobEffects.BLINDNESS)
                && livingEntity.fallDistance > 0;
    }
}