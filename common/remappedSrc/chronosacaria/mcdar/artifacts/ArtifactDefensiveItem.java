package jinrui.mcdar.artifacts;

import jinrui.mcdar.enums.DefensiveArtifactID;
import jinrui.mcdar.registries.ItemGroupRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ArtifactDefensiveItem extends Item {

    public final DefensiveArtifactID id;

    public ArtifactDefensiveItem(DefensiveArtifactID id, int artifactDurability) {
        super(new Properties().stacksTo(1).durability(artifactDurability));
        this.id = id;
        ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.ARTIFACTS).register(entries -> entries.accept(this.getDefaultInstance()));
    }

    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }
}
