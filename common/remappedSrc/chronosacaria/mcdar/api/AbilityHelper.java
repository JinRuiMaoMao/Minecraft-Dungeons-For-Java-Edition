package jinrui.mcdar.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

public class AbilityHelper {

    public static boolean isPetOf(LivingEntity self, LivingEntity owner){
        if (self instanceof OwnableEntity tameable) {
            return tameable.getOwner() == owner;
        }
        return false;
    }

    private static boolean isVillagerOrIronGolem(LivingEntity nearbyEntity) {
        return (nearbyEntity instanceof Villager) || (nearbyEntity instanceof IronGolem);
    }

    public static boolean canHealEntity(LivingEntity healer, LivingEntity nearbyEntity){
        return nearbyEntity != healer
                && isAllyOf(healer, nearbyEntity)
                && nearbyEntity.isAlive()
                && healer.hasLineOfSight(nearbyEntity);
    }

    private static boolean isAllyOf(LivingEntity self, LivingEntity other) {
        return self.isAlliedTo(other)
                || isPetOf(self, other)
                || isVillagerOrIronGolem(other);
    }

    public static boolean isAoeTarget(LivingEntity self, LivingEntity attacker, LivingEntity center) {
        return self != attacker
                && self.isAlive()
                && !isAllyOf(attacker, self)
                && !isUnaffectedByAoe(self)
                && center.hasLineOfSight(self);
    }

    private static boolean isUnaffectedByAoe(LivingEntity entity) {
        if (entity instanceof Player)
            return ((Player) entity).isCreative();
        return false;
    }

    public static final List<ItemStack> SATCHEL_OF_ELIXIRS_LIST = List.of(
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.STRENGTH),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.SWIFTNESS),
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY));

    public static final List<Item> SATCHEL_OF_SNACKS_LIST = Collections.unmodifiableList(Arrays.asList(
            Items.APPLE, Items.BREAD, Items.COOKED_SALMON, Items.COOKED_PORKCHOP, Items.COOKED_MUTTON,
            Items.COOKED_COD, Items.COOKED_COD, Items.COOKED_RABBIT, Items.COOKED_CHICKEN, Items.COOKED_BEEF,
            Items.MELON_SLICE, Items.CARROT, Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.BAKED_POTATO));
}
