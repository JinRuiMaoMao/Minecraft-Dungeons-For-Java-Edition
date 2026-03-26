package mcd_java.registries;

import mcd_java.Mcda;
import mcd_java.entities.SummonedBeeEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import java.util.List;
import java.util.Map;

public class SummonedEntityRegistry {
    public static final Map<EntityType<? extends LivingEntity>, AttributeSupplier> ATTRIBUTES =
            Maps.newHashMap();
    private static final List<EntityType<?>> SUMMONED_ENTITIES = Lists.newArrayList();

    public static final EntityType<SummonedBeeEntity> SUMMONED_BEE_ENTITY =
            FabricEntityTypeBuilder
                    .create(MobCategory.CREATURE, SummonedBeeEntity::new)
                    .dimensions(EntityDimensions.fixed(1,2))
                    .build();

    public static void register(){
        registerEntity("summoned_bee", SUMMONED_BEE_ENTITY);
    }

    public static void registerEntity(String name, EntityType<? extends LivingEntity> entity){
        Registry.register(BuiltInRegistries.ENTITY_TYPE, Mcda.ID(name), entity);
        ATTRIBUTES.put(entity, SummonedBeeEntity.getAttributeContainer());
    }
}
