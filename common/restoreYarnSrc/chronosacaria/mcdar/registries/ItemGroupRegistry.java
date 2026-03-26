package jinrui.mcdar.registries;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.enums.DamagingArtifactID;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ItemGroupRegistry {
    public static final ResourceKey<CreativeModeTab> ARTIFACTS = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Mcdar.ID("artifacts"));

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ARTIFACTS, FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.mcdar.artifacts"))
                .icon(() -> new ItemStack(ArtifactsRegistry.DAMAGING_ARTIFACT.get(DamagingArtifactID.LIGHTNING_ROD)))
                .build());
    }
}
