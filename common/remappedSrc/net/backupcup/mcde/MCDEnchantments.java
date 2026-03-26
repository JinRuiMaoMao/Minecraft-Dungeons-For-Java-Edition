package net.backupcup.mcde;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.backupcup.mcde.block.entity.ModBlockEntities;
import net.backupcup.mcde.screen.handler.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class MCDEnchantments implements ModInitializer {
	public static final String MOD_ID = "mcde";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ResourceLocation SYNC_CONFIG_PACKET = ResourceLocation.tryBuild(MOD_ID, "sync_config");
    private static Config config;

    public static Config getConfig() {
        return config;
    }

	public static void setConfig(Config config) {
        MCDEnchantments.config = config;
    }

    @Override
	public void onInitialize() {
		ModScreenHandlers.registerAllScreenHandlers();
		ModBlockEntities.registerBlockEntities();

        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.tryBuild(MOD_ID, "config");
            }
            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                config = Config.load();
            }
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            var buf = PacketByteBufs.create();
            config.writeToClient(buf);
            ServerPlayNetworking.send(player, SYNC_CONFIG_PACKET, buf);
            if (Config.lastError != null) {
                player.sendSystemMessage(Component.literal("[MCDEnchantments]: ")
                        .append(Config.lastError).withStyle(ChatFormatting.RED));
            }
        });
	}
}
