package mcd_java.mcda.registries;

import mcd_java.mcda.Mcda;
import mcd_java.blocks.FadingObsidianBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.sound.BlockSoundGroup;

public class BlocksRegistry {
    public static final Block FADING_OBSIDIAN = registerBlock("fading_obsidian",
            new FadingObsidianBlock(FabricBlockSettings.copyOf(Blocks.CRYING_OBSIDIAN).randomTicks().sound(SoundType.GLASS)));

    protected static Block registerBlock(String id, Block block) {
        return Registry.register(Registries.BLOCK, Mcda.ID(id), block);
    }

    public static void register() {
        Mcda.LOGGER.info("Registering Blocks for " + Mcda.MOD_ID);
    }
}
