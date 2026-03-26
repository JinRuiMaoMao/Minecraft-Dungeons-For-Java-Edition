package mcd_java.mcda.statuseffects;

import mcd_java.mcda.Mcda;
import net.minecraft.registry.Registry;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class FreezingStatusEffect extends StatusEffect {
    public FreezingStatusEffect(StatusEffectCategory type, int color, String id) {
        super(type, color);
        Registry.register(Registries.MOB_EFFECT, new Identifier(Mcda.MOD_ID, id), this);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier){
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {

        if (!entity.isDeadOrDying()) {
            int freezingDamage = 1;
            World world = entity.getWorld();
            Random random = Random.create();

            if (world.getGameTime() % 20 == 0) {
                entity.hurt(entity.getWorld().damageSources().freeze(), (float) freezingDamage);
            }

            if (world.isClientSide()) {
                boolean bl = entity.xOld != entity.getX() || entity.zOld != entity.getZ();
                if (bl && random.nextBoolean()) {
                    world.addParticle(ParticleTypes.SNOWFLAKE, entity.getX(), entity.position().y() + 1, entity.getZ(),
                            Mth.randomBetween(random, -1.0F, 1.0F) * 0.083333336F,
                            0.05D,
                            Mth.randomBetween(random, -1.0F, 1.0F) * 0.083333336F);
                }
            }
        }
    }
}
