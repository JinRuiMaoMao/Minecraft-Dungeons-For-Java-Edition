package jinrui.mcdar.entities;

import jinrui.mcdar.api.SummoningHelper;
import jinrui.mcdar.api.interfaces.Summonable;
import jinrui.mcdar.goals.FollowSummonerGoal;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;

public class GolemKitGolemEntity extends IronGolem implements OwnableEntity, Summonable {
    @Nullable
    UUID ownerEntityUUID = null;

    public GolemKitGolemEntity(EntityType<? extends GolemKitGolemEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void registerGoals(){
        this.goalSelector.addGoal(6, new FollowSummonerGoal<>(this, 1.0,
                this.getNavigation(), 90.0F, 3.0F));
        this.initCustomGoals();
    }

    protected void initCustomGoals(){
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
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
