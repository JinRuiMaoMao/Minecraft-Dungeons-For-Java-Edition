package jinrui.mcdar.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;

public class AOECloudHelper {

    public static void spawnExplosionCloud(LivingEntity attacker, LivingEntity victim, float radius){
        AreaEffectCloud areaEffectCloudEntity = new AreaEffectCloud(victim.level(), victim.getX(), victim.getY(), victim.getZ());
        areaEffectCloudEntity.setOwner(attacker);
        areaEffectCloudEntity.setParticle(ParticleTypes.EXPLOSION);
        areaEffectCloudEntity.setRadius(radius);
        areaEffectCloudEntity.setDuration(0);
        attacker.level().addFreshEntity(areaEffectCloudEntity);
    }

    public static void spawnStatusEffectCloud(LivingEntity owner, BlockPos blockPos, int duration, MobEffectInstance... statusEffectInstances) {
        AreaEffectCloud aoeCloudEntity = new AreaEffectCloud(owner.level(), blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());
        aoeCloudEntity.setOwner(owner);
        aoeCloudEntity.setRadius(5.0f);
        aoeCloudEntity.setRadiusOnUse(-0.5f);
        aoeCloudEntity.setWaitTime(10);
        aoeCloudEntity.setDuration(duration);
        for (MobEffectInstance instance : statusEffectInstances)
            aoeCloudEntity.addEffect(instance);
        owner.level().addFreshEntity(aoeCloudEntity);
    }

}
