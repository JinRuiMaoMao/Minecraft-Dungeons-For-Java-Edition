package jinrui.mcdar.registries;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.entities.*;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.IronGolem;
import java.util.Map;

public class SummonedEntityRegistry {

    public static final Map<EntityType<? extends LivingEntity>, AttributeSupplier> ATTRIBUTES =
            Maps.newHashMap();
    public static final EntityType<BuzzyNestBeeEntity> BUZZY_NEST_BEE_ENTITY =
            FabricEntityTypeBuilder
                    .create(MobCategory.CREATURE, BuzzyNestBeeEntity::new)
                    .dimensions(EntityDimensions.fixed(1, 2))
                    .build();
    public static final EntityType<EnchantedGrassGreenSheepEntity> ENCHANTED_GRASS_GREEN_SHEEP_ENTITY =
            FabricEntityTypeBuilder
                    .create(MobCategory.CREATURE, EnchantedGrassGreenSheepEntity::new)
                    .dimensions(EntityDimensions.fixed(1, 2))
                    .build();
    public static final EntityType<EnchantedGrassBlueSheepEntity> ENCHANTED_GRASS_BLUE_SHEEP_ENTITY =
            FabricEntityTypeBuilder
                    .create(MobCategory.CREATURE, EnchantedGrassBlueSheepEntity::new)
                    .dimensions(EntityDimensions.fixed(1, 2))
                    .build();
    public static final EntityType<EnchantedGrassRedSheepEntity> ENCHANTED_GRASS_RED_SHEEP_ENTITY =
            FabricEntityTypeBuilder
                    .create(MobCategory.CREATURE, EnchantedGrassRedSheepEntity::new)
                    .dimensions(EntityDimensions.fixed(1, 2))
                    .build();
    public static final EntityType<GolemKitGolemEntity> GOLEM_KIT_GOLEM_ENTITY =
            FabricEntityTypeBuilder
                    .create(MobCategory.CREATURE, GolemKitGolemEntity::new)
                    .dimensions(EntityDimensions.fixed(1, 2))
                    .build();
    public static final EntityType<TastyBoneWolfEntity> TASTY_BONE_WOLF_ENTITY =
            FabricEntityTypeBuilder
                    .create(MobCategory.CREATURE, TastyBoneWolfEntity::new)
                    .dimensions(EntityDimensions.fixed(1, 2))
                    .build();
    public static final EntityType<WonderfulWheatLlamaEntity> WONDERFUL_WHEAT_LLAMA_ENTITY =
            FabricEntityTypeBuilder
                    .create(MobCategory.CREATURE, WonderfulWheatLlamaEntity::new)
                    .dimensions(EntityDimensions.fixed(1, 2))
                    .build();

    public static void register(){
        registerEntity("buzzy_nest_bee", Bee.createAttributes(), BUZZY_NEST_BEE_ENTITY);
        registerEntity("enchanted_blue_sheep", EnchantedGrassBlueSheepEntity.createEnchantedBlueSheepEntityAttributes(), ENCHANTED_GRASS_BLUE_SHEEP_ENTITY);
        registerEntity("enchanted_green_sheep", EnchantedGrassGreenSheepEntity.createEnchantedGreenSheepEntityAttributes(), ENCHANTED_GRASS_GREEN_SHEEP_ENTITY);
        registerEntity("enchanted_red_sheep", EnchantedGrassRedSheepEntity.createEnchantedRedSheepAttributes(), ENCHANTED_GRASS_RED_SHEEP_ENTITY);
        registerEntity("golem_kit_golem", IronGolem.createAttributes(), GOLEM_KIT_GOLEM_ENTITY);
        registerEntity("tasty_bone_wolf", TastyBoneWolfEntity.createTastyBoneWolfAttributes(), TASTY_BONE_WOLF_ENTITY);
        registerEntity("wonderful_wheat_llama", WonderfulWheatLlamaEntity.createAttributes(), WONDERFUL_WHEAT_LLAMA_ENTITY);
    }

    public static void registerEntity(String name,
                                      AttributeSupplier.Builder attributes,
                                      EntityType<? extends LivingEntity> entityType){
        FabricDefaultAttributeRegistry.register(entityType, attributes);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, Mcdar.ID(name), entityType);
    }
}
