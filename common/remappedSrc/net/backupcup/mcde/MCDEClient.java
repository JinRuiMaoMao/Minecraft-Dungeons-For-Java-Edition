package net.backupcup.mcde;

import net.backupcup.mcde.block.ModBlocks;
import net.backupcup.mcde.screen.GildingFoundryScreen;
import net.backupcup.mcde.screen.RollBenchScreen;
import net.backupcup.mcde.screen.RunicTableScreen;
import net.backupcup.mcde.screen.handler.GildingFoundryScreenHandler;
import net.backupcup.mcde.screen.handler.ModScreenHandlers;
import net.backupcup.mcde.screen.handler.RollBenchScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;

public class MCDEClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModScreenHandlers.RUNIC_TABLE_SCREEN_HANDLER, RunicTableScreen::new);
        MenuScreens.register(ModScreenHandlers.ROLL_BENCH_SCREEN_HANDLER, RollBenchScreen::new);
        MenuScreens.register(ModScreenHandlers.GILDING_FOUNDRY_SCREEN_HANDLER, GildingFoundryScreen::new);

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.RUNIC_TABLE, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.ROLL_BENCH, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GILDING_FOUNDRY, RenderType.cutout());

        ClientPlayNetworking.registerGlobalReceiver(MCDEnchantments.SYNC_CONFIG_PACKET, (client, handler, buf, responseSender) -> {
            MCDEnchantments.setConfig(Config.readFromServer(buf));
        });

        ClientPlayNetworking.registerGlobalReceiver(RollBenchScreenHandler.LOCKED_SLOTS_PACKET, RollBenchScreenHandler::receiveNewLocks);
        ClientPlayNetworking.registerGlobalReceiver(GildingFoundryScreenHandler.GILDING_PACKET, GildingFoundryScreenHandler::receiveNewEnchantment);
    }
}
