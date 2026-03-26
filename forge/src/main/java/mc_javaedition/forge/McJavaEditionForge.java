package mc_javaedition.forge;

import mc_javaedition.McJavaEditionCommon;
import mc_javaedition.forge.placeholder.PlaceholderItemsRegistryForge;
import mc_javaedition.forge.placeholder.PlaceholderCreativeTabForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(McJavaEditionForge.MOD_ID)
public class McJavaEditionForge {
    public static final String MOD_ID = "mcd_java";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public McJavaEditionForge() {
        try {
            var bus = FMLJavaModLoadingContext.get().getModEventBus();
            PlaceholderItemsRegistryForge.register(bus);
            PlaceholderCreativeTabForge.register(bus);
            McJavaEditionCommon.init();
            LOGGER.info("Minecraft Dungeons For Java Edition (placeholder items) initialized on Forge");
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize Minecraft Dungeons For Java Edition on Forge", t);
            throw new RuntimeException(t);
        }
    }
}
