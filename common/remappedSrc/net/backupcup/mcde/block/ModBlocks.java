package net.backupcup.mcde.block;

import net.backupcup.mcde.MCDEnchantments;
import net.backupcup.mcde.block.custom.GildingFoundryBlock;
import net.backupcup.mcde.block.custom.RollBenchBlock;
import net.backupcup.mcde.block.custom.RunicTableBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModBlocks {

    public static final Block RUNIC_TABLE = registerBlock("runic_table",
            new RunicTableBlock(FabricBlockSettings.of().forceSolidOn().strength(2f).noOcclusion()), CreativeModeTabs.FUNCTIONAL_BLOCKS);

    public static final Block ROLL_BENCH = registerBlock("roll_bench",
            new RollBenchBlock(FabricBlockSettings.of().forceSolidOn().strength(2f).noOcclusion()), CreativeModeTabs.FUNCTIONAL_BLOCKS);

    public static final Block GILDING_FOUNDRY = registerBlock("gilding_foundry",
            new GildingFoundryBlock(FabricBlockSettings.of().forceSolidOn().strength(2f).noOcclusion()), CreativeModeTabs.FUNCTIONAL_BLOCKS);

    public static Block registerBlock(String name, Block block, ResourceKey<CreativeModeTab> tab) {
        registerBlockItem(name, block, tab);
        return Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(MCDEnchantments.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block, ResourceKey<CreativeModeTab> tab) {
        var item = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MCDEnchantments.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
        ItemGroupEvents.modifyEntriesEvent(tab).register(entries -> entries.accept(item));

        return item;
    }
}
