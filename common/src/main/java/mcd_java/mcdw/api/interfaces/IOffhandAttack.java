/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.api.interfaces;

import mcd_java.mcdw.api.util.PlayerAttackHelper;
import mcd_java.mcdw.client.OffhandAttackChecker;
import mcd_java.mcdw.configs.CompatibilityFlags;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IOffhandAttack {
    default TypedActionResult<ItemStack> useOffhand(World world, PlayerEntity player, Hand hand) {
        if (CompatibilityFlags.noOffhandConflicts) {
            if (hand == InteractionHand.OFF_HAND && world.isClientSide && (PlayerEntity.getOffhandItem().getItem() instanceof IOffhandAttack && PlayerAttackHelper.mixAndMatchWeapons(PlayerEntity))) {
                OffhandAttackChecker.checkForOffhandAttack();
                ItemStack offhand = PlayerEntity.getItemInHand(hand);
                return new TypedActionResult<>(InteractionResult.SUCCESS, offhand);
            }
        }
        return new TypedActionResult<>(InteractionResult.PASS, PlayerEntity.getItemInHand(hand));
    }
}
