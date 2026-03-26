package mcd_java.mcda.mixin;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.api.CleanlinessHelper;
import mcd_java.mcda.api.McdaEnchantmentHelper;
import mcd_java.mcda.enchants.EnchantID;
import mcd_java.mcda.items.ArmorSets;
import mcd_java.mcda.registries.EnchantsRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static mcd_java.mcda.effects.ArmorEffectID.SOULDANCER_EXPERIENCE;
import static mcd_java.mcda.enchants.EnchantID.BAG_OF_SOULS;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbEntityMixin extends Entity {

    public ExperienceOrbEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyArgs(method = "onPlayerCollision", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ExperienceOrbEntity;repairPlayerGears(Lnet/minecraft/entity/PlayerEntity/PlayerEntity;I)I"))
    public void mcda$modifyExperience(Args args){
        int amount = args.get(1);
        PlayerEntity playerEntity = args.get(0);

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