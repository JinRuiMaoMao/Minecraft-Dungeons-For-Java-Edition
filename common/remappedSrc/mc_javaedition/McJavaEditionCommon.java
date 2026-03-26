package mc_javaedition;

import jinrui.mcdar.Mcdar;
import mcd_java.Mcda;
import mcd_java.mcdw.Mcdw;
import mc_javaedition.legacy.LegacyCreeperWoodsRegistry;
import mc_javaedition.legacy.LegacyCreeperWoodsItemsRegistry;
import net.backupcup.mcde.MCDEnchantments;

public final class McJavaEditionCommon {
    private static boolean initialized = false;

    private McJavaEditionCommon() {}

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        new Mcda().onInitialize();
        new Mcdw().onInitialize();
        new Mcdar().onInitialize();
        new MCDEnchantments().onInitialize();
        LegacyCreeperWoodsRegistry.register();
        LegacyCreeperWoodsItemsRegistry.register();
    }
}
