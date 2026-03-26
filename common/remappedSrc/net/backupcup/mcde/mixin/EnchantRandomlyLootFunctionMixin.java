package net.backupcup.mcde.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;
import net.backupcup.mcde.MCDEnchantments;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;

@Mixin(EnchantRandomlyFunction.class)
public abstract class EnchantRandomlyLootFunctionMixin {
    @Shadow private static ItemStack addEnchantmentToStack(ItemStack stack, Enchantment enchantment, RandomSource random) {
        return null;
    }

    @Inject(method = "process", at = @At("HEAD"), cancellable = true)
    private void mcde$processBook(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
        var list = MCDEnchantments.getConfig().getCustomTreasurePool();
        if (!stack.is(Items.BOOK) || list.isEmpty()) {
            return;
        }
        var random = context.getRandom();
        var enchantment = list.get(random.nextInt(list.size()));
        cir.setReturnValue(addEnchantmentToStack(stack, enchantment, random));
    }
}
