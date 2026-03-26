package mcd_java;

import mcd_java.config.McdaConfig;
import mcd_java.data.ConfigItemEnabledCondition;
import mcd_java.networking.McdaC2SPackets;
import mcd_java.registries.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Mcda implements ModInitializer {
    public static final String MOD_ID = "mcda";
    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation ID(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static McdaConfig CONFIG;

    @Override
    public void onInitialize() {
        McdaConfig.register();
        CONFIG = AutoConfig.getConfigHolder(McdaConfig.class).getConfig();
        ConfigItemEnabledCondition.init();
        BlocksRegistry.register();
        ArmorsRegistry.register();
        EnchantsRegistry.register();
        ItemGroupRegistry.register();
        LootRegistry.register();
        SoundsRegistry.register();
        StatusEffectsRegistry.register();
        TradesRegistry.registerVillagerOffers();
        TradesRegistry.registerWanderingTrades();
        SummonedEntityRegistry.register();
        McdaC2SPackets.registerC2SReceivers();
        CompatRegistry.register();
    }
}