/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw;

import mcd_java.mcdw.bases.McdwShield;
import mcd_java.mcdw.enums.ShieldsID;
import mcd_java.mcdw.registries.ItemsRegistry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemCooldowns;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(PlayerEntity.class)
public abstract class ShieldPlayerEntityMixin extends LivingEntity {
    @Shadow public abstract void incrementStat(Stat<?> stat);

    @Shadow public abstract ItemCooldowns getItemCooldownManager();

    @SuppressWarnings("unused")
    protected ShieldPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "damageShield", at = @At("HEAD"))
    private void mcdw$damageMcdwShield(float amount, CallbackInfo ci) {
        if (this.useItem.getItem() instanceof McdwShield) {
            if (!this.getWorld().isClientSide) {
                this.incrementStat(Stats.ITEM_USED.get(this.useItem.getItem()));
            }

            if (amount >= 3.0F) {
                int i = 1 + Mth.floor(amount);
                Hand hand = this.getUsedItemHand();
                this.useItem.hurtAndBreak(i, this, (Consumer<LivingEntity>) ((playerEntity) -> playerEntity.broadcastBreakEvent(hand)));
                if (this.useItem.isEmpty()) {
                    if (hand == InteractionHand.MAIN_HAND) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    } else {
                        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    }

                    this.useItem = ItemStack.EMPTY;
                    this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.getWorld().getRandom().nextFloat() * 0.4F);
                }
            }
        }
    }

    @Inject(method = "disableShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/PlayerEntity/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V"))
    public void mcdw$disableMcdwShield(boolean sprinting, CallbackInfo ci){
        this.getItemCooldownManager().addCooldown(ItemsRegistry.SHIELD_ITEMS.get(ShieldsID.SHIELD_ROYAL_GUARD).asItem(), 100);
        this.getItemCooldownManager().addCooldown(ItemsRegistry.SHIELD_ITEMS.get(ShieldsID.SHIELD_TOWER_GUARD).asItem(), 100);
        this.getItemCooldownManager().addCooldown(ItemsRegistry.SHIELD_ITEMS.get(ShieldsID.SHIELD_VANGUARD).asItem(), 100);
    }
}
