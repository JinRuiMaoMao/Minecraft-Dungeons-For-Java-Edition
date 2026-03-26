package jinrui.mcdar.artifacts.beacon;

import jinrui.mcdar.enums.DamagingArtifactID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class SoulBeaconItem extends AbstractBeaconItem{
    public SoulBeaconItem(DamagingArtifactID artefactID, int artifactDurability) {
        super(artefactID, artifactDurability);
    }

    public boolean canFire(Player playerEntity, ItemStack itemStack){
        SoulBeaconItem soulBeaconItem = itemStack.getItem() instanceof SoulBeaconItem ? ((SoulBeaconItem) itemStack.getItem()) : null;
        if (soulBeaconItem != null){
            return playerEntity.experienceLevel >= soulBeaconItem.getActivationCost(itemStack) || playerEntity.isCreative();
        }
        return false;
    }

    @Override
    protected boolean consumeTick(Player playerEntity) {
        return consumeXP(playerEntity, XP_COST_PER_TICK);
    }

    public float getActivationCost(ItemStack stack){
        return AbstractBeaconItem.XP_COST_PER_TICK;
    }

    public boolean consumeXP(Player playerEntity, float amount){
        if (playerEntity.experienceLevel < amount) return false;
        playerEntity.experienceLevel -= amount/4;
        return true;
    }
}
