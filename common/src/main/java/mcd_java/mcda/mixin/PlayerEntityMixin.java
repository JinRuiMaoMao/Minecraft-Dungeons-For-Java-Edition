package mcd_java.mcda.mixin;

import mcd_java.mcda.api.interfaces.IMcdaBooleans;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin
        extends LivingEntity
        implements IMcdaBooleans {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings("WrongEntityDataParameterClass")
    private static final TrackedData<Boolean> FIRE_TRAIL = SynchedEntityData.defineId(PlayerEntity.class, EntityDataSerializers.BOOLEAN);

    @Override
    public SynchedEntityData getEntityData(){ return entityData; }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void mcda$injectInitDataTracker(CallbackInfo ci) {
        entityData.define(FIRE_TRAIL, false);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void mcda$injectWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("fire_trail_boolean", isFireTrailEnabled());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void mcda$injectReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        setFireTrailEnabled(nbt.getBoolean("fire_trail_boolean"));
    }

    @Override
    public boolean isFireTrailEnabled() {
        return entityData.get(FIRE_TRAIL);
    }

    @Override
    public void setFireTrailEnabled(boolean fireTrailEnabled) {
        entityData.set(FIRE_TRAIL, fireTrailEnabled);
    }
}