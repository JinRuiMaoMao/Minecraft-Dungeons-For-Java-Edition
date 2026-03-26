/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.networking;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.api.interfaces.IDualWielding;
import mcd_java.mcdw.api.util.PlayerAttackHelper;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

@SuppressWarnings("deprecation")
public class OffhandAttackPacket {

    public static final ResourceLocation OFFHAND_ATTACK_PACKET = new ResourceLocation(Mcdw.MOD_ID, "offhand_attack_entity");
    public static final ResourceLocation OFFHAND_MISS_PACKET = new ResourceLocation(Mcdw.MOD_ID, "offhand_miss_entity");

    public static Packet<ServerGamePacketListener> offhandAttackPacket(Entity entity) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(entity.getId());
        return ClientPlayNetworking.createC2SPacket(OFFHAND_ATTACK_PACKET, buf);
    }

    public static Packet<?> offhandMissPacket() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        return ClientPlayNetworking.createC2SPacket(OFFHAND_MISS_PACKET, buf);
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(OFFHAND_ATTACK_PACKET, (server, player, handler, buffer, sender) -> {
            int offhandAttackedEntityId = buffer.readInt();
            Entity entity = ((ServerLevel) player.level()).getEntityOrPart(offhandAttackedEntityId);
            server.execute(() -> {
                player.resetLastActionTime();
                if (entity != null) {
                    PlayerAttackHelper.mcdw$offhandAttack(player, entity);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(OFFHAND_MISS_PACKET, (server, player, handler, buffer, sender) ->
                server.execute(() -> ((IDualWielding) player).mcdw$resetLastAttackedOffhandTicks())
        );
    }
}
