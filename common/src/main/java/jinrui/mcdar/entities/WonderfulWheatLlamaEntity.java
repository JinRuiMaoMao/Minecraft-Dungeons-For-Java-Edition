package jinrui.mcdar.entities;

import jinrui.mcdar.api.SummoningHelper;
import jinrui.mcdar.api.interfaces.OwnableSummon;
import jinrui.mcdar.goals.FollowSummonerGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.TraderLlamaEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WonderfulWheatLlamaEntity extends TraderLlamaEntity implements OwnableSummon {

    @Nullable
    UUID ownerEntityUUID = null;

    public WonderfulWheatLlamaEntity(EntityType<? extends WonderfulWheatLlamaEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return LlamaEntity.createLlamaAttributes().add(EntityEntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void initGoals() {
        this.goals.add(6, new FollowSummonerGoal<>(this, 1.0,
                this.getNavigation(), 90.0F, 3.0F));
        this.addBehaviourGoals();
    }

    protected void addBehaviourGoals() {
        this.goals.add(3, new ProjectileAttackGoal(this, 1.25D, 40, 20.0F));
        this.targetSelector.add(2, new RevengeGoal(this));
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
