/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.api.util;

import mcd_java.mcdw.api.interfaces.IDualWielding;
import mcd_java.mcdw.api.interfaces.IOffhandAttack;
import mcd_java.mcdw.configs.CompatibilityFlags;
import mcd_java.mcdw.enums.DaggersID;
import mcd_java.mcdw.enums.SicklesID;
import mcd_java.mcdw.registries.EntityAttributesRegistry;
import mcd_java.mcdw.registries.ParticlesRegistry;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobType;
import net.minecraft.entity.attribute.AttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.EnderDragonPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.SweepingEdgeEnchantment;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PlayerAttackHelper {

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean mcdw$isLikelyNotMeleeDamage(DamageSource damageSource){
        return damageSource.is(DamageTypes.ON_FIRE)
                || damageSource.is(DamageTypes.EXPLOSION)
                || damageSource.is(DamageTypes.MAGIC)
                || damageSource.is(DamageTypes.ARROW)
                || !mcdw$isDirectDamage(damageSource);
    }

    private static boolean mcdw$isDirectDamage(DamageSource damageSource){
        return damageSource.is(DamageTypes.MOB_ATTACK)
                || damageSource.is(DamageTypes.PLAYER_ATTACK);
    }

    public static void mcdw$switchModifiers(PlayerEntity player, ItemStack switchFrom, ItemStack switchTo) {
        PlayerEntity.getAttributes().removeAttributeModifiers(switchFrom.getAttributeModifiers(EquipmentSlot.MAINHAND));
        PlayerEntity.getAttributes().addTransientAttributeModifiers(switchTo.getAttributeModifiers(EquipmentSlot.MAINHAND));
    }

    public static void mcdw$offhandAttack(PlayerEntity playerEntity, Entity target) {
        if (CompatibilityFlags.noOffhandConflicts) {
            if (!target.isAttackable())
                if (target.skipAttackInteraction(playerEntity))
                    return;

            ItemStack offhandStack = playerEntity.getOffhandItem();

            // use offhand modifiers
            mcdw$switchModifiers(playerEntity, playerEntity.getMainHandItem(), offhandStack);

            float cooldownProgress = ((IDualWielding) playerEntity).mcdw$getOffhandAttackCooldownProgress(0.5F);
            float attackDamage = (float) playerEntity.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
            attackDamage *= (float) (0.2f + Math.pow(cooldownProgress, 2) * 0.8f);

            // use mainhand modifiers
            mcdw$switchModifiers(playerEntity, offhandStack, playerEntity.getMainHandItem());

            float enchantBonusDamage = EnchantmentHelper.getDamageBonus(offhandStack, target instanceof LivingEntity livingTarget ?
                    livingTarget.getMobType() : MobType.UNDEFINED) * cooldownProgress;

            ((IDualWielding) playerEntity).mcdw$resetLastAttackedOffhandTicks();

            if (attackDamage > 0.0f || enchantBonusDamage > 0.0f) {
                /* bl */
                boolean isMostlyCharged = cooldownProgress > 0.9f;

                /* i */
                int knockbackLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, offhandStack);
                if (playerEntity.isSprinting() && isMostlyCharged) {
                    CleanlinessHelper.playCenteredSound(playerEntity, SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.0f);
                    ++knockbackLevel;
                }

                boolean playerShouldCrit = isMostlyCharged && AbilityHelper.entityCanCrit(playerEntity)
                        && target instanceof LivingEntity;
                if (playerShouldCrit && !playerEntity.isSprinting()) {
                    attackDamage *= 1.5f;
                }

                attackDamage += enchantBonusDamage;
                boolean playerShouldSweep = isMostlyCharged && !playerShouldCrit && !playerEntity.isSprinting() && playerEntity.onGround()
                        && playerEntity.walkDist - playerEntity.walkDistO < (double) playerEntity.getSpeed()
                        && offhandStack.getItem() instanceof IOffhandAttack;

                /* j */
                float targetHealth = 0.0f;
                boolean bl5 = false;
                /* k */
                int fireAspectLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, offhandStack);
                if (target instanceof LivingEntity livingTarget) {
                    targetHealth = livingTarget.getHealth();
                    if (fireAspectLevel > 0 && !livingTarget.isOnFire()) {
                        bl5 = true;
                        livingTarget.setSecondsOnFire(1);
                    }
                }

                Vec3d targetVelocity = target.getDeltaMovement();
                if (target.hurt(target.getWorld().damageSources().playerAttack(playerEntity), attackDamage)) {
                    double positionOne = -Mth.sin(playerEntity.getYRot() * ((float) Math.PI / 180));
                    double positionTwo = Mth.cos(playerEntity.getYRot() * ((float) Math.PI / 180));
                    if (knockbackLevel > 0) {
                        if (target instanceof LivingEntity livingTarget) {
                            livingTarget.knockback((float) knockbackLevel * 0.5f, -positionOne, -positionTwo);
                        } else {
                            target.push(positionOne * (float) knockbackLevel * 0.5f, 0.1,
                                    positionTwo * (float) knockbackLevel * 0.5f);
                        }
                        playerEntity.setDeltaMovement(playerEntity.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                        playerEntity.setSprinting(false);
                    }

                    if (playerShouldSweep) {
                        float sweepingEdgeMultiplierTimesDamage = 1.0f + SweepingEdgeEnchantment.getSweepingDamageRatio(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SWEEPING_EDGE, offhandStack)) * attackDamage;
                        playerEntity.getWorld().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0)).forEach(sweptEntity -> {
                            if (AOEHelper.satisfySweepConditions(playerEntity, target, sweptEntity, 3.0f)) {
                                sweptEntity.knockback(0.4f, -positionOne, -positionTwo);
                                sweptEntity.hurt(
                                        sweptEntity.getWorld().damageSources().playerAttack(playerEntity),
                                        sweepingEdgeMultiplierTimesDamage);
                            }
                        });
                        CleanlinessHelper.playCenteredSound(playerEntity, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
                        if (playerEntity.getWorld() instanceof ServerWorld serverWorld) {
                            serverWorld.sendParticles(ParticlesRegistry.OFFHAND_SWEEP_PARTICLE, playerEntity.getX() + positionOne,
                                    playerEntity.getY(0.5D), playerEntity.getZ() + positionTwo, 0, positionOne, 0.0D, positionTwo, 0.0D);
                        }
                    }

                    if (target instanceof ServerPlayerEntity && target.hurtMarked) {
                        ((ServerPlayerEntity) target).connection.send(new ClientboundSetEntityMotionPacket(target));
                        target.hurtMarked = false;
                        target.setDeltaMovement(targetVelocity);
                    }
                    if (playerShouldCrit) {
                        CleanlinessHelper.playCenteredSound(playerEntity, SoundEvents.PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
                        playerEntity.crit(target);
                    } else if (!playerShouldSweep) {
                        CleanlinessHelper.playCenteredSound(playerEntity,
                                isMostlyCharged ? SoundEvents.PLAYER_ATTACK_STRONG : SoundEvents.PLAYER_ATTACK_WEAK,
                                1.0f, 1.0f);
                    }

                    if (enchantBonusDamage > 0.0f) {
                        playerEntity.magicCrit(target);
                    }

                    playerEntity.setLastHurtMob(target);
                    if (target instanceof LivingEntity livingTarget) {
                        EnchantmentHelper.doPostHurtEffects(livingTarget, playerEntity);
                    }

                    EnchantmentHelper.doPostDamageEffects(playerEntity, target);
                    Entity targetedEntity = target;
                    if (target instanceof EnderDragonPart enderDragonPartTarget) {
                        targetedEntity = enderDragonPartTarget.parentMob;
                    }

                    if (!playerEntity.getWorld().isClientSide && !offhandStack.isEmpty() && targetedEntity instanceof LivingEntity livingTarget) {
                        offhandStack.hurtEnemy(livingTarget, playerEntity);
                        if (offhandStack.isEmpty()) {
                            playerEntity.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                        }
                    }

                    if (target instanceof LivingEntity livingTarget) {
                        /* m */
                        float targetCurrentHealth = targetHealth - livingTarget.getHealth();
                        playerEntity.awardStat(Stats.DAMAGE_DEALT, Math.round(targetCurrentHealth * 10.0f));
                        if (fireAspectLevel > 0) {
                            target.setSecondsOnFire(fireAspectLevel * 4);
                        }

                        if (playerEntity.getWorld() instanceof ServerWorld playerServerWorld && targetCurrentHealth > 2.0f) {
                            int particleCount = (int) ((double) targetCurrentHealth * 0.5);
                            playerServerWorld.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                                    target.getX(), target.getY(0.5), target.getZ(),
                                    particleCount, 0.1, 0.0, 0.1, 0.2);
                        }
                    }
                    playerEntity.causeFoodExhaustion(0.1f);
                } else {
                    CleanlinessHelper.playCenteredSound(playerEntity, SoundEvents.PLAYER_ATTACK_NODAMAGE, 1.0f, 1.0f);
                    if (bl5) {
                        target.clearFire();
                    }
                }
            }
        }
    }

    public static boolean mixAndMatchWeapons(PlayerEntity playerEntity) {
            return (playerEntity.getOffhandItem().is(playerEntity.getMainHandItem().getItem())
                    || (playerEntity.getMainHandItem().is(DaggersID.DAGGER_THE_BEGINNING.getItem()) && playerEntity.getOffhandItem().is(DaggersID.DAGGER_THE_END.getItem()))
                    || (playerEntity.getMainHandItem().is(DaggersID.DAGGER_THE_END.getItem()) && playerEntity.getOffhandItem().is(DaggersID.DAGGER_THE_BEGINNING.getItem()))
                    || (playerEntity.getMainHandItem().is(SicklesID.SICKLE_LAST_LAUGH_GOLD.getItem()) && playerEntity.getOffhandItem().is(SicklesID.SICKLE_LAST_LAUGH_SILVER.getItem()))
                    || (playerEntity.getMainHandItem().is(SicklesID.SICKLE_LAST_LAUGH_SILVER.getItem()) && playerEntity.getOffhandItem().is(SicklesID.SICKLE_LAST_LAUGH_GOLD.getItem())));
    }

    /**
     * Copyright 2019 Erlend Åmdal
     * <br/><br/>
     * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
     * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
     * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
     * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
     * <br/><br/>
     * The above copyright notice and this permission notice shall be included in all copies or substantial portions
     * of the Software.
     * <br/><br/>
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
     * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
     * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
     * IN THE SOFTWARE.
     * <br/><br/>
     * The following code is from Reach Entity Attributes and can be found here:
     * <a href = "https://github.com/JamiesWhiteShirt/reach-entity-attributes/blob/1.19/src/main/java/com/jamieswhiteshirt/reachentityattributes/ReachEntityEntityAttributes.java#L27">ReachEntityAttributes Lines 27-30</a>
     */

    public static double mcdw$getReachDistance(LivingEntity livingEntity, double defaultReachDistance) {
        @Nullable
        AttributeInstance reachDistance = livingEntity.getAttribute(EntityAttributesRegistry.REACH);
        return (reachDistance != null) ? (defaultReachDistance + reachDistance.getValue()) : defaultReachDistance;
    }

    /**
     * Copyright 2019 Erlend Åmdal
     * <br/><br/>
     * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
     * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
     * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
     * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
     * <br/><br/>
     * The above copyright notice and this permission notice shall be included in all copies or substantial portions
     * of the Software.
     * <br/><br/>
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
     * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
     * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
     * IN THE SOFTWARE.
     * <br/><br/>
     * The following code is from Reach Entity Attributes and can be found here:
     * <a href = "https://github.com/JamiesWhiteShirt/reach-entity-attributes/blob/1.19/src/main/java/com/jamieswhiteshirt/reachentityattributes/ReachEntityEntityAttributes.java#L32">ReachEntityAttributes Lines 32-35</a>
     */
    @SuppressWarnings("unused")
    public static double mcdw$getSquaredReachDistance(LivingEntity livingEntity, double squareDefaultReachDistance) {
        double reachDistance = mcdw$getReachDistance(livingEntity, Math.sqrt(squareDefaultReachDistance));
        return reachDistance * reachDistance;
    }

    /**
     * Copyright 2019 Erlend Åmdal
     * <br/><br/>
     * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
     * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
     * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
     * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
     * <br/><br/>
     * The above copyright notice and this permission notice shall be included in all copies or substantial portions
     * of the Software.
     * <br/><br/>
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
     * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
     * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
     * IN THE SOFTWARE.
     * <br/><br/>
     * The following code is from Reach Entity Attributes and can be found here:
     * <a href = "https://github.com/JamiesWhiteShirt/reach-entity-attributes/blob/1.19/src/main/java/com/jamieswhiteshirt/reachentityattributes/ReachEntityEntityAttributes.java#L37">ReachEntityAttributes Lines 37-40</a>
     */
    public static double mcdw$getAttackRange(LivingEntity livingEntity, double defaultAttackRange) {
        @Nullable
        AttributeInstance attackRange = livingEntity.getAttribute(EntityAttributesRegistry.ATTACK_RANGE);
        return (attackRange != null) ? (defaultAttackRange + attackRange.getValue()) : defaultAttackRange;
    }

    /**
     * Copyright 2019 Erlend Åmdal
     * <br/><br/>
     * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
     * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
     * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
     * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
     * <br/><br/>
     * The above copyright notice and this permission notice shall be included in all copies or substantial portions
     * of the Software.
     * <br/><br/>
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
     * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
     * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
     * IN THE SOFTWARE.
     * <br/><br/>
     * The following code is from Reach Entity Attributes and can be found here:
     * <a href = "https://github.com/JamiesWhiteShirt/reach-entity-attributes/blob/1.19/src/main/java/com/jamieswhiteshirt/reachentityattributes/ReachEntityEntityAttributes.java#L42">ReachEntityAttributes Lines 42-45</a>
     */
    public static double mcdw$getSquaredAttackRange(LivingEntity livingEntity, double squareDefaultAttackRange) {
        double attackRange = mcdw$getAttackRange(livingEntity, Math.sqrt(squareDefaultAttackRange));
        return attackRange * attackRange;
    }

    /**
     * Copyright 2019 Erlend Åmdal
     * <br/><br/>
     * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
     * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
     * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
     * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
     * <br/><br/>
     * The above copyright notice and this permission notice shall be included in all copies or substantial portions
     * of the Software.
     * <br/><br/>
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
     * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
     * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
     * IN THE SOFTWARE.
     * <br/><br/>
     * The following code is from Reach Entity Attributes and can be found here:
     * <a href = "https://github.com/JamiesWhiteShirt/reach-entity-attributes/blob/1.19/src/main/java/com/jamieswhiteshirt/reachentityattributes/ReachEntityEntityAttributes.java#L47">ReachEntityAttributes Lines 47-49</a>
     */
    @SuppressWarnings("unused")
    public static List<PlayerEntity> mcdw$getPlayerEntitiesWithinReach(World world, int x, int y, int z, double defaultReachDistance) {
        return mcdw$getPlayerEntitiesWithinReach(PlayerEntity -> true, world, x, y, z, defaultReachDistance);
    }

    /**
     * Copyright 2019 Erlend Åmdal
     * <br/><br/>
     * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
     * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
     * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
     * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
     * <br/><br/>
     * The above copyright notice and this permission notice shall be included in all copies or substantial portions
     * of the Software.
     * <br/><br/>
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
     * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
     * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
     * IN THE SOFTWARE.
     * <br/><br/>
     * The following code is from Reach Entity Attributes and can be found here:
     * <a href = "https://github.com/JamiesWhiteShirt/reach-entity-attributes/blob/1.19/src/main/java/com/jamieswhiteshirt/reachentityattributes/ReachEntityEntityAttributes.java#L51">ReachEntityAttributes Lines 51-65</a>
     */
    public static List<PlayerEntity> mcdw$getPlayerEntitiesWithinReach(Predicate<PlayerEntity> viewerPredicate, World world, int x, int y, int z, double defaultReachDistance) {
        List<PlayerEntity> playerEntitiesWithinReach = new ArrayList<>();
        for (PlayerEntity playerEntity : world.players()) {
            if (viewerPredicate.test(playerEntity)) {
                double reach = mcdw$getReachDistance(playerEntity, defaultReachDistance);
                double dx = (x + 0.5) - playerEntity.getX();
                double dy = (y + 0.5) - playerEntity.getEyeY();
                double dz = (z + 0.5) - playerEntity.getZ();
                if (((dx * dx) + (dy * dy) + (dz * dz)) <= (reach * reach)) {
                    playerEntitiesWithinReach.add(playerEntity);
                }
            }
        }
        return playerEntitiesWithinReach;
    }

    /**
     * Copyright 2019 Erlend Åmdal
     * <br/><br/>
     * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
     * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
     * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
     * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
     * <br/><br/>
     * The above copyright notice and this permission notice shall be included in all copies or substantial portions
     * of the Software.
     * <br/><br/>
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
     * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
     * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
     * IN THE SOFTWARE.
     * <br/><br/>
     * The following code is from Reach Entity Attributes and can be found here:
     * <a href = "https://github.com/JamiesWhiteShirt/reach-entity-attributes/blob/1.19/src/main/java/com/jamieswhiteshirt/reachentityattributes/ReachEntityEntityAttributes.java#L67">ReachEntityAttributes Lines 67-69</a>
     */
    public static boolean mcdw$isEntityWithinAttackRange(PlayerEntity playerEntity, Entity entity) {
        return playerEntity.distanceToSqr(entity) <= mcdw$getSquaredAttackRange(playerEntity, 64);
    }
}
