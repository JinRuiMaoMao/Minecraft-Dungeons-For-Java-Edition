package mcd_java.mixin;

import mcd_java.api.CleanlinessHelper;
import mcd_java.effects.ArmorEffectID;
import mcd_java.effects.ArmorEffects;
import mcd_java.items.ArmorSetItem;
import mcd_java.items.ArmorSets;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;

@Mixin(LootItem.class)
public class ItemEntryMixin {

    @ModifyArgs(method = "generateLoot", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    public void mcda$onGenerateMysteryArmorLoot(Args args) {
        if (args.get(0) instanceof ItemStack stack) {
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
                    args.set(0, stack);
                }
            }
        }
    }
}
