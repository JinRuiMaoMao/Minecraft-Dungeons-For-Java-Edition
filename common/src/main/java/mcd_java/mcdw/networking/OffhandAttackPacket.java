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
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.Entity;

@SuppressWarnings("deprecation")
public class OffhandAttackPacket {

    public static final Identifier OFFHAND_ATTACK_PACKET = new Identifier(Mcdw.MOD_ID, "offhand_attack_entity");
    public static final Identifier OFFHAND_MISS_PACKET = new Identifier(Mcdw.MOD_ID, "offhand_miss_entity");

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
        ServerPlayNetworking.registerGlobalReceiver(OFFHAND_ATTACK_PACKET, (server, PlayerEntity, handler, buffer, sender) -> {
            int offhandAttackedEntityId = buffer.readInt();
            Entity entity = ((ServerWorld) PlayerEntity.getWorld()).getEntityOrPart(offhandAttackedEntityId);
            server.execute(() -> {
                PlayerEntity.resetLastActionTime();
                if (entity != null) {
                    PlayerAttackHelper.mcdw$offhandAttack(PlayerEntity, entity);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(OFFHAND_MISS_PACKET, (server, PlayerEntity, handler, buffer, sender) ->
                server.execute(() -> ((IDualWielding) PlayerEntity).mcdw$resetLastAttackedOffhandTicks())
        );
    }
}
