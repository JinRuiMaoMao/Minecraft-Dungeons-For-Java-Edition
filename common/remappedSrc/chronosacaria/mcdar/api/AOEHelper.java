package jinrui.mcdar.api;

import jinrui.mcdar.mixin.CreeperEntityAccessor;
import jinrui.mcdar.registries.StatusEffectInit;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class AOEHelper {

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

    public static void afflictNearbyEntities(LivingEntity user, MobEffectInstance... statusEffectInstances) {
        for (LivingEntity nearbyEntity : getEntitiesByPredicate(user, 5,
                (nearbyEntity) -> nearbyEntity != user && !AbilityHelper.isPetOf(nearbyEntity, user) && nearbyEntity.isAlive())){
            for (MobEffectInstance instance : statusEffectInstances)
                nearbyEntity.addEffect(instance);
        }
    }

    public static void afflictNearbyEntities(Class<? extends LivingEntity> entityType, LivingEntity user, float distance,
                                             Predicate<? super LivingEntity> predicate, MobEffectInstance... statusEffectInstances) {
        for (LivingEntity nearbyEntity : getEntitiesByPredicate(entityType, user, distance, predicate)) {
            for (MobEffectInstance instance : statusEffectInstances)
                nearbyEntity.addEffect(instance);
        }
    }

    public static void affectNearbyEntities(LivingEntity user, Consumer<LivingEntity> method) {
        for (LivingEntity nearbyEntity : getEntitiesByPredicate(user, 5,
                (nearbyEntity) -> nearbyEntity != user && !AbilityHelper.isPetOf(nearbyEntity, user) && nearbyEntity.isAlive())){
            method.accept(nearbyEntity);
        }
    }

    public static void affectNearbyEntities(LivingEntity user, float distance,
                                            Predicate<? super LivingEntity> predicate, Consumer<LivingEntity> method) {
        for (LivingEntity nearbyEntity : getEntitiesByPredicate(user, distance, predicate)) {
            method.accept(nearbyEntity);
        }
    }

    public static void affectNearbyEntities(Class<? extends LivingEntity> entityType, LivingEntity user, float distance,
                                             Predicate<? super LivingEntity> predicate, Consumer<LivingEntity> method) {
        for (LivingEntity nearbyEntity : getEntitiesByPredicate(entityType, user, distance, predicate)) {
            method.accept(nearbyEntity);
        }
    }

    public static void summonLightningBoltOnEntity(Entity target){
        EntityDataAccessor<Boolean> charged = CreeperEntityAccessor.getCHARGED();
        Level world = target.getCommandSenderWorld();
        LightningBolt lightningEntity = EntityType.LIGHTNING_BOLT.create(world);
        if (lightningEntity != null) {
            lightningEntity.moveTo(target.getX(), target.getY(), target.getZ());
            lightningEntity.setVisualOnly(true);
            if (target instanceof Creeper creeperEntity) {
                creeperEntity.getEntityData().set(charged, true);
            }
            if (target instanceof MushroomCow mooshroomEntity) {
                mooshroomEntity.thunderHit((ServerLevel) world, lightningEntity);
            }
            if (target instanceof Pig pigEntity) {
                pigEntity.thunderHit((ServerLevel) world, lightningEntity);
            }
            if (target instanceof Turtle turtleEntity) {
                turtleEntity.thunderHit((ServerLevel) world, lightningEntity);
            }
            if (target instanceof Villager villagerEntity) {
                villagerEntity.thunderHit((ServerLevel)world, lightningEntity);
            }
            world.addFreshEntity(lightningEntity);
        }
    }

    public static void electrocute(LivingEntity victim, float damageAmount){
        summonLightningBoltOnEntity(victim);
        victim.hurt(victim.level().damageSources().lightningBolt(), damageAmount);
    }

    public static void electrocuteNearbyEnemies(LivingEntity user, float distance, float damageAmount, int limit){
        CleanlinessHelper.playCenteredSound(user, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.0F, 1.0F);
        CleanlinessHelper.playCenteredSound(user, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 1.0F, 1.0F);

        for (LivingEntity nearbyEntity : getEntitiesByPredicate(user, distance,
                (nearbyEntity) -> AbilityHelper.isAoeTarget(nearbyEntity, user, user))) {
            electrocute(nearbyEntity, damageAmount);

            limit--;
            if (limit <= 0) break;
        }
    }

    public static void causeExplosion(LivingEntity user, LivingEntity target, float damageAmount, float distance){
        for (LivingEntity nearbyEntity : getEntitiesByPredicate(target, distance,
                (nearbyEntity) -> AbilityHelper.isAoeTarget(nearbyEntity, user, target))) {
            nearbyEntity.hurt(nearbyEntity.level().damageSources().explosion(target, user), damageAmount);
        }

    }

    public static void knockbackNearbyEnemies(Player user, LivingEntity nearbyEntity, float knockbackMultiplier) {
        double xRatio = user.getX() - nearbyEntity.getX();
        double zRatio;
        for (
                zRatio = user.getZ() - nearbyEntity.getZ();
                xRatio * xRatio + zRatio < 1.0E-4D;
                zRatio = (CleanlinessHelper.RANDOM.nextDouble() - CleanlinessHelper.RANDOM.nextDouble()) * 0.01D) {
            xRatio = (CleanlinessHelper.RANDOM.nextDouble() - CleanlinessHelper.RANDOM.nextDouble()) * 0.01D;
        }
        nearbyEntity.knockback(0.4F * knockbackMultiplier, xRatio, zRatio);
    }

    public static void satchelOfElementsEffects(Player user) {
        int effectInt = (CleanlinessHelper.RANDOM.nextInt(3));

        if (effectInt == 0){ // BURNING
            for (LivingEntity nearbyEntity : getEntitiesByPredicate(user, 5,
                    (nearbyEntity) -> nearbyEntity != user && !AbilityHelper.isPetOf(nearbyEntity, user) && nearbyEntity.isAlive())){
                nearbyEntity.setSecondsOnFire(3);
            }
        }
        if (effectInt == 1) { // FROZEN
            afflictNearbyEntities(user, new MobEffectInstance(StatusEffectInit.STUNNED, 100),
                    new MobEffectInstance(MobEffects.CONFUSION, 100),
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));
        }
        if (effectInt == 2){ // LIGHTNING STRIKE
            for (LivingEntity nearbyEntity : getEntitiesByPredicate(user, 5,
                    (nearbyEntity) -> nearbyEntity != user && !AbilityHelper.isPetOf(nearbyEntity, user) && nearbyEntity.isAlive())){
                summonLightningBoltOnEntity(nearbyEntity);
                nearbyEntity.hurt(nearbyEntity.level().damageSources().lightningBolt(), 5.0f);
            }
        }
    }

    private static void addParticles(ServerLevel world, LivingEntity nearbyEntity, ParticleOptions particleEffect) {

        double velX = 0;
        double velY = 1;
        double velZ = 0;

        double startX = nearbyEntity.getX() - .275f;
        double startY = nearbyEntity.getY();
        double startZ = nearbyEntity.getZ() - .275f;

        for (int i = 0; i < 10; i++) {
            double frontX = .5f * world.getRandom().nextDouble();
            world.sendParticles(particleEffect, startX + frontX, startY + world.getRandom().nextDouble() * .5, startZ + .5f,
                    1,velX, velY, velZ, 0);

            double backX = .5f * world.getRandom().nextDouble();
            world.sendParticles(particleEffect, startX + backX, startY + world.getRandom().nextDouble() * .5, startZ,1, velX, velY,
                    velZ,0);

            double leftZ = .5f * world.getRandom().nextDouble();
            world.sendParticles(particleEffect, startX, startY + world.getRandom().nextDouble() * .5, startZ + leftZ,1, velX, velY,
                    velZ,0);

            double rightZ = .5f * world.getRandom().nextDouble();
            world.sendParticles(particleEffect, startX + .5f, startY + world.getRandom().nextDouble() * .5, startZ + rightZ,1, velX,
                    velY, velZ,0);
        }
    }
}
