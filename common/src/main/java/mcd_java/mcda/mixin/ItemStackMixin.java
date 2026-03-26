package mcd_java.mcda.mixin;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.api.CleanlinessHelper;
import mcd_java.mcda.effects.ArmorEffectID;
import mcd_java.mcda.effects.ArmorEffects;
import mcd_java.mcda.items.ArmorSetItem;
import mcd_java.mcda.items.ArmorSets;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "onCraft", at = @At("HEAD"))
    public void mcda$onCraftMysteryArmor(World world, PlayerEntity player, int amount, CallbackInfo ci){
        ItemStack stack = (ItemStack) (Object) this;

        if (stack.getItem() instanceof ArmorSetItem armorItem) {
            ArrayList<ArmorSets> sets = new ArrayList<>(List.of(ArmorSets.MYSTERY, ArmorSets.RED_MYSTERY,
                    ArmorSets.GREEN_MYSTERY, ArmorSets.BLUE_MYSTERY, ArmorSets.PURPLE_MYSTERY));
            ArrayList<List<ArmorEffectID>> effects = new ArrayList<>(List.of(ArmorEffects.ARMOR_EFFECT_ID_LIST,
                    ArmorEffects.RED_ARMOR_EFFECT_ID_LIST, ArmorEffects.GREEN_ARMOR_EFFECT_ID_LIST,
                    ArmorEffects.BLUE_ARMOR_EFFECT_ID_LIST, ArmorEffects.PURPLE_ARMOR_EFFECT_ID_LIST));

            if (sets.contains(armorItem.getSet())) {
                stack.getOrCreateTag().putInt("dominance", CleanlinessHelper.random.nextInt(99));
                stack.getOrCreateTag().putInt("mystery_effect",
                        CleanlinessHelper.random.nextInt(effects.get(sets.indexOf(armorItem.getSet())).size() - 1) + 1);
            }
        }
    }

    @Inject(method = "use", at = @At("HEAD"))
    private void mcda$useEmeraldToChargeArmor(World world, PlayerEntity user, Hand hand,
                                        CallbackInfoReturnable<TypedActionResult<ItemStack>> cir){
        ItemStack getMainHandStack = user.getMainHandItem();

        if (Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(ArmorEffectID.GILDED_HERO) && CleanlinessHelper.checkFullArmor(user, ArmorSets.GILDED)) {
            if (getMainHandStack.getItem() == Items.EMERALD && !user.hasEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
                int decrementAmount = 10;
                if (getMainHandStack.getCount() >= decrementAmount) {
                    getMainHandStack.shrink(decrementAmount);
                    StatusEffectInstance hov = new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 42, 0, false,
                            false);
                    user.addStatusEffect(hov);
                    CleanlinessHelper.playCenteredSound(user, SoundEvents.EXPERIENCE_ORB_PICKUP,0.8f, 0.8f);
                }
            }
        }
    }
}
