package jinrui.mcdar.entities;

import jinrui.mcdar.api.SummoningHelper;
import jinrui.mcdar.api.interfaces.OwnableSummon;
import jinrui.mcdar.goals.FollowSummonerGoal;
import jinrui.mcdar.goals.TrackSummonerAttackerGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EnchantedGrassBlueSheepEntity extends SheepEntity implements OwnableSummon {
    @Nullable
    UUID ownerEntityUUID = null;

    public EnchantedGrassBlueSheepEntity(EntityType<? extends EnchantedGrassBlueSheepEntity> type, World world) {
        super(type, world);
        this.setColor(DyeColor.BLUE);
    }

    public static DefaultAttributeContainer.Builder createEnchantedBlueSheepEntityAttributes() {
        return Monster.createMobAttributes()
                .add(EntityEntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D)
                .add(EntityEntityAttributes.GENERIC_MAX_HEALTH, 8.0D)
                .add(EntityEntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0D)
                .add(EntityEntityAttributes.GENERIC_FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void initGoals() {
        EatGrassGoal eatGrassGoal = new EatGrassGoal(this);
        this.goals.add(1, new SwimGoal(this));
        this.goals.add(2, eatGrassGoal);
        this.goals.add(3, new MeleeAttackGoal(this, 1.0D, true));
        this.goals.add(4, new FollowSummonerGoal<>(this, 1.0,
                this.getNavigation(), 90.0F, 3.0F));
        this.goals.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goals.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new TrackSummonerAttackerGoal<>(this));
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
    public boolean tryAttack(Entity target) {
        return SummoningHelper.mcdar$attackTarget(this, target, SoundEvents.ENTITY_SHEEP_AMBIENT, 8.0f);
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
