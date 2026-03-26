package mcd_java.mcda.effects;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.api.AOEHelper;
import mcd_java.mcda.api.BooleanHelper;
import mcd_java.mcda.api.CleanlinessHelper;
import mcd_java.mcda.registries.EnchantsRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.PlayerEntity.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static mcd_java.mcda.enchants.EnchantID.*;

public class EnchantmentEffects {
    private static final UUID RECKLESS_UUID = UUID.fromString("c131aecf-3b88-43c9-9df3-16f791077d41");

    public static final List<Item> FOOD_RESERVE_LIST = List.of(Items.APPLE, Items.BREAD, Items.COOKED_SALMON,
            Items.COOKED_PORKCHOP, Items.COOKED_MUTTON, Items.COOKED_COD, Items.COOKED_COD, Items.COOKED_RABBIT,
            Items.COOKED_CHICKEN, Items.COOKED_BEEF, Items.MELON_SLICE, Items.CARROT, Items.GOLDEN_CARROT,
            Items.GOLDEN_APPLE, Items.BAKED_POTATO);

    public static final List<ItemStack> SURPRISE_GIFT_LIST =
            List.of(PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.STRENGTH),
                    PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.SWIFTNESS),
                    PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY));

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected static boolean isInstantHealthPotion(ItemStack itemStack) {
        boolean hasInstantHealth = false;

        for (StatusEffectInstance potionEffect : PotionUtil.getMobEffects(itemStack)) {
            if (potionEffect.getEffect() == StatusEffects.HEAL) {
                hasInstantHealth = true;
                break;
            }
        }

        return hasInstantHealth;
    }

    // Effects for LivingEntityMixin
    public static void applyFireTrail(PlayerEntity player, BlockPos blockPos){
        int fireTrailLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(FIRE_TRAIL), PlayerEntity);
        if (fireTrailLevel == 0) return;

        BlockPos placeFireTrail = blockPos.relative(PlayerEntity.getMotionDirection().getOpposite(), 2);
        if (PlayerEntity.getWorld().getBlockState(placeFireTrail).isAir() && PlayerEntity.onGround() && !PlayerEntity.isShiftKeyDown()
                && BooleanHelper.isFireTrailEnabled(PlayerEntity))
            PlayerEntity.getWorld().setBlockAndUpdate(placeFireTrail, Blocks.FIRE.defaultBlockState());

    }

    public static void applyFoodReserves(PlayerEntity playerEntity) {
        if (!isInstantHealthPotion(playerEntity.getUseItem()))
            return;
        int foodReserveLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(FOOD_RESERVES), playerEntity);

        while (foodReserveLevel > 0) {
            Item foodToDrop = FOOD_RESERVE_LIST.get(playerEntity.getRandom().nextInt(FOOD_RESERVE_LIST.size()));
            ItemEntity foodDrop = new ItemEntity(playerEntity.getWorld(), playerEntity.getX(),
                    playerEntity.getY(), playerEntity.getZ(), new ItemStack(foodToDrop));
            playerEntity.getWorld().spawnEntity(foodDrop);
            foodReserveLevel--;
        }
    }

    public static void applyPotionBarrier(PlayerEntity playerEntity) {
        if (!isInstantHealthPotion(playerEntity.getUseItem()))
            return;
        int potionBarrierLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(POTION_BARRIER), playerEntity);
        if (potionBarrierLevel == 0)
            return;
        StatusEffectInstance resistance = new StatusEffectInstance(StatusEffects.DAMAGE_RESISTANCE, 60 + (20 * potionBarrierLevel), 3);
        playerEntity.addStatusEffect(resistance);
    }

    public static void applySurpriseGift(PlayerEntity playerEntity) {
        if (!isInstantHealthPotion(playerEntity.getUseItem()))
            return;
        int surpriseGiftLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(SURPRISE_GIFT), playerEntity);
        if (surpriseGiftLevel == 0) return;

        int surpriseGiftChance = 50 * surpriseGiftLevel;

        while (surpriseGiftChance > 0) {
            if (CleanlinessHelper.percentToOccur(surpriseGiftChance)) {
                ItemStack potionToDrop = SURPRISE_GIFT_LIST.get(playerEntity.getRandom().nextInt(SURPRISE_GIFT_LIST.size()));
                // TODO Find why CleanlinessHelper#mcda$dropItem method is causing an uncraftable potion to drop
                // This code causes a problem
                CleanlinessHelper.mcda$dropItem(playerEntity, potionToDrop);

                // This code works
                /*
                ItemEntity surpriseGift = new ItemEntity(playerEntity.world, playerEntity.getX(),
                        playerEntity.getY(), playerEntity.getZ(), potionToDrop);
                playerEntity.world.spawnEntity(surpriseGift);
                */
            }
            surpriseGiftChance -= 100;
        }
    }

    public static void applyLuckyExplorer(LivingEntity livingEntity){
        World world = livingEntity.getWorld();
        if (livingEntity.onGround() && world.getGameTime() % 50 == 0) {
            int luckyExplorerLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(LUCKY_EXPLORER),
                    livingEntity);
            if (luckyExplorerLevel == 0)
                return;

            float luckyExplorerThreshold = luckyExplorerLevel * 0.10f;
            float luckyExplorerRand = livingEntity.getRandom().nextFloat();

            if (luckyExplorerRand <= luckyExplorerThreshold) {
                ItemStack feetStack = livingEntity.getItemBySlot(EquipmentSlot.FEET);

                double currentXCoord = livingEntity.position().x();
                double currentZCoord = livingEntity.position().z();

                if (!feetStack.getOrCreateTag().contains("x-coord")) {
                    feetStack.getOrCreateTag().putDouble("x-coord", currentXCoord);
                    feetStack.getOrCreateTag().putDouble("z-coord", currentZCoord);
                    return;
                }

                double storedXCoord = feetStack.getOrCreateTag().getDouble("x-coord");
                double storedZCoord = feetStack.getOrCreateTag().getDouble("z-coord");

                Vec3d vec3d = new Vec3(storedXCoord, 0, storedZCoord);

                double distanceBetween = Math.sqrt(vec3d.distanceToSqr(currentXCoord, 0, currentZCoord));

                if (distanceBetween >= 100) {
                    CleanlinessHelper.mcda$dropItem(livingEntity, Items.EMERALD);

                    feetStack.getOrCreateTag().putDouble("x-coord", currentXCoord);
                    feetStack.getOrCreateTag().putDouble("z-coord", currentZCoord);
                }
            }
        }
    }

    public static float applyFireFocusDamage(LivingEntity target){
        for (LivingEntity nearbyEntity : AOEHelper.getAttackersOfEntities(target, 6.0f)) {
            int fireFocusLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(FIRE_FOCUS), nearbyEntity);
            if (fireFocusLevel > 0) {
                return 1 + (0.25F * fireFocusLevel);
            }
        }
        return 1;
    }

    public static float applyPoisonFocusDamage(LivingEntity target){
        for (LivingEntity nearbyEntity : AOEHelper.getAttackersOfEntities(target, 6.0f)) {
            int poisonFocusLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(POISON_FOCUS), nearbyEntity);
            if (poisonFocusLevel > 0) {
                return 1 + (0.25F * poisonFocusLevel);
            }
        }
        return 1;
    }

    public static void applyChilling(LivingEntity wearer, LivingEntity livingEntity){
        int chillingLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(CHILLING), wearer);
        if (chillingLevel > 0){
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * chillingLevel, chillingLevel * 2 - 1));
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.DIG_SLOWDOWN, 20 * chillingLevel, chillingLevel * 2 - 1));
        }
    }

    public static boolean deathBarterEffect(PlayerEntity playerEntity){

        int deathBarterLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(DEATH_BARTER), playerEntity);
        if (deathBarterLevel > 0) {
            Inventory playerInventory = playerEntity.getInventory();
            int emeraldTotal = 0;
            List<Integer> emeraldSlotIndices = new ArrayList<>();
            for (int slotIndex = 0; slotIndex < playerInventory.getContainerSize(); slotIndex++){
                ItemStack currentStack = playerInventory.getItem(slotIndex);
                if(currentStack.getItem() == Items.EMERALD){
                    emeraldTotal += currentStack.getCount();
                    emeraldSlotIndices.add(slotIndex);
                }
            }
            int minEmeralds = 150 / deathBarterLevel;
            if (emeraldTotal >= minEmeralds && emeraldTotal > 0) {
                for (Integer slotIndex : emeraldSlotIndices) {
                    if (minEmeralds > 0) {
                        ItemStack currEmeraldsStack = playerInventory.getItem(slotIndex);
                        int currEmeraldsCount = currEmeraldsStack.getCount();
                        int emeraldsToTake = Math.min(minEmeralds, currEmeraldsCount);
                        currEmeraldsStack.setCount(currEmeraldsCount - emeraldsToTake);
                        minEmeralds -= emeraldsToTake;
                    } else
                        break;
                }
                CleanlinessHelper.onTotemDeathEffects(playerEntity);
                return  true;
            }
        }
        return false;
    }

    // Effects for ServerPlayerEntityMixin
    public static void applyCowardice(ServerPlayerEntity player) {
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(COWARDICE))
            return;

        if (PlayerEntity.getHealth() == PlayerEntity.getMaxHealth()) {
            int cowardiceLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(COWARDICE), PlayerEntity);
            if (cowardiceLevel == 0)
                return;
            StatusEffectInstance strengthBoost = new StatusEffectInstance(StatusEffects.DAMAGE_BOOST, 42,
                    cowardiceLevel - 1, false, false);
            PlayerEntity.addStatusEffect(strengthBoost);
        }
    }

    public static void applyFrenzied(ServerPlayerEntity player) {
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(FRENZIED))
            return;

        if (PlayerEntity.getHealth() <= (0.5F * PlayerEntity.getMaxHealth())) {
            int frenziedLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(FRENZIED), PlayerEntity);
            if (frenziedLevel == 0)
                return;
            StatusEffectInstance frenzied = new StatusEffectInstance(StatusEffects.DIG_SPEED, 40, frenziedLevel - 1, false,
                    false);
            PlayerEntity.addStatusEffect(frenzied);
        }
    }

    public static void applyReckless(ServerPlayerEntity player){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(RECKLESS))
            return;

        int recklessLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(RECKLESS), PlayerEntity);
        PlayerEntity.getAttribute(EntityAttributes.MAX_HEALTH).removeModifier(RECKLESS_UUID);
        if (recklessLevel > 0) {
            if (PlayerEntity.getAttributes().hasAttribute(EntityAttributes.MAX_HEALTH)) {
                float previousMaxHealth = PlayerEntity.getMaxHealth();
                PlayerEntity.getAttribute(EntityAttributes.MAX_HEALTH).addTransientModifier(
                        new AttributeModifier(RECKLESS_UUID,
                                "reckless modifier",
                                -0.6,
                                AttributeModifier.Operation.MULTIPLY_TOTAL));
                float afterMaxHealth = PlayerEntity.getMaxHealth();
                if (afterMaxHealth != previousMaxHealth) {
                    PlayerEntity.setHealth(PlayerEntity.getHealth());
                }
            }

            StatusEffectInstance reckless = new StatusEffectInstance(StatusEffects.DAMAGE_BOOST, 40, recklessLevel - 1, false, false);
            PlayerEntity.addStatusEffect(reckless);
        }
    }

    public static void applySwiftfooted(ServerPlayerEntity player){
        int swiftfootedLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(SWIFTFOOTED),PlayerEntity);
        if (swiftfootedLevel == 0)
            return;

        StatusEffectInstance swiftfooted = new StatusEffectInstance(StatusEffects.MOVEMENT_SPEED, 60, swiftfootedLevel - 1,
                false, false);
        PlayerEntity.addStatusEffect(swiftfooted);
    }
}
