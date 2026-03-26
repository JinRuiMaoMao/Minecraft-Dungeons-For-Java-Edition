package mcd_java.registries;

import mcd_java.Mcda;
import mcd_java.api.CleanlinessHelper;
import mcd_java.api.McdaEnchantmentHelper;
import mcd_java.enchants.EnchantID;
import mcd_java.items.ArmorSets;
import com.blamejared.clumps.api.events.ClumpsEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import static mcd_java.effects.ArmorEffectID.SOULDANCER_EXPERIENCE;
import static mcd_java.enchants.EnchantID.BAG_OF_SOULS;

public class CompatRegistry {

    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("clumps")) {
            ClumpsEvents.VALUE_EVENT.register(event -> {
                int amount = event.getValue();
                Player playerEntity = event.getPlayer();

                if (Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(SOULDANCER_EXPERIENCE))
                    if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.SOULDANCER))
                        amount = (int) Math.round(1.5 * amount);

                if (Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(BAG_OF_SOULS)) {
                    int bagOfSoulsLevel = McdaEnchantmentHelper.getBagOfSoulsLevel(EnchantsRegistry.enchants.get(EnchantID.BAG_OF_SOULS),
                            playerEntity);

                    if (bagOfSoulsLevel > 0) {
                        int bagOfSoulsCount = 0;
                        for (ItemStack itemStack : playerEntity.getArmorSlots())
                            if (EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchants.get(BAG_OF_SOULS), itemStack) > 0)
                                bagOfSoulsCount++;

                        // Thank you, Amph
                        amount = (amount * (1 + (bagOfSoulsLevel / 12)) + Math.round(((bagOfSoulsLevel % 12) / 12.0f) * amount)) * bagOfSoulsCount;
                    }
                }
                event.setValue(amount);
                return null;
            });
        }
    }
}
