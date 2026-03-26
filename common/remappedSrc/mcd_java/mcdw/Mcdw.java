/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw;

import mcd_java.mcdw.compat.RangedWeaponAPICompat;
import mcd_java.mcdw.configs.CompatibilityFlags;
import mcd_java.mcdw.configs.McdwConfig;
import mcd_java.mcdw.data.ConfigItemEnabledCondition;
import mcd_java.mcdw.networking.OffhandAttackPacket;
import mcd_java.mcdw.registries.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Mcdw implements ModInitializer {

    public static final String MOD_ID = "mcdw";
    @SuppressWarnings("unused")
    public static final Logger LOGGER = LogManager.getLogger();
    public static ResourceLocation ID(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
    public static McdwConfig CONFIG;

    @Override
    public void onInitialize() {
        CompatibilityFlags.init();
        CompatRegistry.register();
        EntityAttributesRegistry.register();
        McdwConfig.register();
        CONFIG = AutoConfig.getConfigHolder(McdwConfig.class).getConfig();
        ConfigItemEnabledCondition.register();
        ParticlesRegistry.registerOnServer();
        ItemGroupRegistry.register();
        ItemsRegistry.register();
        OffhandAttackPacket.register();
        LootTablesRegistry.register();
        EnchantsRegistry.register();
        SoundEventsRegistry.register();
        SummonedEntityRegistry.register();
        StatusEffectsRegistry.register();
        EnchantmentRestrictionsRegistry.register();
        if (FabricLoader.getInstance().isModLoaded("ranged_weapon_api")) {
            RangedWeaponAPICompat.init();
        }
    }
}