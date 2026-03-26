package mcd_java.mcda.api;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;

public class AOECloudHelper {

    public static void spawnParticleCloud(LivingEntity attacker, LivingEntity victim, float radius, int duration, ParticleEffect particleEffect){
        AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloud(victim.getWorld(), victim.getX(), victim.getY(), victim.getZ());
        areaEffectCloudEntity.setOwner(attacker);
        areaEffectCloudEntity.setRadius(radius);
        areaEffectCloudEntity.setDuration(duration);
        areaEffectCloudEntity.setParticle(particleEffect);
        attacker.getWorld().spawnEntity(areaEffectCloudEntity);
    }
}
