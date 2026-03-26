package jinrui.mcdar.entities;

import jinrui.mcdar.api.SummoningHelper;
import jinrui.mcdar.api.interfaces.Summonable;
import jinrui.mcdar.goals.FollowSummonerGoal;
import jinrui.mcdar.goals.TrackSummonerAttackerGoal;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EnchantedGrassRedSheepEntity extends Sheep implements OwnableEntity, Summonable {
    @Nullable
    UUID ownerEntityUUID = null;

    public EnchantedGrassRedSheepEntity(EntityType<? extends EnchantedGrassRedSheepEntity> type, Level world) {
        super(type, world);
        this.setColor(DyeColor.RED);
    }

    public static AttributeSupplier.Builder createEnchantedRedSheepAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals(){
        EatBlockGoal eatGrassGoal = new EatBlockGoal(this);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, eatGrassGoal);
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(4, new FollowSummonerGoal<>(this, 1.0,
                this.getNavigation(), 90.0F, 3.0F));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new TrackSummonerAttackerGoal<>(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));    }

    @Override
    public void setSummoner(Entity player) {
        ownerEntityUUID = player.getUUID();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag){
        super.addAdditionalSaveData(tag);
        if (getOwnerUUID() != null)
            tag.putUUID("SummonerUUID",getOwnerUUID());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        UUID id = tag.getUUID("SummonerUUID");
        if (id != null){
            this.ownerEntityUUID = id;
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return SummoningHelper.mcdar$attackTarget(this, target, SoundEvents.SHEEP_AMBIENT, 8.0f);
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

    @Override
    protected void customServerAiStep(){

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
