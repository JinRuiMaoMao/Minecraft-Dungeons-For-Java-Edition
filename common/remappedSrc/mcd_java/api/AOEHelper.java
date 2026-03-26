package mcd_java.api;

import mcd_java.registries.EnchantsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import java.util.List;

import static mcd_java.enchants.EnchantID.HEAL_ALLIES;

public class AOEHelper {

    /* Return targets of an AOE effect from 'attacker' around 'center'. This includes 'center'. */
    public static List<LivingEntity> getAoeTargets(LivingEntity center, LivingEntity attacker, float distance) {
        return center.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class,
                new AABB(center.blockPosition()).inflate(distance),
                (nearbyEntity) -> AbilityHelper.isAoeTarget(nearbyEntity, attacker, center)
        );
    }

    public static List<LivingEntity> getAttackersOfEntities(LivingEntity affectedEntity, float distance) {
        return affectedEntity.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class,
                new AABB(affectedEntity.blockPosition()).inflate(distance),
                (nearbyEntity) -> nearbyEntity.getLastHurtMob() == affectedEntity
        );
    }

    public static void healNearbyAllies(LivingEntity healer, MobEffectInstance effectInstance, float distance) {
        if (!(healer instanceof Player playerEntity)) return;

        Level world = healer.getCommandSenderWorld();
        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class,
                new AABB(healer.blockPosition()).inflate(distance),
                (nearbyEntity) -> nearbyEntity != healer && AbilityHelper.canHealEntity(healer, nearbyEntity));

        for (LivingEntity nearbyEntity : nearbyEntities) {
            if (nearbyEntity == null) return;
            if (nearbyEntity.getHealth() < nearbyEntity.getMaxHealth()) {
                if (effectInstance.getEffect().isInstantenous()) {
                    effectInstance.getEffect().applyInstantenousEffect(playerEntity, playerEntity, nearbyEntity,
                            effectInstance.getAmplifier(), 1.0D);
                } else {
                    nearbyEntity.addEffect(new MobEffectInstance(effectInstance));
                }

                addParticles((ServerLevel) world, nearbyEntity, ParticleTypes.HEART);
            }
        }
    }

    public static void healNearbyAllies(LivingEntity healer, float amount) {
        if (healer.getHealth() >= healer.getMaxHealth())
            return;

        int healAlliesLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(HEAL_ALLIES), healer);
        if (healAlliesLevel <= 0)
            return;

        amount *= 0.25f * healAlliesLevel;
        float distance = 12;
        Level world = healer.getCommandSenderWorld();

        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class,
                new AABB(healer.blockPosition()).inflate(distance),
                (nearbyEntity) -> nearbyEntity != healer && AbilityHelper.canHealEntity(healer, nearbyEntity));
        for (LivingEntity nearbyEntity : nearbyEntities) {
            if (nearbyEntity == null) return;
            if (nearbyEntity.getHealth() < nearbyEntity.getMaxHealth()) {
                nearbyEntity.heal(amount);
                addParticles((ServerLevel) world, nearbyEntity, ParticleTypes.HEART);
            }
        }
    }

    public static void burnNearbyEnemies(LivingEntity attacker, float damage, float distance) {
        Level world = attacker.getCommandSenderWorld();

        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class,
                new AABB(attacker.blockPosition()).inflate(distance),
                (nearbyEntity) -> AbilityHelper.canFireAtEnemy(attacker, nearbyEntity));

        for (LivingEntity nearbyEntity : nearbyEntities) {
            if (nearbyEntity == null) return;
            nearbyEntity.hurt(nearbyEntity.level().damageSources().onFire(), damage);
        }
    }

    public static void causeExplosion(LivingEntity user, LivingEntity target, float damageAmount, float distance) {
        Explosion explosion = new Explosion(target.getCommandSenderWorld(), target, 5, 5, 5, 3, false, Explosion.BlockInteraction.KEEP);
        for (LivingEntity nearbyEntity : getAoeTargets(target, user, distance)) {
            nearbyEntity.hurt(nearbyEntity.level().damageSources().explosion(explosion), damageAmount);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void addParticles(ServerLevel world, LivingEntity nearbyEntity, ParticleOptions particleEffect) {

        double velX = 0;
        double velY = 1;
        double velZ = 0;

        double startX = nearbyEntity.getX() - .275f;
        double startY = nearbyEntity.getY();
        double startZ = nearbyEntity.getZ() - .275f;

        for (int i = 0; i < 10; i++) {
            RandomSource random = world.getRandom();
            double frontX = .5f * random.nextDouble();
            world.sendParticles(particleEffect, startX + frontX, startY + random.nextDouble() * .5, startZ + .5f,
                    1,velX, velY, velZ, 0);

            double backX = .5f * random.nextDouble();
            world.sendParticles(particleEffect, startX + backX, startY + random.nextDouble() * .5, startZ,1, velX, velY,
                    velZ,0);

            double leftZ = .5f * random.nextDouble();
            world.sendParticles(particleEffect, startX, startY + random.nextDouble() * .5, startZ + leftZ,1, velX, velY,
                    velZ,0);

            double rightZ = .5f * random.nextDouble();
            world.sendParticles(particleEffect, startX + .5f, startY + random.nextDouble() * .5, startZ + rightZ,1, velX,
                    velY, velZ,0);
        }
    }

    public static void addParticlesToBlock(ServerLevel world, BlockPos blockPos, ParticleOptions particleEffect){
        double velX = 0;
        double velY = 1;
        double velZ = 0;

        double startX = blockPos.getX() - .275f;
        double startY = blockPos.getY();
        double startZ = blockPos.getZ() - .275f;

        for (int i = 0; i < 10; i++) {
            RandomSource random = world.getRandom();
            double frontX = .5f * random.nextDouble();
            world.sendParticles(particleEffect, startX + frontX, startY + random.nextDouble() * .5, startZ + .5f,
                    1, velX, velY, velZ, 0);

            double backX = .5f * random.nextDouble();
            world.sendParticles(particleEffect, startX + backX, startY + random.nextDouble() * .5, startZ, 1, velX, velY,
                    velZ, 0);

            double leftZ = .5f * random.nextDouble();
            world.sendParticles(particleEffect, startX, startY + random.nextDouble() * .5, startZ + leftZ, 1, velX, velY,
                    velZ, 0);

            double rightZ = .5f * random.nextDouble();
            world.sendParticles(particleEffect, startX + .5f, startY + random.nextDouble() * .5, startZ + rightZ, 1, velX,
                    velY, velZ, 0);
        }
    }
}
