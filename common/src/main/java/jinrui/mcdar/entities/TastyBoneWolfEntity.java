package jinrui.mcdar.entities;

import jinrui.mcdar.api.SummoningHelper;
import jinrui.mcdar.api.interfaces.OwnableSummon;
import jinrui.mcdar.goals.FollowSummonerGoal;
import jinrui.mcdar.goals.TrackSummonerAttackerGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TastyBoneWolfEntity extends WolfEntity implements OwnableSummon {

    @Nullable
    UUID ownerEntityUUID = null;

    public TastyBoneWolfEntity(EntityType<? extends TastyBoneWolfEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createTastyBoneWolfAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityEntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30000001192092896)
                .add(EntityEntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityEntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void initGoals() {
        this.goals.add(1, new SwimGoal(this));
        this.goals.add(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goals.add(3, new PounceAtTargetGoal(this, 0.4F));
        this.goals.add(4, new FollowSummonerGoal<>(this, 1.0,
                this.getNavigation(), 90.0F, 3.0F));
        this.goals.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.targetSelector.add(1, new TrackSummonerAttackerGoal<>(this));
        this.targetSelector.add(2, new RevengeGoal(this).setGroupRevenge());
    }

    @Override
    public void setSummoner(Entity summoner) {
        ownerEntityUUID = summoner.getUuid();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        if (getOwnerUUID() != null)
            tag.putUuid("SummonerUUID", getOwnerUUID());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        if (tag.containsUuid("SummonerUUID")) {
            this.ownerEntityUUID = tag.getUuid("SummonerUUID");
        }
    }

    @Override
    public float getTailAngle() {
        if (this.hasAngerTime()) {
            return 1.5393804F;
        } else {
            return (0.90F - (this.getMaxHealth() - this.getHealth()) * 0.02F) * ((float) Math.PI / 2);
        }
    }

    @Override
    public boolean canMate(AnimalEntity other) {
        return false;
    }

    @Override
    public boolean isTamed() {
        return true;
    }

    @Override
    public boolean isSitting() {
        return false;
    }

    @Override
    public boolean isInSittingPose() {
        return false;
    }

    @Override
    public void setLastAttacker(LivingEntity attacker) {
        if (attacker != null && !attacker.equals(getOwner()))
            super.setLastAttacker(attacker);
    }

    @Override
    public void tick() {
        super.tick();
        SummoningHelper.mcdar$trackAndProtectSummoner(this);
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return ownerEntityUUID != null ? ownerEntityUUID : super.getOwnerUuid();
    }
}
