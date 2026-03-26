package mcd_java.mcda;

import mcd_java.mcda.config.McdaConfig;
import mcd_java.mcda.data.ConfigItemEnabledCondition;
import mcd_java.mcda.networking.McdaC2SPackets;
import mcd_java.mcda.registries.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Mcda implements ModInitializer {
    public static final String MOD_ID = "mcda";
    public static final Logger LOGGER = LogManager.getLogger();

    public static Identifier ID(String path) {
        return new Identifier(MOD_ID, path);
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