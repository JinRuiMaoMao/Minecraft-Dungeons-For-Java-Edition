/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw;

import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.enums.SwordsID;
import mcd_java.mcdw.registries.ItemsRegistry;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();
    @Shadow public abstract int getDamage();
    @Shadow public abstract int getMaxDamage();
    @Shadow public abstract ListTag getEnchantments();
    
    // When the Mechanised Sawblade breaks, it "becomes" the Broken Sawblade
    @Inject(at = @At("HEAD"), method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V")
    public <T extends LivingEntity> void mcdw$damage(int amount, T entity, Consumer<T> breakCallback, CallbackInfo ci) {
        ItemStack itemStack = this.getItem().getDefaultInstance();
        if (itemStack.getItem() == ItemsRegistry.SWORD_ITEMS.get(SwordsID.SWORD_MECHANIZED_SAWBLADE) && getDamage() + amount >= getMaxDamage()) {
            ListTag oldEnchantments = this.getEnchantments().copy();
            ItemStack brokenSawblade = new ItemStack(ItemsRegistry.SWORD_ITEMS.get(SwordsID.SWORD_BROKEN_SAWBLADE));
            int oldRepairCost = itemStack.getBaseRepairCost();
            brokenSawblade.addTagElement(ItemStack.TAG_ENCH, oldEnchantments);
            CleanlinessHelper.mcdw$dropItem(entity, brokenSawblade);
            Map<Enchantment, Integer> brokenSawbladeEnchantments = EnchantmentHelper.getEnchantments(brokenSawblade);
            brokenSawbladeEnchantments.remove(Enchantments.FIRE_ASPECT);
            EnchantmentHelper.setEnchantments(brokenSawbladeEnchantments, brokenSawblade);
            brokenSawblade.setRepairCost(oldRepairCost);
        }
    }
    
    // The enchantment table does not allow enchanting items that already have enchantments applied
    // This mixin changes items, that only got their IInnateEnchantments to still be enchantable
    @Inject(method = "isEnchantable()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasEnchantments()Z"), cancellable = true)
    public void mcdw$isEnchantable(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof IInnateEnchantment iInnateEnchantment && iInnateEnchantment.onlyHasInnateEnchantments(stack)) {
            cir.setReturnValue(true);
        }
    }
}