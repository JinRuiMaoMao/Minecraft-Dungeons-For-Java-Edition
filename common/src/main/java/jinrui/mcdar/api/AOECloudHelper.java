package jinrui.mcdar.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;

public class AOECloudHelper {

    public static void spawnExplosionCloud(LivingEntity attacker, LivingEntity victim, float radius){
        AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloud(victim.getWorld(), victim.getX(), victim.getY(), victim.getZ());
        areaEffectCloudEntity.setOwner(attacker);
        areaEffectCloudEntity.setParticle(ParticleTypes.EXPLOSION);
        areaEffectCloudEntity.setRadius(radius);
        areaEffectCloudEntity.setDuration(0);
        attacker.getWorld().spawnEntity(areaEffectCloudEntity);
    }

    public static void spawnStatusEffectCloud(LivingEntity owner, BlockPos blockPos, int duration, StatusEffectInstance... statusEffectInstances) {
        AreaEffectCloudEntity aoeCloudEntity = new AreaEffectCloud(owner.getWorld(), blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());
        aoeCloudEntity.setOwner(owner);
        aoeCloudEntity.setRadius(5.0f);
        aoeCloudEntity.setRadiusOnUse(-0.5f);
        aoeCloudEntity.setWaitTime(10);
        aoeCloudEntity.setDuration(duration);
        for (StatusEffectInstance instance : statusEffectInstances)
            aoeCloudEntity.addStatusEffect(instance);
        owner.getWorld().spawnEntity(aoeCloudEntity);
    }

}
