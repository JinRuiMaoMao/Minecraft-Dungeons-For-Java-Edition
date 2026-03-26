package mcd_java.mcda.effects;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.api.AOECloudHelper;
import mcd_java.mcda.api.AOEHelper;
import mcd_java.mcda.api.AbilityHelper;
import mcd_java.mcda.api.CleanlinessHelper;
import mcd_java.blocks.FadingObsidianBlock;
import mcd_java.mcda.entities.SummonedBeeEntity;
import mcd_java.mcda.items.ArmorSets;
import mcd_java.mcda.mixin.PlayerTeleportationStateAccessor;
import mcd_java.mcda.registries.BlocksRegistry;
import mcd_java.mcda.registries.SoundsRegistry;
import mcd_java.mcda.registries.StatusEffectsRegistry;
import mcd_java.mcda.registries.SummonedEntityRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.ai.targeting.TargetingConditions;
import net.minecraft.entity.passive.Fox;
import net.minecraft.entity.MobEntity.AbstractIllager;
import net.minecraft.entity.MobEntity.Enemy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.FrostedIceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.ShapeContext;
import java.util.List;
import java.util.UUID;

import static mcd_java.mcda.api.CleanlinessHelper.*;
import static mcd_java.mcda.effects.ArmorEffectID.*;

public class ArmorEffects {

    public static EntityType<SummonedBeeEntity> summonedBee = SummonedEntityRegistry.SUMMONED_BEE_ENTITY;

    public static final List<StatusEffect> TITAN_SHROUD_STATUS_EFFECTS_LIST =
            List.of(StatusEffects.HUNGER, StatusEffects.NAUSEA, StatusEffects.BLINDNESS,
                    StatusEffects.DIG_SLOWDOWN, StatusEffects.SLOWNESS,
                    StatusEffects.UNLUCK, StatusEffects.WEAKNESS);

    public static final List<ItemStack> CAULDRONS_OVERFLOW_LIST =
            List.of(PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.STRENGTH),
                    PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.SWIFTNESS),
                    PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY));

    // DO NOT CHANGE THE ORDER OF THESE ARMOUR EFFECTS
    public static final List<ArmorEffectID> ARMOR_EFFECT_ID_LIST =
            List.of(MYSTERY_EFFECTS, CURIOUS_TELEPORTATION, FIRE_RESISTANCE, FLUID_FREEZING, FROST_BITE_EFFECT,
                    GHOST_KINDLING, GOURDIANS_HATRED, HASTE, HERO_OF_THE_VILLAGE, INVISIBILITY, LEADER_OF_THE_PACK,
                    LUCK, NIMBLE_TURTLE_EFFECTS, NO_FALL_DAMAGE, RENEGADES_RUSH, SHULKER_LIKE, SLOW_FALLING,
                    SPIDER_CLIMBING, SPRINTING, STALWART_BULWARK, SYLVAN_PRESENCE, WATER_BREATHING, WEB_WALKING,
                    WITHERED, GHOST_KINDLER_TRAIL);

    public static final List<ArmorEffectID> RED_ARMOR_EFFECT_ID_LIST =
            List.of(MYSTERY_EFFECTS, FIRE_RESISTANCE, GHOST_KINDLING, GOURDIANS_HATRED, LEADER_OF_THE_PACK,
                    RENEGADES_RUSH,STALWART_BULWARK, WITHERED, GHOST_KINDLER_TRAIL);

    public static final List<ArmorEffectID> GREEN_ARMOR_EFFECT_ID_LIST =
            List.of(MYSTERY_EFFECTS, HASTE, HERO_OF_THE_VILLAGE, LUCK, NO_FALL_DAMAGE, SYLVAN_PRESENCE);

    public static final List<ArmorEffectID> BLUE_ARMOR_EFFECT_ID_LIST =
            List.of(MYSTERY_EFFECTS, FLUID_FREEZING, FROST_BITE_EFFECT, NIMBLE_TURTLE_EFFECTS, SLOW_FALLING, WATER_BREATHING);

    public static final List<ArmorEffectID> PURPLE_ARMOR_EFFECT_ID_LIST =
            List.of(MYSTERY_EFFECTS, CURIOUS_TELEPORTATION, INVISIBILITY, SHULKER_LIKE, SPIDER_CLIMBING,
                    SOULDANCER_GRACE, SPRINTING, WEB_WALKING);

    public static int applyMysteryArmorEffect(LivingEntity livingEntity, ArmorSets armorSets) {
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(MYSTERY_EFFECTS))
            return 0;

        //Checks Mystery Armor Color
        if (CleanlinessHelper.checkFullArmor(livingEntity, armorSets)){

            ItemStack helmetStack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
            ItemStack chestplateStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack leggingsStack = livingEntity.getItemBySlot(EquipmentSlot.LEGS);
            ItemStack bootsStack = livingEntity.getItemBySlot(EquipmentSlot.FEET);

            int[] domArr = {0,0,0,0};

            domArr[0] = helmetStack.getOrCreateTag().getInt("dominance");
            domArr[1] = chestplateStack.getOrCreateTag().getInt("dominance");
            domArr[2] = leggingsStack.getOrCreateTag().getInt("dominance");
            domArr[3] = bootsStack.getOrCreateTag().getInt("dominance");

            switch (mcdaIndexOfLargestElementInArray(domArr)) {
                case 0: return helmetStack.getOrCreateTag().getInt("mystery_effect");
                case 1: return chestplateStack.getOrCreateTag().getInt("mystery_effect");
                case 2: return leggingsStack.getOrCreateTag().getInt("mystery_effect");
                case 3: return bootsStack.getOrCreateTag().getInt("mystery_effect");
            }
        }
        return 0;
    }

    // Effects for LivingEntityMixin
    public static void endermanLikeTeleportEffect(LivingEntity livingEntity) {
        World world = livingEntity.getWorld();

        if (!world.isClientSide) {

            if (livingEntity.isPassenger())
                livingEntity.stopRiding();

            double xPos = livingEntity.getX();
            double yPos = livingEntity.getY();
            double zPos = livingEntity.getZ();

            double teleportX;
            double teleportY;
            double teleportZ;
            int i = 0;

            do {
                // TODO TEST TELEPORTATION FOR GRANULARITY AND ACCURACY
                double xDiff = ((livingEntity.getRandom().nextDouble() / 2) + 0.5D) * 32.0D;
                double zDiff = ((livingEntity.getRandom().nextDouble() / 2) + 0.5D) * 32.0D;
                teleportX = livingEntity.getRandom().nextInt() % 2 == 0 ?
                        xPos + xDiff :
                        xPos - xDiff;
                teleportY =
                        Mth.clamp(yPos + (double) (livingEntity.getRandom().nextInt(16) - 8),
                                world.dimensionType().minY() + 5, world.getHeight() - 1);
                teleportZ = livingEntity.getRandom().nextInt() % 2 == 0 ?
                        zPos + zDiff :
                        zPos - zDiff;
                i++;

            } while (!livingEntity.randomTeleport(teleportX, teleportY, teleportZ, true) && i != 16);

            if (i == 16 && livingEntity.getX() == xPos && livingEntity.getY() == yPos && livingEntity.getZ() == zPos)
                return;
            SoundEvent soundEvent = livingEntity instanceof Fox ? SoundEvents.FOX_TELEPORT :
                    SoundEvents.CHORUS_FRUIT_TELEPORT;
            CleanlinessHelper.playCenteredSound(livingEntity, soundEvent, 1f, 1f);
        }
    }

    public static void controlledTeleportEffect(LivingEntity livingEntity) {
        Vec3d target = raytraceForTeleportation(livingEntity);

        if (!livingEntity.getWorld().isClientSide /*&& target != null*/) {

            if (livingEntity.isPassenger())
                livingEntity.stopRiding();

            if (livingEntity.randomTeleport(target.x, target.y, target.z, true)) {
                SoundEvent soundEvent = livingEntity instanceof Fox ? SoundEvents.FOX_TELEPORT :
                        SoundEvents.CHORUS_FRUIT_TELEPORT;
                CleanlinessHelper.playCenteredSound(livingEntity, soundEvent, 1f, 1f);
            }
        }
    }

    public static Vec3d raytraceForTeleportation(LivingEntity livingEntity) {
        World world = livingEntity.getWorld();
        Vec3d eyeVec = livingEntity.getEyePosition(0f);
        Vec3d direction = livingEntity.getViewVector(0f);
        Vec3d rayEnd = eyeVec.add(direction.x * 16, direction.y * 16, direction.z * 16);
        BlockHitResult result = world.clip(new ClipContext(eyeVec, rayEnd, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, livingEntity));

        BlockPos rayResult = result.getBlockPos().relative(result.getDirection());

        boolean positionIsFree = positionIsClear(world, rayResult);

        if (!result.isInside()) {

            while (!positionIsFree) {
                rayResult = livingEntity.getBlockPos();
                positionIsFree = positionIsClear(world, rayResult) && world.clip(new ClipContext(eyeVec,
                        Vec3.atCenterOf(rayResult.above()),
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, livingEntity)).getType() == HitResult.Type.MISS;
                if (rayResult.getY() <= -60)
                    break;
            }
        } else if (positionIsFree) {
            Vec3.atCenterOf(rayResult);
        }
        return Vec3.atCenterOf(rayResult);
    }

    private static boolean positionIsClear(World world, BlockPos pos) {
        return ((world.isEmptyBlock(pos)
                || world.getBlockState(pos).getBlock() == Blocks.DEAD_BUSH
                || world.getBlockState(pos).getBlock() == Blocks.GRASS
                || (world.getBlockState(pos).getBlock() == Blocks.TALL_GRASS && world.getBlockState(pos.above()).getBlock() == Blocks.TALL_GRASS)
                || world.getBlockState(pos).getCollisionShape(world, pos).isEmpty())
                && (world.isEmptyBlock(pos.above()) || world.getBlockState(pos.above()).getBlock() == Blocks.TALL_GRASS || world.getBlockState(pos.above()).getCollisionShape(world, pos.above()).isEmpty()));
    }

    public static void teleportationRobeTeleport(ServerPlayerEntity playerEntity) {
        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.TELEPORTATION)) {
            if (playerEntity.isShiftKeyDown()) {
                ((PlayerTeleportationStateAccessor)playerEntity).setInTeleportationState(true);
                ArmorEffects.endermanLikeTeleportEffect(playerEntity);
                if (mcdaCooldownCheck(playerEntity, 40))
                    mcdaRandomArmorDamage(playerEntity, 0.10f, 4, false);

            } else ((PlayerTeleportationStateAccessor)playerEntity).setInTeleportationState(false);
        }
    }

    public static void unstableRobeTeleport(ServerPlayerEntity playerEntity){
        if (checkFullArmor(playerEntity, ArmorSets.UNSTABLE)) {
            if (playerEntity.isShiftKeyDown()) {
                ((PlayerTeleportationStateAccessor)playerEntity).setInTeleportationState(true);
                AOECloudHelper.spawnParticleCloud(playerEntity, playerEntity, 3.0F, 0, ParticleTypes.EXPLOSION);
                AOEHelper.causeExplosion(playerEntity, playerEntity, 5, 3.0f);
                if (Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.controlledTeleportation){
                    ArmorEffects.controlledTeleportEffect(playerEntity);
                } else {
                    ArmorEffects.endermanLikeTeleportEffect(playerEntity);
                }
                if (mcdaCooldownCheck(playerEntity, 40))
                    mcdaRandomArmorDamage(playerEntity, 0.10f, 4, false);
            } else {
                ((PlayerTeleportationStateAccessor)playerEntity).setInTeleportationState(false);
            }
        }
    }

    public static void applyFluidFreezing(PlayerEntity playerEntity) {
        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.FROST)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == FLUID_FREEZING)
                || (BLUE_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.BLUE_MYSTERY)) == FLUID_FREEZING)) {

            if (!playerEntity.onGround()) return;
            BlockState frostedIceBlockState = Blocks.FROSTED_ICE.defaultBlockState();
            BlockState fadingObsidianBlockState = BlocksRegistry.FADING_OBSIDIAN.defaultBlockState();
            int i = Math.min(16, 2 + 1);
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            World world = playerEntity.getWorld();
            BlockPos playerBlockPos = playerEntity.getBlockPos();
            for (BlockPos blockPos2 : BlockPos.betweenClosed(playerBlockPos.offset(-i, -1, -i), playerBlockPos.offset(i, -1, i))) {
                if (blockPos2.closerToCenterThan(playerEntity.position(), i)) {
                    mutable.set(blockPos2.getX(), blockPos2.getY() + 1, blockPos2.getZ());
                    BlockState blockState2 = world.getBlockState(mutable);
                    // Water Transformation to Frosted Ice
                    if (blockState2.isAir()) {
                        BlockState blockState3 = world.getBlockState(blockPos2);
                        if(blockState3.getBlock().defaultBlockState()
                                == FrostedIceBlock.meltsInto()
                                && frostedIceBlockState.canSurvive(world, blockPos2)
                                && world.isUnobstructed(frostedIceBlockState, blockPos2, CollisionContext.empty())) {
                            world.setBlockAndUpdate(blockPos2, frostedIceBlockState);
                            world.scheduleTick(blockPos2, Blocks.FROSTED_ICE, Mth.nextInt(playerEntity.getRandom(), 60, 120));
                        }
                    }
                    // Lava Transformation to Crying Obsidian
                    if (blockState2.isAir()) {
                        BlockState blockState3 = world.getBlockState(blockPos2);
                        if (blockState3.getBlock().defaultBlockState()
                                == FadingObsidianBlock.getMeltedState()
                                && fadingObsidianBlockState.canSurvive(world, blockPos2)
                                && world.isUnobstructed(frostedIceBlockState, blockPos2, CollisionContext.empty())) {
                            world.playSound(null, blockPos2.getX(), blockPos2.getY(), blockPos2.getZ(), SoundEvents.LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.25f, 1.0f);
                            world.setBlockAndUpdate(blockPos2, fadingObsidianBlockState);
                            world.scheduleTick(blockPos2, BlocksRegistry.FADING_OBSIDIAN, Mth.nextInt(playerEntity.getRandom(), 60, 120));
                        }
                    }
                }
            }
        }
    }

    public static void applyThiefInvisibilityTick(PlayerEntity playerEntity) {
        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.THIEF)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == INVISIBILITY)
                || (PURPLE_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.PURPLE_MYSTERY)) == INVISIBILITY))
            playerEntity.setInvisible(playerEntity.isShiftKeyDown());
    }

    public static void applyWithered(PlayerEntity playerEntity, LivingEntity attacker) {
        if (attacker == null)
            return;
        if (!playerEntity.isAlive())
            return;

        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.WITHER)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == WITHERED)
                || (RED_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.RED_MYSTERY)) == WITHERED)) {
            attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 120, 0));
        }
    }

    public static void applyNimbleTurtleEffects(PlayerEntity playerEntity) {
        if (!playerEntity.isAlive())
            return;

        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.NIMBLE_TURTLE)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == NIMBLE_TURTLE_EFFECTS)
                || (BLUE_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.BLUE_MYSTERY)) == NIMBLE_TURTLE_EFFECTS)) {
            playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.DAMAGE_RESISTANCE, 60, 1, false,
                    false));
            playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 1, false,
                    false));
        }

    }

    public static void applyTitanShroudStatuses(LivingEntity livingEntity, LivingEntity target) {
        if (CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.TITAN)) {
            StatusEffect titanStatusEffect =
                    TITAN_SHROUD_STATUS_EFFECTS_LIST.get(livingEntity.getRandom().nextInt(TITAN_SHROUD_STATUS_EFFECTS_LIST.size()));
            target.addStatusEffect(new StatusEffectInstance(titanStatusEffect, 60, 0));
        }
    }

    public static void applyFrostBiteStatus(LivingEntity livingEntity, LivingEntity target) {
        if (CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.FROST_BITE)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(livingEntity, ArmorSets.MYSTERY)) == FROST_BITE_EFFECT)
                || (BLUE_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(livingEntity, ArmorSets.BLUE_MYSTERY)) == FROST_BITE_EFFECT)) {
            if (percentToOccur(30)) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffectsRegistry.FREEZING, 60, 0, true, true,
                        false));
            }
        }
    }

    public static void applyGourdiansHatredStatus(LivingEntity livingEntity) {
        if (!livingEntity.isAlive())
            return;

        if (CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.GOURDIAN)
                || (ARMOR_EFFECT_ID_LIST.get(ArmorEffects.applyMysteryArmorEffect(livingEntity, ArmorSets.MYSTERY)) == GOURDIANS_HATRED)
                || (RED_ARMOR_EFFECT_ID_LIST.get(ArmorEffects.applyMysteryArmorEffect( livingEntity, ArmorSets.RED_MYSTERY)) == GOURDIANS_HATRED)) {
            if (percentToOccur(15)) {
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.DAMAGE_BOOST, 200, 1));
            }
        }
    }

    public static void applyCauldronsOverflow(LivingEntity targetedEntity) {
        if (targetedEntity == null)
            return;

        if (CleanlinessHelper.checkFullArmor(targetedEntity, ArmorSets.CAULDRON)) {
            if (percentToOccur(15)) {
                ItemStack potionToDrop =
                        CAULDRONS_OVERFLOW_LIST.get(targetedEntity.getRandom().nextInt(CAULDRONS_OVERFLOW_LIST.size()));
                CleanlinessHelper.mcda$dropItem(targetedEntity, potionToDrop);
            }
        }

    }

    public static void applyCuriousTeleportationEffect(PlayerEntity playerEntity, LivingEntity target) {
        if (!playerEntity.isAlive())
            return;

        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.CURIOUS)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == CURIOUS_TELEPORTATION)
                || (PURPLE_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.PURPLE_MYSTERY)) == CURIOUS_TELEPORTATION)) {
            if (percentToOccur(10)) {
                if (percentToOccur(50))
                    endermanLikeTeleportEffect(playerEntity);
                else
                    endermanLikeTeleportEffect(target);
            }
        }
    }

    public static void applyGhostKindlingEffect(LivingEntity livingEntity, LivingEntity target) {
        if (CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.GHOST_KINDLER)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(livingEntity, ArmorSets.MYSTERY)) == GHOST_KINDLING)
                || (RED_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(livingEntity, ArmorSets.RED_MYSTERY)) == GHOST_KINDLING)) {
            target.setSecondsOnFire(4);
        }
    }

    public static void applySylvanPresence(LivingEntity livingEntity) {
        World world = livingEntity.getWorld();

        if (world.getGameTime() % 20 != 0)
            return;
        if (!livingEntity.isShiftKeyDown())
            return;

        if (CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.VERDANT)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(livingEntity, ArmorSets.MYSTERY)) == SYLVAN_PRESENCE)
                || (GREEN_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(livingEntity, ArmorSets.GREEN_MYSTERY)) == SYLVAN_PRESENCE)) {
            int size = Math.min(16, 2 + 1);
            BlockPos.MutableBlockPos mutablePosition = new BlockPos.MutableBlockPos();
            BlockPos blockPos = livingEntity.getBlockPos();

            for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-size, 0, -size), blockPos.offset(size, 0, size))) {
                if (blockPos2.closerToCenterThan(livingEntity.position(), size)) {
                    mutablePosition.set(blockPos2.getX(), blockPos2.getY() + 1, blockPos2.getZ());
                    BlockState checkstate = world.getBlockState(blockPos2);
                    if (checkstate.getBlock() instanceof BonemealableBlock fertilizable) {
                        if (fertilizable.isValidBonemealTarget(world, blockPos2, checkstate, world.isClientSide)) {
                            if (world instanceof ServerWorld) {
                                if (fertilizable.isBonemealSuccess(world, world.random, blockPos2, checkstate)) {
                                    fertilizable.performBonemeal((ServerWorld) world, world.random, blockPos2, checkstate);
                                    AOEHelper.addParticlesToBlock((ServerWorld) world, blockPos2,
                                            ParticleTypes.HAPPY_VILLAGER);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void applyEmberJumpEffect(LivingEntity livingEntity) {
        if (!CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.EMBER))
            return;
        if (!livingEntity.isShiftKeyDown())
            return;

        boolean playFireSound = false;

        for (LivingEntity nearbyEntity : AOEHelper.getAoeTargets(livingEntity, livingEntity, 6.0f)) {
            if (nearbyEntity instanceof Enemy){
                nearbyEntity.setSecondsOnFire(5);
                playFireSound = true;
            }
        }
        if (playFireSound) {
            if (mcdaCooldownCheck(livingEntity, 40))
                mcdaRandomArmorDamage(livingEntity, 0.10f, 3, true);
            CleanlinessHelper.playCenteredSound(livingEntity, SoundEvents.BLAZE_SHOOT, 1f, 1f);
        }
    }

    public static void applySplendidAoEAttackEffect(LivingEntity livingEntity, LivingEntity target) {
        if (!CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.SPLENDID))
            return;

        if (percentToOccur(30)) {

            for (LivingEntity nearbyEntity : AOEHelper.getAoeTargets(target, livingEntity, 6.0f)){
                float damageToBeDone = (float) livingEntity.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
                if (nearbyEntity instanceof AbstractIllager){
                    damageToBeDone = damageToBeDone * 1.5f;
                }
                if (nearbyEntity instanceof Enemy && nearbyEntity != target){
                    nearbyEntity.hurt(target.getWorld().damageSources().generic(), damageToBeDone);

                    CleanlinessHelper.playCenteredSound(nearbyEntity, SoundEvents.VEX_CHARGE, 1f, 1f);
                    AOEHelper.addParticlesToBlock((ServerWorld) nearbyEntity.getWorld(), nearbyEntity.getBlockPos(), ParticleTypes.ENCHANTED_HIT);
                }
            }
        }
    }

    public static float gildedHeroDamageBuff(LivingEntity livingEntity, LivingEntity target) {
        if ((CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.GILDED))) {
            float gildedDamage =
                    (float) livingEntity.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
            if (target instanceof AbstractIllager && livingEntity.hasEffect(StatusEffects.HERO_OF_THE_VILLAGE))
                return gildedDamage * 0.5f;
        }
        return 0;
    }

    public static float archersProwessDamageBuff(LivingEntity livingEntity) {
        if ((CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.ARCHER)))
            return 1.5f;
        return 1f;
    }

    public static float leaderOfThePackEffect(DamageSource source) {
        if (!(source.getDirectEntity() instanceof TameableEntity petSrc) || !(petSrc.getWorld() instanceof ServerWorld serverWorld))
            return 1f;
        if (!(petSrc.getOwner() instanceof PlayerEntity owner))
            return 1f;

        if (CleanlinessHelper.checkFullArmor(owner, ArmorSets.BLACK_WOLF)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(owner, ArmorSets.MYSTERY)) == LEADER_OF_THE_PACK)
                || (RED_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(owner, ArmorSets.RED_MYSTERY)) == LEADER_OF_THE_PACK)) {
            UUID petOwnerUUID = owner.getUUID();

            if (petOwnerUUID != null)
                if (serverWorld.getEntity(petOwnerUUID) instanceof LivingEntity)
                    return 1.5f;
        }
        return 1f;
    }

    public static boolean souldancerGraceEffect(PlayerEntity playerEntity) {
        if (!playerEntity.isAlive())
            return false;
        if (!CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.SOULDANCER))
            return false;

        if (percentToOccur(30)) {
            // Dodge the damage
            CleanlinessHelper.playCenteredSound(playerEntity, SoundsRegistry.DODGE_SOUND_EVENT, 1f, 1f);
            AOECloudHelper.spawnParticleCloud(playerEntity, playerEntity, 0.5F, 0, ParticleTypes.CLOUD);
            // Apply Speed after dodge
            playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.MOVEMENT_SPEED, 42, 0, false, false));
            return true;
        }
        return  false;
    }

    public static boolean gildedGloryTotemEffect(LivingEntity livingEntity) {
        if (CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.GILDED)
                && livingEntity.hasEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {

            int index = mcdaFindHighestDurabilityEquipment(livingEntity);
            ArmorItem.Type equipmentSlot = switch (index) {
                case 0 -> ArmorItem.Type.BOOTS;
                case 1 -> ArmorItem.Type.LEGGINGS;
                case 2 -> ArmorItem.Type.CHESTPLATE;
                case 3 -> ArmorItem.Type.HELMET;
                // Never reached but make Java happy
                default -> throw new IllegalStateException("Unexpected value: " + index);
            };
            mcdaDamageEquipment(livingEntity, equipmentSlot, 0.5f);
            CleanlinessHelper.onTotemDeathEffects(livingEntity);
            return true;
        }
        return false;
    }

    public static void buzzyHiveEffect(LivingEntity targetedEntity) {
        int beeSummonChance = 0;
        if (CleanlinessHelper.checkFullArmor(targetedEntity, ArmorSets.BEEHIVE))
            beeSummonChance = 30;
        if (CleanlinessHelper.checkFullArmor(targetedEntity, ArmorSets.BEENEST))
            beeSummonChance = 10;

        if (beeSummonChance == 0)
            return;
        if (percentToOccur(beeSummonChance)) {
            World world = targetedEntity.getWorld();
            SummonedBeeEntity summonedBeeEntity = summonedBee.create(world);
            if (summonedBeeEntity != null) {
                summonedBeeEntity.setSummoner(targetedEntity);
                summonedBeeEntity.moveTo(targetedEntity.getX(), targetedEntity.getY() + 1, targetedEntity.getZ(), 0, 0);
                world.spawnEntity(summonedBeeEntity);
            }
        }
    }

    public static boolean spiderClimbing(PlayerEntity playerEntity) {
        return playerEntity.horizontalCollision
                && (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.SPIDER)
                || (ARMOR_EFFECT_ID_LIST.get(ArmorEffects.applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == SPIDER_CLIMBING)
                || (ArmorEffects.PURPLE_ARMOR_EFFECT_ID_LIST.get(ArmorEffects.applyMysteryArmorEffect(playerEntity, ArmorSets.PURPLE_MYSTERY)) == SPIDER_CLIMBING));
    }

    public static boolean ruggedClimbing(PlayerEntity playerEntity){
        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.RUGGED_CLIMBING_GEAR)){
            // If Statement provided by Apace100; Thanks, Apace!
            if (mcdaBoundingBox(playerEntity, 0.01f)
                    || mcdaBoundingBox(playerEntity, -0.01f)) {
                playerEntity.setOnGround(true);
                playerEntity.resetFallDistance();

                double f = 0.1D;
                double x = Mth.clamp(playerEntity.getDeltaMovement().x, -f, f);
                double z = Mth.clamp(playerEntity.getDeltaMovement().z, -f, f);
                double y = Math.max(playerEntity.getDeltaMovement().y, -f);

                if (y < 0.0D && !playerEntity.getFeetBlockState().is(Blocks.SCAFFOLDING) && playerEntity.isShiftKeyDown()) {
                    y = 0.0D;
                } else if (playerEntity.horizontalCollision
                        && !playerEntity.getFeetBlockState().is(Blocks.SCAFFOLDING)
                        && !playerEntity.getFeetBlockState().is(Blocks.VINE)) {
                    x /= 3.5D;
                    y = f/2;
                    z /= 3.5D;
                }
                playerEntity.setDeltaMovement(x, y, z);
                return true;
            }
        }
        return false;
    }

    public static float arcticFoxesHighGround(LivingEntity livingEntity){
        if (CleanlinessHelper.checkFullArmor(livingEntity, ArmorSets.ARCTIC_FOX)) {
            if (livingEntity.getDeltaMovement().y < 0
                    && !livingEntity.onGround()
                    && !livingEntity.isSuppressingSlidingDownLadder())
                return 1.2f;
        }
        return 1f;
    }

    public static void ghostKindlerTrail(PlayerEntity playerEntity, BlockPos blockPos){
        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.GHOST_KINDLER)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == GHOST_KINDLER_TRAIL)
                || (RED_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.RED_MYSTERY)) == GHOST_KINDLER_TRAIL)) {

            for (LivingEntity nearbyEntity : AOEHelper.getAoeTargets(playerEntity, playerEntity, 3.0f)){
                if (nearbyEntity instanceof Enemy){
                    if (blockPos.relative(playerEntity.getMotionDirection().getOpposite()).closerToCenterThan(nearbyEntity.position(), 3)) {
                        nearbyEntity.setSecondsOnFire(5);
                        AOEHelper.addParticlesToBlock((ServerWorld) playerEntity.getWorld(), playerEntity.getBlockPos(),
                                ParticleTypes.FLAME);
                    }
                }
            }
        }
    }

    public static void foxPouncing(PlayerEntity playerEntity){
        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.FOX)
                || CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.ARCTIC_FOX)) {
            if (!mcdaCheckHorizontalVelocity(playerEntity.getDeltaMovement(), 0, true))
                return;
            if (!playerEntity.isShiftKeyDown() || !playerEntity.onGround())
                return;

            LivingEntity target = playerEntity.getWorld().getNearestEntity(
                    AbilityHelper.getPotentialPounceTargets(playerEntity, 6.0f),
                    TargetingConditions.DEFAULT,
                    playerEntity,
                    playerEntity.getX(),
                    playerEntity.getY(),
                    playerEntity.getZ());

            if (target == null) return;

            // TODO Look into changing out brute force box to EntityDimensions#getBoxAt?
            if (mcdaCanTargetEntity(playerEntity, target)){

                Vec3d vecHorTargetDist = new Vec3((target.getX() - playerEntity.getX()),
                        (target.getY() - playerEntity.getY()),(target.getZ() - playerEntity.getZ()));
                Vec3d vecVelHorTargetDist = vecHorTargetDist.normalize().scale(vecHorTargetDist.horizontalDistance()/6);

                playerEntity.setDeltaMovement(vecVelHorTargetDist.x + target.getDeltaMovement().x, 0.8,
                        vecVelHorTargetDist.z + target.getDeltaMovement().z);
                // Thanks Apace!
                playerEntity.hurtMarked = true;
                // Somehow make the PlayerEntity model move like the fox does?
            }
        }
    }

    // Effects for ServerPlayerEntityMixin
    public static void applyFireResistance(ServerPlayerEntity playerEntity) {
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(FIRE_RESISTANCE))
            return;

        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.SPROUT) || CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.LIVING_VINES)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == FIRE_RESISTANCE)
                || (RED_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.RED_MYSTERY)) == FIRE_RESISTANCE)) {
            playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 42, 1,
                    false,false));
        }
    }

    public static void applyHaste(ServerPlayerEntity playerEntity){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(HASTE))
            return;

        if (playerEntity.getY() < 32.0F) {

            if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.CAVE_CRAWLER)
                    || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == HASTE)
                    || (GREEN_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.GREEN_MYSTERY)) == HASTE)) {
                playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.DIG_SPEED, 42, 0, false, false));
            }
        }
        if (playerEntity.getY() > 100.0F) {

            if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.HIGHLAND)
                    || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == HASTE)
                    || (GREEN_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.GREEN_MYSTERY)) == HASTE)) {
                playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.DIG_SPEED, 42, 0, false, false));
            }
        }
    }

    public static void applyHeroOfTheVillage(ServerPlayerEntity playerEntity){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(HERO_OF_THE_VILLAGE))
            return;

        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.HERO)
                || (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.GILDED)
                    && Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(GILDED_HERO)
                    && playerEntity.hasEffect(StatusEffects.HERO_OF_THE_VILLAGE))
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == HERO_OF_THE_VILLAGE)
                || (GREEN_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.GREEN_MYSTERY)) == HERO_OF_THE_VILLAGE)) {
            playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 42, 0, false,
                    false));
        }
    }

    public static void applyHungerPain(ServerPlayerEntity playerEntity){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(HUNGER))
            return;

        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.HUNGRY_HORROR)) {

            playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 42, 1, false,
                    false));

            int foodLevel = playerEntity.getFoodData().getFoodLevel();

            if(foodLevel <= 18){

                if (foodLevel > 12){
                    //apply Strength 1
                    StatusEffectInstance snacky = new StatusEffectInstance(StatusEffects.DAMAGE_BOOST, 42, 0,
                            false, true);
                    playerEntity.addStatusEffect(snacky);
                } else if (foodLevel > 6){
                    //apply Strength 2
                    StatusEffectInstance tummyGrumbles = new StatusEffectInstance(StatusEffects.DAMAGE_BOOST, 42, 1,
                            false, true);
                    playerEntity.removeEffect(StatusEffects.DAMAGE_BOOST);
                    playerEntity.addStatusEffect(tummyGrumbles);
                } else {
                    //Sooner Starvation
                    playerEntity.hurt(playerEntity.getWorld().damageSources().starve(), 0.5f);
                    //apply Strength 3
                    StatusEffectInstance hungerPain = new StatusEffectInstance(StatusEffects.DAMAGE_BOOST, 42, 2,
                            false, true);
                    playerEntity.removeEffect(StatusEffects.DAMAGE_BOOST);
                    playerEntity.addStatusEffect(hungerPain);
                }
            }
        }
    }

    public static void applyInvisibility(ServerPlayerEntity playerEntity){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(INVISIBILITY))
            return;

        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.THIEF)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == INVISIBILITY)
                || (PURPLE_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.PURPLE_MYSTERY)) == INVISIBILITY)) {
            if (playerEntity.isShiftKeyDown()) {
                playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 42, 0,
                        false, false));
            }
        }
    }

    public static void applyLuck(ServerPlayerEntity playerEntity){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(LUCK))
            return;

        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.OPULENT)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == LUCK)
                || (GREEN_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.GREEN_MYSTERY)) == LUCK)) {
            playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 42, 0, false,
                    false));
        }
    }

    public static void applySprintingSpeed(ServerPlayerEntity playerEntity){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(SPRINTING))
            return;

        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.SHADOW_WALKER)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == SPRINTING)
                || (PURPLE_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.PURPLE_MYSTERY)) == SPRINTING)) {
            if (playerEntity.isSprinting()) {
                playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.MOVEMENT_SPEED, 42, 0, false,
                        false));
            }
        }
    }

    public static void applySlowFalling(ServerPlayerEntity playerEntity){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(SLOW_FALLING))
            return;

        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.PHANTOM) || CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.FROST_BITE)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == SLOW_FALLING)
                || (BLUE_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.BLUE_MYSTERY)) == SLOW_FALLING)) {
            playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 42, 0, false,
                    false));
        }
    }

    public static void applyStalwartBulwarkResistanceEffect(ServerPlayerEntity playerEntity){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(STALWART_BULWARK))
            return;

        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.STALWART_MAIL)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == STALWART_BULWARK)
                || (RED_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.RED_MYSTERY)) == STALWART_BULWARK)) {
            if (playerEntity.isShiftKeyDown()) {
                playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.DAMAGE_RESISTANCE, 42, 0, false,
                        false));
            }
        }
    }

    public static void applyWaterBreathing(ServerPlayerEntity playerEntity){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(WATER_BREATHING))
            return;

        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.GLOW_SQUID)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == WATER_BREATHING)
                || (BLUE_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.BLUE_MYSTERY)) == WATER_BREATHING)) {
            if (playerEntity.isUnderWater() || FabricLoader.getInstance().isModLoaded("origins")) {
                playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 42, 0,
                        false, false));
            }
        }
    }

    public static void applyRenegadesRushEffect(ServerPlayerEntity playerEntity){
        if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(RENEGADES_RUSH))
            return;

        if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.RENEGADE)
                || (ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.MYSTERY)) == RENEGADES_RUSH)
                || (RED_ARMOR_EFFECT_ID_LIST.get(applyMysteryArmorEffect(playerEntity, ArmorSets.RED_MYSTERY)) == RENEGADES_RUSH)) {
            if (playerEntity.isSprinting()) {
                StatusEffectInstance strength = new StatusEffectInstance(StatusEffects.DAMAGE_BOOST, 42, 2, false,
                        false);
                playerEntity.addStatusEffect(strength);
            }
        }
    }

    public static void sweetBerrySpeed(ServerPlayerEntity playerEntity){
        if (Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableArmorEffect.get(SWEET_BERRY_SPEED)) {
            if (CleanlinessHelper.checkFullArmor(playerEntity, ArmorSets.ARCTIC_FOX)) {
                if (playerEntity.getUseItem().is(Items.SWEET_BERRIES)) {
                    playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.MOVEMENT_SPEED, 200, 0, false, false));
                }
            }
        }
    }
}