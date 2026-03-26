package jinrui.mcdar.statuseffect;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.goals.LoveMedallionAttackGoal;
import jinrui.mcdar.mixin.MobEntityAccessor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;

public class CharmedStatusEffect extends MobEffect {
    public CharmedStatusEffect(MobEffectCategory statusEffectCategory, int color, String id) {
        super(statusEffectCategory, color);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation(Mcdar.MOD_ID, id), this);
    }

    private static Goal inLoveIdiot;

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (entity instanceof Mob mobEntity) {
            inLoveIdiot = new LoveMedallionAttackGoal(mobEntity);
            ((MobEntityAccessor) mobEntity).targetSelector().addGoal(0, inLoveIdiot);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (entity instanceof Mob mobEntity) {
            if (inLoveIdiot != null) {
                ((MobEntityAccessor) mobEntity).targetSelector().removeGoal(inLoveIdiot);
                entity.hurt(entity.level().damageSources().magic(), (float) attributes.getValue(Attributes.ATTACK_DAMAGE));
            }
        }
    }
}
