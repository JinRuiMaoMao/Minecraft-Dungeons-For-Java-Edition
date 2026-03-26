package net.backupcup.mcde.block.entity;

import net.backupcup.mcde.MCDEnchantments;
import net.backupcup.mcde.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static BlockEntityType<GildingFoundryBlockEntity> GILDING_FOUNDRY;

    public static void registerBlockEntities() {
        GILDING_FOUNDRY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(MCDEnchantments.MOD_ID, "gilding_foundry"),
                FabricBlockEntityTypeBuilder.create(GildingFoundryBlockEntity::new,
                        ModBlocks.GILDING_FOUNDRY).build());
    }
}
