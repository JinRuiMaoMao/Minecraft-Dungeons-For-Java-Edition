/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.registries;

import mcd_java.mcdw.Mcdw;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.particle.AttackSweepParticle;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ParticlesRegistry {

    public static final SimpleParticleType OFFHAND_SWEEP_PARTICLE = FabricParticleTypes.simple(true);

    public static void registerOnServer() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(Mcdw.MOD_ID, "offhand_sweep"), OFFHAND_SWEEP_PARTICLE);
    }

    public static void registerOnClient() {
        ParticleFactoryRegistry.getInstance().register(OFFHAND_SWEEP_PARTICLE, AttackSweepParticle.Provider::new);
    }
}
