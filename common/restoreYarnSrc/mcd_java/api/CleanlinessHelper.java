package mcd_java.api;

import mcd_java.items.ArmorSetItem;
import mcd_java.items.ArmorSets;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class CleanlinessHelper {
    @SuppressWarnings("deprecation")
    public static RandomSource random = RandomSource.createThreadSafe();

    @SuppressWarnings("ConstantConditions")
    public static boolean checkFullArmor(LivingEntity livingEntity, ArmorSets armorSets) {
        for (ArmorItem.Type slot : armorSets.getSlots()) {
            if (livingEntity.getItemBySlot(slot.getSlot()).getItem() instanceof ArmorSetItem armorItem) {
                if (!armorItem.getSet().isOf(armorSets)) // If it is an armor item and is of the set, we don't return false
                    return false;
            } else return false;
        }
        return true;
    }

    public static void onTotemDeathEffects(LivingEntity livingEntity) {
        livingEntity.setHealth(1.0F);
        livingEntity.removeAllEffects();
        livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        livingEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 900, 1));
        livingEntity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        livingEntity.level().broadcastEntityEvent(livingEntity, (byte) 35);
    }

    public static int mcdaIndexOfLargestElementInArray(int[] arr) {
        int maxVal = arr[0];
        int index = 0;

        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > maxVal) {
                maxVal = arr[i];
                index = i;
            }
        }
        return index;
    }

    public static int mcdaFindHighestDurabilityEquipment(LivingEntity livingEntity) {
        int i = 0;
        int[] armorPieceDurability = {0, 0, 0, 0};

        // Store durability percents of armor
        for (ItemStack itemStack : livingEntity.getArmorSlots()) {
            float k = itemStack.getMaxDamage();
            float j = k - itemStack.getDamageValue();
            armorPieceDurability[i] = (int) ((j / k) * 100);
            i++;
        }

        // Find the highest durability armor's index
        return mcdaIndexOfLargestElementInArray(armorPieceDurability);
    }

    public static void mcdaRandomArmorDamage(LivingEntity livingEntity, float damagePercentage, int totalNumOfPieces, boolean missingBoots){
        int index = random.nextInt(totalNumOfPieces);
        if (missingBoots)
            index++;

        ArmorItem.Type equipment = switch (index){
            case 0 -> ArmorItem.Type.BOOTS;
            case 1 -> ArmorItem.Type.LEGGINGS;
            case 2 -> ArmorItem.Type.CHESTPLATE;
            case 3 -> ArmorItem.Type.HELMET;
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
        mcdaDamageEquipment(livingEntity, equipment, damagePercentage);
    }

    public static void mcdaDamageEquipment(LivingEntity livingEntity, ArmorItem.Type armorItemType, float damagePercentage) {
        ItemStack armorStack = livingEntity.getItemBySlot(armorItemType.getSlot());
        int k = armorStack.getMaxDamage();
        int j = k - armorStack.getDamageValue();
        // Necessary for proper types.
        int breakDamage = (int) (k * damagePercentage);
        boolean toBreak = j <= breakDamage;

        if (toBreak)
            armorStack.hurtAndBreak(j, livingEntity,
                    (entity) -> entity.broadcastBreakEvent(armorItemType.getSlot()));
        else
            armorStack.hurtAndBreak(breakDamage, livingEntity,
                    (entity) -> entity.broadcastBreakEvent(armorItemType.getSlot()));
    }

    public static boolean mcdaCooldownCheck(LivingEntity livingEntity, int ticks){
        ItemStack chestStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);

        long currentTime = livingEntity.level().getGameTime();

        if (!chestStack.getOrCreateTag().contains("time-check")) {
            chestStack.getOrCreateTag().putLong("time-check", currentTime);
            return false;
        }
        long storedTime = chestStack.getOrCreateTag().getLong("time-check");

        return Math.abs(currentTime - storedTime) > ticks;
    }

    public static boolean mcdaBoundingBox(Player playerEntity, float boxSize) {
        return playerEntity.level().getBlockCollisions(playerEntity,
                playerEntity.getBoundingBox().move(boxSize * playerEntity.getBoundingBox().getXsize(), 0,
                        boxSize * playerEntity.getBoundingBox().getZsize())).iterator().hasNext();
    }

    public static boolean mcdaCanTargetEntity(Player playerEntity, Entity target){
        Vec3 playerVec = playerEntity.getViewVector(0f);
        Vec3 vecHorTargetDist = new Vec3((target.getX() - playerEntity.getX()),
                (target.getY() - playerEntity.getY()),(target.getZ() - playerEntity.getZ()));
        double horTargetDist = vecHorTargetDist.horizontalDistance();
        Vec3 perpHorTargetDist =
                vecHorTargetDist.normalize().cross(new Vec3(0, 1, 0));
        Vec3 leftSideVec =
                vecHorTargetDist.normalize().scale(horTargetDist)
                        .add(perpHorTargetDist.scale(target.getBbWidth()));
        Vec3 rightSideVec =
                vecHorTargetDist.normalize().scale(horTargetDist)
                        .add(perpHorTargetDist.scale(-target.getBbWidth()));
        double playerEyeHeight = playerEntity.getEyeY() - playerEntity.blockPosition().getY();

                return Math.max(leftSideVec.normalize().x, rightSideVec.normalize().x) >= playerVec.normalize().x
                && Math.min(leftSideVec.normalize().x, rightSideVec.normalize().x) <= playerVec.normalize().x
                && Math.max(leftSideVec.normalize().z, rightSideVec.normalize().z) >= playerVec.normalize().z
                && Math.min(leftSideVec.normalize().z, rightSideVec.normalize().z) <= playerVec.normalize().z
                && playerVec.y > -Math.atan(playerEyeHeight / horTargetDist)
                && playerVec.y < Math.atan((target.getBbHeight() - playerEyeHeight) / horTargetDist);
    }

    public static boolean mcdaCheckHorizontalVelocity(Vec3 vec3d, double magnitude, boolean equality) {
        double horVelocity = vec3d.horizontalDistance();
        if (equality)
            return horVelocity == magnitude;
        return horVelocity > magnitude;
    }

    public static boolean percentToOccur (int chance) {
        return random.nextInt(100) < chance;
    }

    public static void playCenteredSound (LivingEntity center, SoundEvent soundEvent, float volume, float pitch) {
        center.level().playSound(null,
                center.getX(), center.getY(), center.getZ(),
                soundEvent, SoundSource.PLAYERS,
                volume, pitch);
    }

    public static void mcda$dropItem(LivingEntity le, Item item) {
        mcda$dropItem(le, item, 1);
    }

    public static void mcda$dropItem(LivingEntity le, ItemStack itemStack) {
        ItemEntity it = new ItemEntity(
                le.level(), le.getX(), le.getY(), le.getZ(),
                itemStack);
        le.level().addFreshEntity(it);
    }

    public static void mcda$dropItem(LivingEntity le, Item item, int amount) {
        mcda$dropItem(le, new ItemStack(item, amount));
    }
}
