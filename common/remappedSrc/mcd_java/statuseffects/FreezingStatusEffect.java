package mcd_java.statuseffects;

import mcd_java.Mcda;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class FreezingStatusEffect extends MobEffect {
    public FreezingStatusEffect(MobEffectCategory type, int color, String id) {
        super(type, color);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation(Mcda.MOD_ID, id), this);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier){
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {

        if (!entity.isDeadOrDying()) {
            int freezingDamage = 1;
            Level world = entity.level();
            RandomSource random = RandomSource.create();

            if (world.getGameTime() % 20 == 0) {
                entity.hurt(entity.level().damageSources().freeze(), (float) freezingDamage);
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
