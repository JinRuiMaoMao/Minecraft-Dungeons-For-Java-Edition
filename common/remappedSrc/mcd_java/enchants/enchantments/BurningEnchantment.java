package mcd_java.enchants.enchantments;

import mcd_java.Mcda;
import mcd_java.enchants.EnchantID;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import java.util.Map.Entry;

public class BurningEnchantment extends Enchantment {

    public BurningEnchantment(Rarity weight, EnchantmentCategory type, EquipmentSlot... slotTypes) {
        super(weight, type, slotTypes);
    }

    @Override
    public void doPostHurt(LivingEntity user, Entity attacker, int level) {
        RandomSource random = user.getRandom();
        Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(BurningEnchantment.this, user);
        if (shouldDamageAttacker(level, random)) {
            if (attacker != null) {
                if (!attacker.isOnFire()) {
                    attacker.setSecondsOnFire(3 * level);
                }
            }
        }
    }

    public static boolean shouldDamageAttacker(int level, RandomSource random){
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
