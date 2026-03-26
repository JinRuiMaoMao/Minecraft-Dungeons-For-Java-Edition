package mcd_java.enchants.enchantments;

import mcd_java.Mcda;
import mcd_java.api.CleanlinessHelper;
import mcd_java.enchants.EnchantID;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class RecyclerEnchantment extends Enchantment {
    public RecyclerEnchantment(Rarity weight, EnchantmentCategory type, EquipmentSlot... slotTypes) {
        super(weight, type, slotTypes);
    }

    @Override
    public void doPostHurt(LivingEntity user, Entity attacker, int level) {
        DamageSource damageSource = user.getLastDamageSource();

        if (damageSource != null && damageSource.is(DamageTypes.ARROW)) {
            if (CleanlinessHelper.percentToOccur(10 * level)) {
                CleanlinessHelper.mcda$dropItem(user, Items.ARROW);
            }
        }
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isDiscoverable() {
        return Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(EnchantID.RECYCLER)
                && Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantmentForRandomSelection.get(EnchantID.RECYCLER);
    }

    @Override
    public boolean isTradeable() {
        return Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(EnchantID.RECYCLER)
                && Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantmentForVillagerTrade.get(EnchantID.RECYCLER);
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
