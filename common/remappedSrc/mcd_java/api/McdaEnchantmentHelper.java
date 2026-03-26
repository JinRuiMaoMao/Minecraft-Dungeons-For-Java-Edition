package mcd_java.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class McdaEnchantmentHelper {
    public static int getBagOfSoulsLevel(Enchantment enchantment, Player playerEntity){
        int totalLevel = 0;
        for (ItemStack itemStack : enchantment.getSlotItems(playerEntity).values())
            totalLevel += EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemStack);

        return totalLevel;
    }
}
