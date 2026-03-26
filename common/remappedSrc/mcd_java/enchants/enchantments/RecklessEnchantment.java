package mcd_java.enchants.enchantments;

import mcd_java.Mcda;
import mcd_java.enchants.EnchantID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class RecklessEnchantment extends Enchantment {
    public RecklessEnchantment(Rarity weight, EnchantmentCategory type, EquipmentSlot... slotTypes) {
        super(weight, type, slotTypes);
    }

    @Override
    protected boolean checkCompatibility(Enchantment other){
        return !(other instanceof CowardiceEnchantment);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isDiscoverable() {
        return Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(EnchantID.RECKLESS)
                && Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantmentForRandomSelection.get(EnchantID.RECKLESS);
    }

    @Override
    public boolean isTradeable() {
        return Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(EnchantID.RECKLESS)
                && Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantmentForVillagerTrade.get(EnchantID.RECKLESS);
    }

    @Override
    public int getMinCost(int level) {
        return 1 + level * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 5;
    }
}
