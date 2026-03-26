package jinrui.mcdar.artifacts;

import jinrui.mcdar.enums.SummoningArtifactID;
import jinrui.mcdar.registries.ItemGroupRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;

public class ArtifactSummoningItem extends Item {

    public final SummoningArtifactID id;

    public ArtifactSummoningItem(SummoningArtifactID id, int artifactDurability) {
        super(new Properties().stacksTo(1).durability(artifactDurability));
        this.id = id;
        ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.ARTIFACTS).register(entries -> entries.accept(this.getDefaultInstance()));
    }

    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }
}
