package jinrui.mcdar.registries;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.enums.DamagingArtifactID;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ItemGroupRegistry {
    public static final RegistryKey<ItemGroup> ARTIFACTS = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Mcdar.ID("artifacts"));

    public static void register() {
        Registry.register(Registries.CREATIVE_MODE_TAB, ARTIFACTS, FabricItemGroup.builder()
                .title(Text.translatable("itemGroup.mcdar.artifacts"))
                .icon(() -> new ItemStack(ArtifactsRegistry.DAMAGING_ARTIFACT.get(DamagingArtifactID.LIGHTNING_ROD)))
                .build());
    }
}
