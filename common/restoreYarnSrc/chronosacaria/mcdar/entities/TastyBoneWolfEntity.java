package jinrui.mcdar.entities;

import jinrui.mcdar.api.SummoningHelper;
import jinrui.mcdar.api.interfaces.Summonable;
import jinrui.mcdar.goals.FollowSummonerGoal;
import jinrui.mcdar.goals.TrackSummonerAttackerGoal;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TastyBoneWolfEntity extends Wolf implements OwnableEntity, Summonable {

    @Nullable
    UUID ownerEntityUUID = null;

    public TastyBoneWolfEntity(EntityType<? extends TastyBoneWolfEntity> type, Level world) {
        super(type, world);
    }

    public static AttributeSupplier.Builder createTastyBoneWolfAttributes(){
        // Attributes of Tamed Wolf
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.30000001192092896)
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    //TODO Find why they be spinning
    @Override
    protected void registerGoals(){
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new FollowSummonerGoal<>(this, 1.0,
                this.getNavigation(), 90.0F, 3.0F));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new TrackSummonerAttackerGoal<>(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public void setSummoner(Entity player) {
        ownerEntityUUID = player.getUUID();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getOwnerUUID() != null)
            tag.putUUID("SummonerUUID",getOwnerUUID());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        UUID id = tag.getUUID("SummonerUUID");
        if (id != null)
            this.ownerEntityUUID = id;
    }

    @Override
    public float getTailAngle() {
        if (this.isAngry()) {
            return 1.5393804F;
        } else {
            return (0.90F - (this.getMaxHealth() - this.getHealth()) * 0.02F) * (3.1415927F/2);
        }
    }

    @Override
    public boolean canMate(Animal other) {
        return false;
    }

    @Override
    public boolean isTame() {
        return true;
    }

    @Override
    public boolean isOrderedToSit() {
        return false;
    }

    @Override
    public boolean isInSittingPose() {
        return false;
    }

    @Override
    public void setLastHurtByMob(LivingEntity attacker){
        if (attacker != null && !attacker.equals(getOwner()))
            super.setLastHurtByMob(attacker);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        SummoningHelper.mcdar$trackAndProtectSummoner(this);
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return ownerEntityUUID;
    }

    @Override
    public EntityGetter level() {
        return this.getCommandSenderWorld();
    }
}
