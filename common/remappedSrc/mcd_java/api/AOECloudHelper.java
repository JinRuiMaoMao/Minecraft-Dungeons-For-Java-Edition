package mcd_java.api;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;

public class AOECloudHelper {

    public static void spawnParticleCloud(LivingEntity attacker, LivingEntity victim, float radius, int duration, ParticleOptions particleEffect){
        AreaEffectCloud areaEffectCloudEntity = new AreaEffectCloud(victim.level(), victim.getX(), victim.getY(), victim.getZ());
        areaEffectCloudEntity.setOwner(attacker);
        areaEffectCloudEntity.setRadius(radius);
        areaEffectCloudEntity.setDuration(duration);
        areaEffectCloudEntity.setParticle(particleEffect);
        attacker.level().addFreshEntity(areaEffectCloudEntity);
    }
}
