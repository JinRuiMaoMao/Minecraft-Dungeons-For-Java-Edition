package mcd_java.enchants.enchantments;

import mcd_java.Mcda;
import mcd_java.enchants.EnchantID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class BagOfSoulsEnchantment extends Enchantment {

    public BagOfSoulsEnchantment(Rarity weight, EnchantmentCategory type, EquipmentSlot... slotTypes) {
        super(weight, type, slotTypes);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isDiscoverable() {
        return Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(EnchantID.BAG_OF_SOULS)
                && Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantmentForRandomSelection.get(EnchantID.BAG_OF_SOULS);
    }

    @Override
    public boolean isTradeable() {
        return Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(EnchantID.BAG_OF_SOULS)
                && Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantmentForVillagerTrade.get(EnchantID.BAG_OF_SOULS);
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
