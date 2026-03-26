package jinrui.mcdar.entities;

import jinrui.mcdar.api.interfaces.Summonable;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;

public class BuzzyNestBeeEntity extends Bee implements OwnableEntity, Summonable {

    @Nullable
    UUID ownerEntityUUID = null;

    final int MAX_AGE = 12000;

    public BuzzyNestBeeEntity(EntityType<BuzzyNestBeeEntity> type, Level world){
        super(type, world);
    }

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

    protected void customServerAiStep(){
        if (getOwner() instanceof Player summoner){
            if (summoner.getLastHurtMob() != null)
                this.setBeeAttacker(summoner.getLastHurtMob());
            else if (summoner.getLastHurtByMob() != null)
                this.setBeeAttacker(summoner.getLastHurtByMob());
        }
        if (MAX_AGE > -1)
            if (tickCount >= MAX_AGE)
                kill();
        super.customServerAiStep();
    }

    private void setBeeAttacker(LivingEntity attacker){
        if (!attacker.equals(getOwner()))
            super.setLastHurtByMob(attacker);
    }

    public boolean doHurtTarget(Entity target){
        if (target.equals(getOwner()) || this.hasStung())
            return false;
        return super.doHurtTarget(target);
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
