package jinrui.mcdar.entities;

import jinrui.mcdar.api.interfaces.Summonable;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;

public class BuzzyNestBeeEntity extends BeeEntity implements Summonable {

    @Nullable
    UUID ownerEntityUUID = null;

    final int MAX_AGE = 12000;

    public BuzzyNestBeeEntity(EntityType<BuzzyNestBeeEntity> type, World world){
        super(type, world);
    }

    public void setSummoner(Entity PlayerEntity) {
        ownerEntityUUID = PlayerEntity.getUUID();
    }

    @Override
    public void addAdditionalSaveData(NbtCompound tag) {
        super.addAdditionalSaveData(tag);
        if (getOwnerUUID() != null)
            tag.putUUID("SummonerUUID",getOwnerUUID());
    }

    @Override
    public void readAdditionalSaveData(NbtCompound tag) {
        super.readAdditionalSaveData(tag);
        UUID id = tag.getUUID("SummonerUUID");
        if (id != null)
            this.ownerEntityUUID = id;
    }

    protected void customServerAiStep(){
        if (getOwner() instanceof PlayerEntity summoner){
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
    public EntityView level() {
        return this.getWorld();
    }
}
