package net.backupcup.mcde.mixin;


import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.backupcup.mcde.MCDEnchantments;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.enchantment.Enchantment;

@Mixin(VillagerTrades.EnchantBookForEmeralds.class)
public class EnchantBookFactoryMixin {
    @ModifyVariable(method = "create", at = @At("STORE"))
    private List<Enchantment> mcde$changeTrade(List<Enchantment> list) {
        var pool = MCDEnchantments.getConfig().getVillagerBookPool();
        if (pool.isEmpty()) {
            return list;
        }
        return pool;
    }
}
