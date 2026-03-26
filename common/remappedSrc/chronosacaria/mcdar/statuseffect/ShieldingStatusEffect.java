package jinrui.mcdar.statuseffect;

import jinrui.mcdar.Mcdar;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ShieldingStatusEffect extends MobEffect {

    public ShieldingStatusEffect(MobEffectCategory type, int color, String id) {
        super(type, color);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation(Mcdar.MOD_ID, id), this);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier){
        return true;
    }
}
