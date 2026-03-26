package mcd_java.mcda.client;

import com.mojang.blaze3d.platform.InputConstants;
import mcd_java.mcda.Mcda;
import mcd_java.mcda.entities.SummonedBeeRenderer;
import mcd_java.mcda.registries.SummonedEntityRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class McdaClient implements ClientModInitializer {

    private static boolean fireTrailEnabled = false;

    private static final KeyMapping FIRE_TRAIL_KEY_BINDING = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.mcda.firetrail.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SEMICOLON,
            "key.category.mcda"
    ));

    public static void toggleFireTrail(){
        fireTrailEnabled = !fireTrailEnabled;
    }

    public static boolean isFireTrailEnabled() {
        return fireTrailEnabled;
    }

    @Override
    public void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while(FIRE_TRAIL_KEY_BINDING.consumeClick()) {
                toggleFireTrail();
                if (client.PlayerEntity != null) {
                    String affirmativeKey = "activity.mcda.firetrail.enabled";
                    String negativeKey = "activity.mcda.firetrail.disabled";
                    if (isFireTrailEnabled())
                        client.PlayerEntity.displayClientMessage(Text.translatable(affirmativeKey), true);
                    else
                        client.PlayerEntity.displayClientMessage(Text.translatable(negativeKey), true);
                }
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeBoolean(isFireTrailEnabled());

                ClientPlayNetworking.send(Mcda.ID("fire_trail_toggle"), buf);
            }
        });

        EntityRendererRegistry.register(SummonedEntityRegistry.SUMMONED_BEE_ENTITY, SummonedBeeRenderer::new);
    }
}
