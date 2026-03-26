/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw;

import mcd_java.mcdw.api.interfaces.IDualWielding;
import mcd_java.mcdw.configs.CompatibilityFlags;
import mcd_java.mcdw.enchants.summons.IBeeSummoning;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IDualWielding, IBeeSummoning {

    @Unique
    private static final TrackedData<Integer> LAST_ATTACKED_OFFHAND_TICKS = SynchedEntityData.defineId(PlayerEntityMixin.class, EntityDataSerializers.INT);

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public float mcdw$getOffhandAttackCooldownProgressPerTick() {
        return (float) (1.0D / this.getAttributeValue(EntityAttributes.ATTACK_SPEED) * 20.0D);
    }

    @Override
    public float mcdw$getOffhandAttackCooldownProgress(float baseTime) {
        return Mth.clamp(((float) mcdw$getOffhandAttackedTicks() + baseTime) / this.mcdw$getOffhandAttackCooldownProgressPerTick(), 0.0F, 1.0F);
    }

    @Override
    public void mcdw$resetLastAttackedOffhandTicks() {
        mcdw$setOffhandAttackedTicks(0);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/PlayerEntity/PlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
    public void mcdw$tick(CallbackInfo ci) {
        if (CompatibilityFlags.noOffhandConflicts)
            mcdw$setOffhandAttackedTicks(mcdw$getOffhandAttackedTicks() + 1);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void mcdw$initDataTracker(CallbackInfo ci) {
        if (CompatibilityFlags.noOffhandConflicts)
            entityData.define(LAST_ATTACKED_OFFHAND_TICKS, 0);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void mcdw$writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (CompatibilityFlags.noOffhandConflicts)
            nbt.putInt("LastAttackedOffhandTicks", mcdw$getOffhandAttackedTicks());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void mcdw$readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (CompatibilityFlags.noOffhandConflicts)
            mcdw$setOffhandAttackedTicks(nbt.getInt("LastAttackedOffhandTicks"));
    }

    @Unique
    private double roundedDouble(double value, int percision) {
        int scale = (int) Math.pow(10, percision);
        return (double) Math.round(value * scale) / scale;
    }

    @Override
    public int mcdw$getOffhandAttackedTicks() {
        if (CompatibilityFlags.noOffhandConflicts)
            return entityData.get(LAST_ATTACKED_OFFHAND_TICKS);
        return 0;
    }

    @Override
    public void mcdw$setOffhandAttackedTicks(int lastAttackedOffhandTicks) {
        if (CompatibilityFlags.noOffhandConflicts) {
            if (lastAttackedOffhandTicks >= 0)
                entityData.set(LAST_ATTACKED_OFFHAND_TICKS, lastAttackedOffhandTicks);
        }
    }

    /**
     * IBeeSummoning
     */
    @Unique
    private int lastTimeSummonedBee = 0;

    public void mcdw$setLastSummonedBee(int time) {
        lastTimeSummonedBee = time;
    }
    public int mcdw$getLastSummonedBee() {
        return lastTimeSummonedBee;
    }


}
