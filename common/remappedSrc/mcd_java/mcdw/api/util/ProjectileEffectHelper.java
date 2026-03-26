/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.api.util;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ProjectileEffectHelper {

    public static void mcdw$spawnExtraArrows(LivingEntity owner, LivingEntity makeArrowFromMe, int numArrowsLimit, int distance, double bonusShotDamageMultiplier) {
        List<LivingEntity> nearbyEntities = mcdw$getSecondaryTargets(makeArrowFromMe, distance);
        for (int i = 0; i < Math.min(numArrowsLimit, nearbyEntities.size()); i++) {
            AbstractArrow arrowEntity = mcdw$createProjectileEntityTowards(makeArrowFromMe, nearbyEntities.get(i));
            arrowEntity.setBaseDamage(arrowEntity.getBaseDamage() * bonusShotDamageMultiplier);
            arrowEntity.setOwner(owner);
            makeArrowFromMe.level().addFreshEntity(arrowEntity);
        }
    }

    public static AbstractArrow mcdw$createAbstractArrow(LivingEntity attacker) {
        return ((ArrowItem) Items.ARROW).createArrow(attacker.getCommandSenderWorld(), new ItemStack(Items.ARROW), attacker);
    }

    public static void mcdw$fireChainReactionProjectileFromTarget(Level world, LivingEntity attacker, LivingEntity target,
                                                                  float v1, float v2) {
        if (!world.isClientSide) {
            for (int i = 0 ; i < 4 ; i++) {
                AbstractArrow projectile = mcdw$createAbstractArrow(attacker);
                if (attacker instanceof Player) {
                    projectile.setCritArrow(true);
                }

                projectile.setSoundEvent(SoundEvents.CROSSBOW_HIT);
                projectile.setShotFromCrossbow(true);
                projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                Vec3 upVector = target.getUpVector(1.0F);
                Quaternionf quaternionf = new Quaternionf(upVector.x(), upVector.y(), upVector.z(), -135.0F + (90.0f * i));
                Vector3f vector3f = target.getViewVector(1.0F).toVector3f();
                vector3f.rotate(quaternionf);
                projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), v1, v2);
                projectile.setOwner(target);
                world.addFreshEntity(projectile);
            }
        }
    }

    public static List<LivingEntity> mcdw$getSecondaryTargets(LivingEntity source, double distance) {
        List<LivingEntity> nearbyEntities = AOEHelper.getEntitiesByConfig(source, (float) distance);
        if (nearbyEntities.size() < 2) return Collections.emptyList();

        nearbyEntities.sort(Comparator.comparingDouble(livingEntity -> livingEntity.distanceToSqr(source)));
        return nearbyEntities;
    }

    public static AbstractArrow mcdw$createProjectileEntityTowards(LivingEntity source, LivingEntity target) {
        AbstractArrow projectile = mcdw$createAbstractArrow(source);
        // borrowed from AbstractSkeletonEntity
        double towardsX = target.getX() - source.getX();
        double towardsZ = target.getZ() - source.getZ();
        double euclideanDist = Mth.length(towardsX, towardsZ);
        double towardsY = target.getY(0.3333333333333333D) - projectile.getY() + euclideanDist * 0.2d;
        mcdw$setProjectileTowards(projectile, towardsX, towardsY, towardsZ);
        projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        return projectile;
    }

    public static void mcdw$setProjectileTowards(Projectile projectileEntity, double x, double y, double z) {
        Vec3 vec3d = new Vec3(x, y, z).normalize();
        projectileEntity.setDeltaMovement(vec3d);
        float f = Mth.sqrt((float) projectileEntity.distanceToSqr(vec3d));
        //noinspection SuspiciousNameCombination
        projectileEntity.setYRot((float) (Mth.atan2(vec3d.x, vec3d.z) * (180d / Math.PI)));
        projectileEntity.setXRot((float) (Mth.atan2(vec3d.y, f) * (180d / Math.PI)));
        projectileEntity.yRotO = projectileEntity.getYRot();
        projectileEntity.xRotO = projectileEntity.getXRot();
    }
}