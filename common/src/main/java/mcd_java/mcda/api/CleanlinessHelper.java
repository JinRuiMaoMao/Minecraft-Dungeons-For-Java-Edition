package mcd_java.mcda.api;

import mcd_java.mcda.items.ArmorSetItem;
import mcd_java.mcda.items.ArmorSets;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class CleanlinessHelper {
    @SuppressWarnings("deprecation")
    public static Random random = Random.createThreadSafe();

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
        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 900, 1));
        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
        livingEntity.getWorld().broadcastEntityEvent(livingEntity, (byte) 35);
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

        long currentTime = livingEntity.getWorld().getGameTime();

        if (!chestStack.getOrCreateTag().contains("time-check")) {
            chestStack.getOrCreateTag().putLong("time-check", currentTime);
            return false;
        }
        long storedTime = chestStack.getOrCreateTag().getLong("time-check");

        return Math.abs(currentTime - storedTime) > ticks;
    }

    public static boolean mcdaBoundingBox(PlayerEntity playerEntity, float boxSize) {
        return playerEntity.getWorld().getBlockCollisions(playerEntity,
                playerEntity.getBoundingBox().move(boxSize * playerEntity.getBoundingBox().getXsize(), 0,
                        boxSize * playerEntity.getBoundingBox().getZsize())).iterator().hasNext();
    }

    public static boolean mcdaCanTargetEntity(PlayerEntity playerEntity, Entity target){
        Vec3d playerVec = playerEntity.getViewVector(0f);
        Vec3d vecHorTargetDist = new Vec3((target.getX() - playerEntity.getX()),
                (target.getY() - playerEntity.getY()),(target.getZ() - playerEntity.getZ()));
        double horTargetDist = vecHorTargetDist.horizontalDistance();
        Vec3d perpHorTargetDist =
                vecHorTargetDist.normalize().cross(new Vec3(0, 1, 0));
        Vec3d leftSideVec =
                vecHorTargetDist.normalize().scale(horTargetDist)
                        .add(perpHorTargetDist.scale(target.getBbWidth()));
        Vec3d rightSideVec =
                vecHorTargetDist.normalize().scale(horTargetDist)
                        .add(perpHorTargetDist.scale(-target.getBbWidth()));
        double playerEyeHeight = playerEntity.getEyeY() - playerEntity.getBlockPos().getY();

                return Math.max(leftSideVec.normalize().x, rightSideVec.normalize().x) >= playerVec.normalize().x
                && Math.min(leftSideVec.normalize().x, rightSideVec.normalize().x) <= playerVec.normalize().x
                && Math.max(leftSideVec.normalize().z, rightSideVec.normalize().z) >= playerVec.normalize().z
                && Math.min(leftSideVec.normalize().z, rightSideVec.normalize().z) <= playerVec.normalize().z
                && playerVec.y > -Math.atan(playerEyeHeight / horTargetDist)
                && playerVec.y < Math.atan((target.getBbHeight() - playerEyeHeight) / horTargetDist);
    }

    public static boolean mcdaCheckHorizontalVelocity(Vec3d vec3d, double magnitude, boolean equality) {
        double horVelocity = vec3d.horizontalDistance();
        if (equality)
            return horVelocity == magnitude;
        return horVelocity > magnitude;
    }

    public static boolean percentToOccur (int chance) {
        return random.nextInt(100) < chance;
    }

    public static void playCenteredSound (LivingEntity center, SoundEvent soundEvent, float volume, float pitch) {
        center.getWorld().playSound(null,
                center.getX(), center.getY(), center.getZ(),
                soundEvent, SoundCategory.PLAYERS,
                volume, pitch);
    }

    public static void mcda$dropItem(LivingEntity le, Item item) {
        mcda$dropItem(le, item, 1);
    }

    public static void mcda$dropItem(LivingEntity le, ItemStack itemStack) {
        ItemEntity it = new ItemEntity(
                le.getWorld(), le.getX(), le.getY(), le.getZ(),
                itemStack);
        le.getWorld().spawnEntity(it);
    }

    public static void mcda$dropItem(LivingEntity le, Item item, int amount) {
        mcda$dropItem(le, new ItemStack(item, amount));
    }
}
