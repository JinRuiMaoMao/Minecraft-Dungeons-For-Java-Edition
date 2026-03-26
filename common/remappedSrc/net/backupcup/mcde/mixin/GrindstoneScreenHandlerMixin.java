package net.backupcup.mcde.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.backupcup.mcde.MCDEnchantments;
import net.backupcup.mcde.util.EnchantmentSlot;
import net.backupcup.mcde.util.EnchantmentSlots;
import net.backupcup.mcde.util.EnchantmentUtils;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneScreenHandlerMixin extends AbstractContainerMenu {
    protected GrindstoneScreenHandlerMixin(MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Final @Shadow private Container input;
    @Final @Shadow private Container result;

    @Mixin(targets = "net.minecraft.screen.GrindstoneScreenHandler$4")
    public static abstract class Slot3 extends Slot {

        public Slot3(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Inject(method = "onTakeItem", at = @At("HEAD"))
        private void mcde$onTake(Player playerEntity, ItemStack itemStack, CallbackInfo ci) {
            MCDEnchantments.LOGGER.info("On Take");
        }

        @ModifyExpressionValue(method = "getExperience(Lnet/minecraft/item/ItemStack;)I", at = @At(target = "Lnet/minecraft/enchantment/Enchantment;isCursed()Z", value = "INVOKE"))
        private boolean mcde$isCursedOrGilding(boolean original, ItemStack itemStack, @Local Enchantment enchantment) {
            MCDEnchantments.LOGGER.info("{}'s slots: {}", itemStack.getHoverName().getString(), EnchantmentSlots.fromItemStack(itemStack));
            return original || EnchantmentUtils.isGilding(enchantment, itemStack);
        }
    }

    @ModifyReturnValue(method = "grind", at = @At("RETURN"))
    private ItemStack mcde$removeChoiceOnGrind(ItemStack itemStack, ItemStack item) {
        return EnchantmentSlots.fromItemStack(item).map(slots -> {
            for (var slot : slots) {
                slot.clearChoice();
            }
            MCDEnchantments.LOGGER.info("{}'s NBT before update: {}", itemStack.getHoverName().getString(), itemStack.getTag());
            slots.updateItemStack(itemStack);
            MCDEnchantments.LOGGER.info("{}'s NBT after update: {}", itemStack.getHoverName().getString(), itemStack.getTag());
            return itemStack;
        }).orElse(itemStack);
    }

    @ModifyVariable(method = "grind", at = @At("STORE"))
    private Map<Enchantment, Integer> mcde$isCursedOrGilding(Map<Enchantment, Integer> map, ItemStack item) {
        map = EnchantmentHelper.getEnchantments(item);
        map.entrySet().removeIf(kvp -> !kvp.getKey().isCurse() && !EnchantmentUtils.isGilding(kvp.getKey(), item));
        return map;
    }
}
