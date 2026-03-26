package jinrui.mcdar.statuseffect;

import jinrui.mcdar.Mcdar;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class SoulProtectionStatusEffect extends StatusEffect {

    public SoulProtectionStatusEffect(StatusEffectCategory type, int color, String id) {
        super(type, color);
        Registry.register(Registries.MOB_EFFECT, new Identifier(Mcdar.MOD_ID, id), this);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier){
        return true;
    }
}
