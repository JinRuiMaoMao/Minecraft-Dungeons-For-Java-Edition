/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.registries;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.enchants.summons.entity.SummonedBeeEntity;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import java.util.Map;

public class SummonedEntityRegistry {
    public static final Map<EntityType<? extends LivingEntity>, AttributeSupplier> ATTRIBUTES =
            Maps.newHashMap();

    public static final EntityType<SummonedBeeEntity> SUMMONED_BEE_ENTITY =
            FabricEntityTypeBuilder
                    .create(MobCategory.CREATURE, SummonedBeeEntity::new)
                    .dimensions(EntityDimensions.fixed(1,2))
                    .build();

    public static void register(){
        registerEntity(
                "summoned_bee",
                SUMMONED_BEE_ENTITY,
                SummonedBeeEntity.createSummonedBeeEntityAttributes());
    }

    public static void registerEntity(String name, EntityType<? extends LivingEntity> entity, AttributeSupplier.Builder attributes){
        Registry.register(BuiltInRegistries.ENTITY_TYPE, Mcdw.ID(name), entity);
        ATTRIBUTES.put(entity, attributes.build());
    }

}
