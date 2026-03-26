package net.backupcup.mcde.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import java.util.Optional;
import net.backupcup.mcde.MCDEnchantments;
import net.backupcup.mcde.util.EnchantmentSlots;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@Mixin(AnvilMenu.class)
public abstract class AnvilScreenHandlerMixin extends ItemCombinerMenu {
    
    public AnvilScreenHandlerMixin(MenuType<?> type, int syncId, Inventory playerInventory,
            ContainerLevelAccess context) {
        super(type, syncId, playerInventory, context);
    }

    @Final @Shadow private DataSlot levelCost;
    @Shadow private String newItemName;

    @ModifyVariable(
        method = "updateResult",
        at = @At(value = "STORE"),
        ordinal = 0,
        slice = @Slice(
            from = @At(value = "INVOKE", target = "net/minecraft/enchantment/Enchantment.getRarity()Lnet/minecraft/enchantment/Enchantment$Rarity;"),
            to = @At(value = "INVOKE", target = "net/minecraft/item/ItemStack.getCount()I", ordinal = 1)
        )
    )
    private int mcde$adjustPrice(int original, @Local(index = 13) Enchantment enchantment, @Local(index = 15) int level, @Local(index = 17) int rarity) {
        int cost = MCDEnchantments.getConfig().getEnchantCost(EnchantmentHelper.getEnchantmentId(enchantment), level);
        return original + cost - rarity * level;
    }

    @Inject(method = "updateResult", at = @At("RETURN"))
    private void mcde$adjustResult(CallbackInfo ci) {
        var slotsOptional1 = EnchantmentSlots.fromItemStack(inputSlots.getItem(0));
        var slotsOptional2 = EnchantmentSlots.fromItemStack(inputSlots.getItem(1));
        var result = resultSlots.getItem(0);
        if (ItemStack.isSameItem(inputSlots.getItem(0), inputSlots.getItem(1)) && !MCDEnchantments.getConfig().isAnvilItemMixingAllowed()) {
            resultSlots.setItem(0, ItemStack.EMPTY);
            return;
        }
        if (inputSlots.getItem(1).is(Items.ENCHANTED_BOOK) && !MCDEnchantments.getConfig().isEnchantingWithBooksAllowed()) {
            resultSlots.setItem(0, ItemStack.EMPTY);
            return;
        }
        if (slotsOptional1.isEmpty() || slotsOptional2.isEmpty()) {
            return;
        }
        var slots1 = slotsOptional1.get();
        var slots2 = slotsOptional2.get();
        var resultMap = EnchantmentHelper.getEnchantments(result);
        resultMap.entrySet().removeIf(kvp -> 
            switch (MCDEnchantments.getConfig().getGildingMergeStrategy()) {
                case REMOVE -> slots1.hasGilding(kvp.getKey()) || slots2.hasGilding(kvp.getKey());
                case FIRST -> slots2.hasGilding(kvp.getKey());
                case SECOND -> slots1.hasGilding(kvp.getKey());
                case BOTH -> false;
            });
        EnchantmentHelper.setEnchantments(resultMap, result);
        slots1.merge(slots2).updateItemStack(result);
        resultSlots.setItem(0, result);
    }
}
