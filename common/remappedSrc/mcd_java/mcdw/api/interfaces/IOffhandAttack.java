/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.api.interfaces;

import mcd_java.mcdw.api.util.PlayerAttackHelper;
import mcd_java.mcdw.client.OffhandAttackChecker;
import mcd_java.mcdw.configs.CompatibilityFlags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IOffhandAttack {
    default InteractionResultHolder<ItemStack> useOffhand(Level world, Player player, InteractionHand hand) {
        if (CompatibilityFlags.noOffhandConflicts) {
            if (hand == InteractionHand.OFF_HAND && world.isClientSide && (player.getOffhandItem().getItem() instanceof IOffhandAttack && PlayerAttackHelper.mixAndMatchWeapons(player))) {
                OffhandAttackChecker.checkForOffhandAttack();
                ItemStack offhand = player.getItemInHand(hand);
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, offhand);
            }
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
    }
}
