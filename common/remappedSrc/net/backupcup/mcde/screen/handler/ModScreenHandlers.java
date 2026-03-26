package net.backupcup.mcde.screen.handler;

import net.backupcup.mcde.MCDEnchantments;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

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
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.tryBuild(MCDEnchantments.MOD_ID, id), type);
    }
}
