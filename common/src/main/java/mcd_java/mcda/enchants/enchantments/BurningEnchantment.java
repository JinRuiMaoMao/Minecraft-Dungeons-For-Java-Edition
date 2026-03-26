package mcd_java.mcda.enchants.enchantments;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.enchants.EnchantID;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.EnchantmentHelper;
import java.util.Map.Entry;

public class BurningEnchantment extends Enchantment {

    public BurningEnchantment(Rarity weight, EnchantmentTarget type, EquipmentSlot... slotTypes) {
        super(weight, type, slotTypes);
    }

    @Override
    public void doPostHurt(LivingEntity user, Entity attacker, int level) {
        Random random = user.getRandom();
        Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(BurningEnchantment.this, user);
        if (shouldDamageAttacker(level, random)) {
            if (attacker != null) {
                if (!attacker.isOnFire()) {
                    attacker.setSecondsOnFire(3 * level);
                }
            }
        }
    }

    public static boolean shouldDamageAttacker(int level, Random random){
        if (level <= 0){
            return false;
        } else {
            return random.nextFloat() < 0.15F * (float) level;
        }
    }

    @Override
    protected boolean checkCompatibility(Enchantment other){
        return !(other instanceof ChillingEnchantment);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isDiscoverable() {
        return Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(EnchantID.BURNING)
                && Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantmentForRandomSelection.get(EnchantID.BURNING);
    }

    @Override
    public boolean isTradeable() {
        return Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(EnchantID.BURNING)
                && Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantmentForVillagerTrade.get(EnchantID.BURNING);
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
