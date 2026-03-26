package mcd_java.registries;

import mcd_java.statuseffects.FreezingStatusEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class StatusEffectsRegistry {
    public static MobEffect FREEZING;

    public static void register(){
        FREEZING = new FreezingStatusEffect(MobEffectCategory.HARMFUL, 0xadd8e6, "freezing");
    }
}
