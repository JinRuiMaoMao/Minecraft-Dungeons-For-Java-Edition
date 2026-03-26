package jinrui.mcdar.artifacts;

import jinrui.mcdar.enums.DamagingArtifactID;
import jinrui.mcdar.registries.ItemGroupRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;

public class ArtifactDamagingItem extends Item {

    public final DamagingArtifactID id;

    public ArtifactDamagingItem(DamagingArtifactID id, int artifactDurability) {
        super(new Properties().stacksTo(1).durability(artifactDurability));
        this.id = id;
        ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.ARTIFACTS).register(entries -> entries.accept(this.getDefaultInstance()));
    }

    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }
}
