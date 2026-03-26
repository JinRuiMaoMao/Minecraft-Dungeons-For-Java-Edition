package mcd_java.mixin;

import mcd_java.api.interfaces.IMcdaBooleans;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityMixin
        extends LivingEntity
        implements IMcdaBooleans {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @SuppressWarnings("WrongEntityDataParameterClass")
    private static final EntityDataAccessor<Boolean> FIRE_TRAIL = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);

    @Override
    public SynchedEntityData getEntityData(){ return entityData; }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void mcda$injectInitDataTracker(CallbackInfo ci) {
        entityData.define(FIRE_TRAIL, false);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void mcda$injectWriteCustomDataToNbt(CompoundTag nbt, CallbackInfo ci) {
        nbt.putBoolean("fire_trail_boolean", isFireTrailEnabled());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void mcda$injectReadCustomDataFromNbt(CompoundTag nbt, CallbackInfo ci) {
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