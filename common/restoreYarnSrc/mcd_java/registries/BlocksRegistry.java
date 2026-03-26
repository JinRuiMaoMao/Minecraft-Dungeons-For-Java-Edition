package mcd_java.registries;

import mcd_java.Mcda;
import mcd_java.blocks.FadingObsidianBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

public class BlocksRegistry {
    public static final Block FADING_OBSIDIAN = registerBlock("fading_obsidian",
            new FadingObsidianBlock(FabricBlockSettings.copyOf(Blocks.CRYING_OBSIDIAN).randomTicks().sound(SoundType.GLASS)));

    protected static Block registerBlock(String id, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, Mcda.ID(id), block);
    }

    public static void register() {
        Mcda.LOGGER.info("Registering Blocks for " + Mcda.MOD_ID);
    }
}
