package mc_javaedition.legacy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class LegacyCreeperWoodsRegistry {
    private static final String LEGACY_NAMESPACE = "mcd_javaedition";

    private static final String[] LEGACY_BLOCK_IDS = new String[] {
            "bigwell", "blueurn", "chiseledstonecube", "chiseledstonecubemossy", "chiseledstoneplus",
            "chiseledstoneplusdirt", "chiseledstoneplusdirty", "cobblestonefloordirty", "cobblestonefloordirty2",
            "cobblestonefloordirty3", "cobblestonemossy", "cobblestonemossy2", "crackedandesite", "cwandesite",
            "cwchiseledstonebrick", "cwcobblestone", "cwcrackedmossystone", "cwcrackedstonebrick", "cwdarkerdirt",
            "cwdirt", "cwflowers", "cwglowmushroom", "cwgrass", "cwgrassblock", "cwgrassdirt", "cwgrasspath",
            "cwleaves", "cwmossyandesite", "cwmossycobblestone", "cwmossycobblestone2", "cwmossydirt", "cwmossystone",
            "cwmossystonebricks", "cwmossystonebricks2", "cwstone", "cwstonebricks", "cwtallgrassbottom", "cwtallgrassup",
            "darkerstone", "diamondchest", "dirt", "dirtmossy", "dirtmossystone", "dirtpath", "dirtygranite",
            "dirtystonebricksskeleton", "emeraldchest", "fancytest", "floorcobweb", "glowingplantblock", "glowingplants",
            "grass", "grassdirt", "grave", "lightbblue", "lightbwhite", "lightbyellow", "mcdtent", "skeletonchest",
            "stonefloor", "stonefloordirty", "stonefloordirty2", "stonefloordirty3", "stonefloorpath", "stonefloorpath2",
            "stonefloorpath3", "stonefloorpath4", "stonefloorpathdirtstone", "stonefloorpathstone", "stonegranitesmooth",
            "woodenchest"
    };

    private LegacyCreeperWoodsRegistry() {}

    public static void register() {
        for (String id : LEGACY_BLOCK_IDS) {
            registerBlockWithItem(id);
        }
    }

    private static void registerBlockWithItem(String path) {
        ResourceLocation id = new ResourceLocation(LEGACY_NAMESPACE, path);
        if (BuiltInRegistries.BLOCK.containsKey(id)) {
            return;
        }

        Block block = new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5f, 6.0f)
                .sound(SoundType.STONE)
                .pushReaction(PushReaction.NORMAL));

        Registry.register(BuiltInRegistries.BLOCK, id, block);
        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties()));
    }
}
