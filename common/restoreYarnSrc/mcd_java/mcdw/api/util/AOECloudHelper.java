/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.api.util;

import mcd_java.mcdw.api.interfaces.IExclusiveAOECloud;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;

public class AOECloudHelper {

    public static void spawnAreaEffectCloudEntityWithAttributes(LivingEntity user, LivingEntity center, float cloudRadius,
                                                    int cloudWaitTime, int cloudDuration,
                                                    MobEffect statusEffect, int effectDuration, int effectAmplifier,
                                                    boolean isPicky, boolean exclOwner,
                                                    boolean exclAllies, boolean exclEnemy
                                                    ) {
        AreaEffectCloud areaEffectCloudEntity = new AreaEffectCloud(
                center.level(), center.getX(), center.getY(), center.getZ());
        areaEffectCloudEntity.setOwner(user);
        areaEffectCloudEntity.setRadius(cloudRadius);
        areaEffectCloudEntity.setRadiusOnUse((cloudRadius / 10) * -1);
        areaEffectCloudEntity.setWaitTime(cloudWaitTime);
        areaEffectCloudEntity.setDuration(cloudDuration);
        areaEffectCloudEntity.addEffect(new MobEffectInstance(
                statusEffect, effectDuration, effectAmplifier));
        if (isPicky)
            ((IExclusiveAOECloud) areaEffectCloudEntity).mcdw$setExclusions(exclOwner, exclAllies, exclEnemy);
        center.level().addFreshEntity(areaEffectCloudEntity);
    }

    //Exploding
    public static void spawnExplosionCloud(LivingEntity user, LivingEntity target, float radius) {
        AreaEffectCloud areaEffectCloudEntity = new AreaEffectCloud(
                target.level(), target.getX(), target.getY(), target.getZ());
        areaEffectCloudEntity.setOwner(user);
        areaEffectCloudEntity.setParticle(ParticleTypes.EXPLOSION);
        areaEffectCloudEntity.setRadius(radius);
        areaEffectCloudEntity.setDuration(0);
        user.level().addFreshEntity(areaEffectCloudEntity);
    }

    //Regen Arrow
    public static void spawnRegenCloudAtPos(LivingEntity user, boolean arrow, BlockPos blockPos, int amplifier) {
        int inGroundMitigator = arrow ? 1 : 0;
        AreaEffectCloud areaEffectCloudEntity = new AreaEffectCloud(
                user.level(), blockPos.getX(), blockPos.getY() + inGroundMitigator, blockPos.getZ());
        areaEffectCloudEntity.setOwner(user);
        areaEffectCloudEntity.setRadius(5.0F);
        areaEffectCloudEntity.setRadiusOnUse(-0.5F);
        areaEffectCloudEntity.setWaitTime(10);
        areaEffectCloudEntity.setDuration(60);
        MobEffectInstance regeneration = new MobEffectInstance(MobEffects.REGENERATION, 100, amplifier);
        areaEffectCloudEntity.addEffect(regeneration);
        ((IExclusiveAOECloud) areaEffectCloudEntity).mcdw$setExclusions(false, false, true);
        user.level().addFreshEntity(areaEffectCloudEntity);
    }
}