package net.backupcup.mcde.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class Choice {
    private final SlotPosition choicePos;
    private final EnchantmentSlot enchantmentSlot;

    public Choice(EnchantmentSlot enchantmentSlot, SlotPosition choicePos) {
        this.choicePos = choicePos;
        this.enchantmentSlot = enchantmentSlot;
    }

    public int getLevel() {
        return enchantmentSlot.getLevel();
    }

    public boolean isMaxedOut() {
        return enchantmentSlot.isMaxedOut();
    }

    public boolean isChosen() {
        return enchantmentSlot.getChosenPosition().map(choicePos::equals).orElse(false) &&
            enchantmentSlot.getLevel() > 0;
    }

    public EnchantmentSlot getEnchantmentSlot() {
        return enchantmentSlot;
    }

    public SlotPosition getChoicePosition() {
        return choicePos;
    }

    public int ordinal() {
        return choicePos.ordinal();
    }

    public ResourceLocation getEnchantmentId() {
        return enchantmentSlot.getChoice(choicePos).get();
    }

    public Enchantment getEnchantment() {
        return BuiltInRegistries.ENCHANTMENT.get(enchantmentSlot.getChoice(choicePos).get());
    }
}
