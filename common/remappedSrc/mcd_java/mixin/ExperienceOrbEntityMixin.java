package mcd_java.mixin;

import mcd_java.Mcda;
import mcd_java.api.CleanlinessHelper;
import mcd_java.api.McdaEnchantmentHelper;
import mcd_java.enchants.EnchantID;
import mcd_java.items.ArmorSets;
import mcd_java.registries.EnchantsRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static mcd_java.effects.ArmorEffectID.SOULDANCER_EXPERIENCE;
import static mcd_java.enchants.EnchantID.BAG_OF_SOULS;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbEntityMixin extends Entity {

    public ExperienceOrbEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyArgs(method = "onPlayerCollision", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ExperienceOrbEntity;repairPlayerGears(Lnet/minecraft/entity/player/PlayerEntity;I)I"))
    public void mcda$modifyExperience(Args args){
        int amount = args.get(1);
        Player playerEntity = args.get(0);

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
        args.set(1, amount);
    }
}