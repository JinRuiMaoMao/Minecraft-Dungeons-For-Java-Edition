package net.backupcup.mcde.mixin;

import java.util.List;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.backupcup.mcde.util.Choice;
import net.backupcup.mcde.util.EnchantmentSlot;
import net.backupcup.mcde.util.EnchantmentSlots;
import net.backupcup.mcde.util.EnchantmentUtils;
import net.minecraft.util.Formatting;
import net.minecraft.registry.Registries;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.text.MutableComponent;
import net.minecraft.util.Identifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow private NbtCompound nbt;

    @Shadow public abstract boolean hasNbt();

    @Shadow public static void appendEnchantments(List<Text> tooltip, ListTag list) { }

    @ModifyArg(
        method = "getTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;appendEnchantments(Ljava/util/List;Lnet/minecraft/nbt/NbtList;)V",
            ordinal = 0
        ),
        index = 1
    )
    private ListTag mcde$removeMcdeManagedEnchantments(ListTag original) {
        var itemStack = (ItemStack)(Object)this;
        var slotsOptional = EnchantmentSlots.fromItemStack(itemStack);
        if (slotsOptional.isEmpty()) {
            return original;
        }
        var slots = slotsOptional.get();
        var list = original.copy();
        list.removeIf(e -> slots.stream().flatMap(slot -> slot.getChosen().stream())
                .anyMatch(c -> c.getEnchantmentId().equals(EnchantmentHelper.getEnchantmentId((NbtCompound)e))));
        list.removeIf(e -> slots.getGildingIds().contains(EnchantmentHelper.getEnchantmentId((NbtCompound)e)));
        return list;
    }

    @Inject(
        method = "getTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;appendEnchantments(Ljava/util/List;Lnet/minecraft/nbt/NbtList;)V",
            ordinal = 0,
            shift = Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void mcde$appendMcdeEnchantmentLines(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> tooltip) {
        var slotsOptional = EnchantmentSlots.fromItemStack((ItemStack)(Object)this);
        if (slotsOptional.isEmpty()) {
            return;
        }
        var slots = slotsOptional.get();
        for (var slot : slots) {
            if (slot.getChosen().isPresent()) {
                var chosen =  slot.getChosen().get();
                var name = Text.translatable(chosen.getEnchantmentId().toLanguageKey("enchantment"));
                var enchantment = Registries.ENCHANTMENT.get(chosen.getEnchantmentId());
                if (enchantment.getMaxLevel() > 1) {
                    name.append(" ")
                        .append(Text.translatable("enchantment.level." + chosen.getLevel()));
                }
                tooltip.add(name.withStyle(EnchantmentUtils.formatEnchantment(chosen.getEnchantmentId())));
            }
        }
        for (var gilded : slots.getGildingIds()) {
            tooltip.add(Text.translatable("item.tooltip.gilded", Text.translatable(gilded.toLanguageKey("enchantment")))
                    .withStyle(Formatting.GOLD));
        }
    }

    @ModifyReturnValue(method = "getEnchantments", at = @At("RETURN"))
    private ListTag mcde$forceLevelOfGilding(ListTag list) {
        EnchantmentSlots.fromItemStack((ItemStack)(Object)this).ifPresent(slots -> {
            for (var e : list) {
                NbtCompound c = (NbtCompound)e;
                if (slots.getGildingIds().contains((EnchantmentHelper.getEnchantmentId(c)))) {
                    EnchantmentHelper.setEnchantmentLevel(c, 1);
                }
            }
        });
        return list;
    }
}
