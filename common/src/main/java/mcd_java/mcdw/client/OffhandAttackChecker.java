/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.client;

import mcd_java.mcdw.configs.CompatibilityFlags;
import mcd_java.mcdw.networking.OffhandAttackPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

@Environment(EnvType.CLIENT)
public class OffhandAttackChecker {
    public static void checkForOffhandAttack() {
        if (CompatibilityFlags.noOffhandConflicts) {
            Minecraft mc = Minecraft.getInstance();
            PlayerEntity player = mc.PlayerEntity;
            if (mc.level != null
                    && mc.screen == null
                    && !mc.isPaused()
                    && PlayerEntity != null
                    && !PlayerEntity.isBlocking()) {

                if (mc.gameMode != null && mc.getConnection() != null) {
                    mc.getConnection().send(mc.hitResult instanceof EntityHitResult entityHitResult
                            ? OffhandAttackPacket.offhandAttackPacket(entityHitResult.getEntity())
                            : OffhandAttackPacket.offhandMissPacket());
                }
            }
        }
    }
}
