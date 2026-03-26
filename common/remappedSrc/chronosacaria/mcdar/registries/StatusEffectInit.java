package jinrui.mcdar.registries;

import jinrui.mcdar.statuseffect.CharmedStatusEffect;
import jinrui.mcdar.statuseffect.ShieldingStatusEffect;
import jinrui.mcdar.statuseffect.SoulProtectionStatusEffect;
import jinrui.mcdar.statuseffect.StunnedStatusEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class StatusEffectInit {
    public static MobEffect CHARMED;
    public static MobEffect SHIELDING;
    public static MobEffect SOUL_PROTECTION;
    public static MobEffect STUNNED;

    public static void init(){
        CHARMED = new CharmedStatusEffect(MobEffectCategory.HARMFUL, 0xC7005B, "charmed");
        SHIELDING = new ShieldingStatusEffect(MobEffectCategory.BENEFICIAL, 0x808080, "shielding");
        SOUL_PROTECTION = new SoulProtectionStatusEffect(MobEffectCategory.BENEFICIAL, 0x2552a5, "soul_protection");
        STUNNED = new StunnedStatusEffect(MobEffectCategory.HARMFUL, 0xFFFF00, "stunned");
    }
}
