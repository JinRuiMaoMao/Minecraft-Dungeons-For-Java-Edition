package jinrui.mcdar;

import jinrui.mcdar.config.McdarConfig;
import jinrui.mcdar.registries.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;

public class Mcdar implements ModInitializer {
    public static final String MOD_ID = "mcdar";
    public static ResourceLocation ID (String path){
        return new ResourceLocation(MOD_ID, path);
    }
    public static McdarConfig CONFIG;

    @Override
    public void onInitialize() {
        McdarConfig.register();
        CONFIG = AutoConfig.getConfigHolder(McdarConfig.class).getConfig();
        ItemGroupRegistry.register();
        ArtifactsRegistry.register();
        EnchantsRegistry.register();
        StatusEffectInit.init();
        LootRegistry.register();
        SummonedEntityRegistry.register();
    }
}
