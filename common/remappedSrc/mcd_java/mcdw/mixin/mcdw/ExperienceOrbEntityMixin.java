
/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.damagesources.OffHandDamageSource;
import mcd_java.mcdw.effects.EnchantmentEffects;
import mcd_java.mcdw.enums.EnchantmentsID;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({ExperienceOrb.class})
public class ExperienceOrbEntityMixin {

    @Unique
    private Player playerEntity;

    @Unique
    public void mcdw$setPlayerEntity(Player playerEntity) {
        this.playerEntity = playerEntity;
    }

    @Unique
    public Player mcdw$getPlayerEntity() { return this.playerEntity; }

    @ModifyArgs(method = "onPlayerCollision", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ExperienceOrbEntity;repairPlayerGears(Lnet/minecraft/entity/player/PlayerEntity;I)I"))
    public void mcdw$ModifyExperience(Args args){
        Player playerEntity = args.get(0);
        mcdw$setPlayerEntity(playerEntity);
        int amount = args.get(1);

        if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.SOUL_DEVOURER).mcdw$getIsEnabled())
            amount = EnchantmentEffects.soulDevourerExperience(playerEntity, amount);

        args.set(1, amount);
    }

   @ModifyArg(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExperience(I)V"))
    public int mcdw$RepairPlayer(int experience){

       Player playerEntity = mcdw$getPlayerEntity();
       boolean isOffHandAttack = playerEntity.getLastDamageSource() instanceof OffHandDamageSource;

        if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.ANIMA_CONDUIT).mcdw$getIsEnabled())
            return EnchantmentEffects.animaConduitExperience(playerEntity, experience, isOffHandAttack);
        return experience;
    }

}