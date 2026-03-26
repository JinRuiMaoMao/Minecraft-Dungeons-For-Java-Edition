package jinrui.mcdar.entities;

import jinrui.mcdar.api.SummoningHelper;
import jinrui.mcdar.api.interfaces.OwnableSummon;
import jinrui.mcdar.goals.FollowSummonerGoal;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.world.World;

public class GolemKitGolemEntity extends IronGolemEntity implements OwnableSummon {
    @Nullable
    UUID ownerEntityUUID = null;

    public GolemKitGolemEntity(EntityType<? extends GolemKitGolemEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initGoals() {
        this.goals.add(6, new FollowSummonerGoal<>(this, 1.0,
                this.getNavigation(), 90.0F, 3.0F));
        this.initCustomGoals();
    }

    protected void initCustomGoals() {
        this.goals.add(1, new MeleeAttackGoal(this, 1.0D, true));
        this.targetSelector.add(2, new RevengeGoal(this));
    }

    @Override
    public void setSummoner(Entity PlayerEntity) {
        ownerEntityUUID = PlayerEntity.getUUID();
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
        return ownerEntityUUID;
    }
}
