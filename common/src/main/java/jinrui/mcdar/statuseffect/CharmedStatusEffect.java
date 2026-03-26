package jinrui.mcdar.statuseffect;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.goals.LoveMedallionAttackGoal;
import jinrui.mcdar.mixin.MobEntityAccessor;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.ai.goal.Goal;

public class CharmedStatusEffect extends StatusEffect {
    public CharmedStatusEffect(StatusEffectCategory statusEffectCategory, int color, String id) {
        super(statusEffectCategory, color);
        Registry.register(Registries.MOB_EFFECT, new Identifier(Mcdar.MOD_ID, id), this);
    }

    private static Goal inLoveIdiot;

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (entity instanceof MobEntity mobEntity) {
            inLoveIdiot = new LoveMedallionAttackGoal(mobEntity);
            ((MobEntityAccessor) mobEntity).targetSelector().addGoal(0, inLoveIdiot);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (entity instanceof MobEntity mobEntity) {
            if (inLoveIdiot != null) {
                ((MobEntityAccessor) mobEntity).targetSelector().removeGoal(inLoveIdiot);
                entity.hurt(entity.getWorld().damageSources().magic(), (float) attributes.getValue(EntityAttributes.ATTACK_DAMAGE));
            }
        }
    }
}
