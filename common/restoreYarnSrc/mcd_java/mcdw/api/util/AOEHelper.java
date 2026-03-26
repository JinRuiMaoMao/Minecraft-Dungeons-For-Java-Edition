/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.api.util;

import mcd_java.mcdw.Mcdw;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import java.util.List;
import java.util.function.Predicate;


public class AOEHelper {

    // Owner is center
    public static List<LivingEntity> getEntitiesByConfig(LivingEntity center, float distance) {
        int permissionLevel = Mcdw.CONFIG.mcdwEnchantmentSettingsConfig.aoePermission;
        //noinspection DuplicatedCode
        Predicate<? super LivingEntity> predicate = livingEntity -> AbilityHelper.isAoeTarget(center, livingEntity) &&
                switch (permissionLevel) {
                    case 1 -> !AbilityHelper.isTrueAlly(center, livingEntity);
                    case 2 -> !AbilityHelper.isTrueAlly(center, livingEntity) && !(livingEntity instanceof Animal);
                    case 3 -> !AbilityHelper.isPotentialAlly(livingEntity);
                    case 4 -> livingEntity instanceof Monster;
                    // case 0 has no further restrictions
                    default -> true;
        };
        return getEntitiesByPredicate(center, distance, predicate);
    }

    // Owner and center are different
    public static List<LivingEntity> getEntitiesByConfig(LivingEntity center, LivingEntity owner, float distance) {
        int permissionLevel = Mcdw.CONFIG.mcdwEnchantmentSettingsConfig.aoePermission;
        //noinspection DuplicatedCode
        Predicate<? super LivingEntity> predicate = livingEntity -> AbilityHelper.isAoeTarget(center, owner, livingEntity) &&
                switch (permissionLevel) {
                    case 1 -> !AbilityHelper.isTrueAlly(owner, livingEntity);
                    case 2 -> !AbilityHelper.isTrueAlly(owner, livingEntity) && !(livingEntity instanceof Animal);
                    case 3 -> !AbilityHelper.isPotentialAlly(livingEntity);
                    case 4 -> livingEntity instanceof Monster;
                    // case 0 has no further restrictions
                    default -> true;
                };
        return getEntitiesByPredicate(center, distance, predicate);
    }

    /** Returns targets of an AOE effect from 'attacker' around 'center'. This includes 'center'. */
    public static List<LivingEntity> getEntitiesByPredicate(LivingEntity center, float distance, Predicate<? super LivingEntity> predicate) {
        return center.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class,
                new AABB(center.blockPosition()).inflate(distance), predicate
        );
    }

    public static List<? extends LivingEntity> getEntitiesByPredicate(Class<? extends LivingEntity> entityType,
                                                                      LivingEntity center, float distance, Predicate<? super LivingEntity> predicate) {
        return center.getCommandSenderWorld().getEntitiesOfClass(entityType,
                new AABB(center.blockPosition()).inflate(distance), predicate
        );
    }

    public static void afflictNearbyEntities(LivingEntity user, float distance, MobEffectInstance... statusEffectInstances) {
        for (LivingEntity nearbyEntity : getEntitiesByConfig(user, distance)) {
            for (MobEffectInstance instance : statusEffectInstances)
                nearbyEntity.addEffect(instance);
        }
    }

    public static boolean satisfySweepConditions(LivingEntity attackingEntity, Entity targetEntity, LivingEntity collateralEntity, float distanceToCollateral) {
        return collateralEntity != attackingEntity && collateralEntity != targetEntity && !attackingEntity.isAlliedTo(collateralEntity)
                && !(collateralEntity instanceof ArmorStand armorStand && armorStand.isMarker())
                && attackingEntity.distanceTo(collateralEntity) < distanceToCollateral;
    }
}
