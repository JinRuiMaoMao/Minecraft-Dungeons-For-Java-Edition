package mcd_java.effects;

import mcd_java.Mcda;
import mcd_java.api.AOEHelper;
import mcd_java.api.BooleanHelper;
import mcd_java.api.CleanlinessHelper;
import mcd_java.registries.EnchantsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static mcd_java.enchants.EnchantID.*;

public class EnchantmentEffects {
    private static final UUID RECKLESS_UUID = UUID.fromString("c131aecf-3b88-43c9-9df3-16f791077d41");

    public static final List<Item> FOOD_RESERVE_LIST = List.of(Items.APPLE, Items.BREAD, Items.COOKED_SALMON,
            Items.COOKED_PORKCHOP, Items.COOKED_MUTTON, Items.COOKED_COD, Items.COOKED_COD, Items.COOKED_RABBIT,
            Items.COOKED_CHICKEN, Items.COOKED_BEEF, Items.MELON_SLICE, Items.CARROT, Items.GOLDEN_CARROT,
            Items.GOLDEN_APPLE, Items.BAKED_POTATO);

    public static final List<ItemStack> SURPRISE_GIFT_LIST =
            List.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.STRENGTH),
                    PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.SWIFTNESS),
                    PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY));

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected static boolean isInstantHealthPotion(ItemStack itemStack) {
        boolean hasInstantHealth = false;

        for (MobEffectInstance potionEffect : PotionUtils.getMobEffects(itemStack)) {
            if (potionEffect.getEffect() == MobEffects.HEAL) {
                hasInstantHealth = true;
                break;
            }
        }

        return hasInstantHealth;
    }

    // Effects for LivingEntityMixin
    public static void applyFireTrail(Player player, BlockPos blockPos){
        int fireTrailLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(FIRE_TRAIL), player);
        if (fireTrailLevel == 0) return;

        BlockPos placeFireTrail = blockPos.relative(player.getMotionDirection().getOpposite(), 2);
        if (player.level().getBlockState(placeFireTrail).isAir() && player.onGround() && !player.isShiftKeyDown()
                && BooleanHelper.isFireTrailEnabled(player))
            player.level().setBlockAndUpdate(placeFireTrail, Blocks.FIRE.defaultBlockState());

    }

    public static void applyFoodReserves(Player playerEntity) {
        if (!isInstantHealthPotion(playerEntity.getUseItem()))
            return;
        int foodReserveLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(FOOD_RESERVES), playerEntity);

        while (foodReserveLevel > 0) {
            Item foodToDrop = FOOD_RESERVE_LIST.get(playerEntity.getRandom().nextInt(FOOD_RESERVE_LIST.size()));
            ItemEntity foodDrop = new ItemEntity(playerEntity.level(), playerEntity.getX(),
                    playerEntity.getY(), playerEntity.getZ(), new ItemStack(foodToDrop));
            playerEntity.level().addFreshEntity(foodDrop);
            foodReserveLevel--;
        }
    }

    public static void applyPotionBarrier(Player playerEntity) {
        if (!isInstantHealthPotion(playerEntity.getUseItem()))
            return;
        int potionBarrierLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(POTION_BARRIER), playerEntity);
        if (potionBarrierLevel == 0)
            return;
        MobEffectInstance resistance = new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60 + (20 * potionBarrierLevel), 3);
        playerEntity.addEffect(resistance);
    }

    public static void applySurpriseGift(Player playerEntity) {
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
        Level world = livingEntity.level();
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

                Vec3 vec3d = new Vec3(storedXCoord, 0, storedZCoord);

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
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * chillingLevel, chillingLevel * 2 - 1));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20 * chillingLevel, chillingLevel * 2 - 1));
        }
    }

    public static boolean deathBarterEffect(Player playerEntity){

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
    public static void applyCowardice(ServerPlayer player) {
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(COWARDICE))
            return;

        if (player.getHealth() == player.getMaxHealth()) {
            int cowardiceLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(COWARDICE), player);
            if (cowardiceLevel == 0)
                return;
            MobEffectInstance strengthBoost = new MobEffectInstance(MobEffects.DAMAGE_BOOST, 42,
                    cowardiceLevel - 1, false, false);
            player.addEffect(strengthBoost);
        }
    }

    public static void applyFrenzied(ServerPlayer player) {
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(FRENZIED))
            return;

        if (player.getHealth() <= (0.5F * player.getMaxHealth())) {
            int frenziedLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(FRENZIED), player);
            if (frenziedLevel == 0)
                return;
            MobEffectInstance frenzied = new MobEffectInstance(MobEffects.DIG_SPEED, 40, frenziedLevel - 1, false,
                    false);
            player.addEffect(frenzied);
        }
    }

    public static void applyReckless(ServerPlayer player){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(RECKLESS))
            return;

        int recklessLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(RECKLESS), player);
        player.getAttribute(Attributes.MAX_HEALTH).removeModifier(RECKLESS_UUID);
        if (recklessLevel > 0) {
            if (player.getAttributes().hasAttribute(Attributes.MAX_HEALTH)) {
                float previousMaxHealth = player.getMaxHealth();
                player.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(
                        new AttributeModifier(RECKLESS_UUID,
                                "reckless modifier",
                                -0.6,
                                AttributeModifier.Operation.MULTIPLY_TOTAL));
                float afterMaxHealth = player.getMaxHealth();
                if (afterMaxHealth != previousMaxHealth) {
                    player.setHealth(player.getHealth());
                }
            }

            MobEffectInstance reckless = new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, recklessLevel - 1, false, false);
            player.addEffect(reckless);
        }
    }

    public static void applySwiftfooted(ServerPlayer player){
        int swiftfootedLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.enchants.get(SWIFTFOOTED),player);
        if (swiftfootedLevel == 0)
            return;

        MobEffectInstance swiftfooted = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, swiftfootedLevel - 1,
                false, false);
        player.addEffect(swiftfooted);
    }
}
