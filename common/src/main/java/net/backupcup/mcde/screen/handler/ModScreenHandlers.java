package net.backupcup.mcde.screen.handler;

import net.backupcup.mcde.MCDEnchantments;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.screen.MenuType;

public class ModScreenHandlers {
    public static MenuType<RunicTableScreenHandler> RUNIC_TABLE_SCREEN_HANDLER = new MenuType<>(RunicTableScreenHandler::new, FeatureFlags.VANILLA_SET);
    public static MenuType<RollBenchScreenHandler> ROLL_BENCH_SCREEN_HANDLER = new MenuType<>(RollBenchScreenHandler::new, FeatureFlags.VANILLA_SET);
    public static ExtendedScreenHandlerType<GildingFoundryScreenHandler> GILDING_FOUNDRY_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(GildingFoundryScreenHandler::new);

    public static void registerAllScreenHandlers() {
        registerScreenHandler("runic_table", RUNIC_TABLE_SCREEN_HANDLER);
        registerScreenHandler("roll_bench", ROLL_BENCH_SCREEN_HANDLER);
        registerScreenHandler("gilding_foundry", GILDING_FOUNDRY_SCREEN_HANDLER);
    }

    private static void registerScreenHandler(String id, MenuType<?> type) {
        Registry.register(Registries.MENU, Identifier.tryBuild(MCDEnchantments.MOD_ID, id), type);
    }
}
