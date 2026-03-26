package jinrui.mcdar.entities.renderers;

import jinrui.mcdar.entities.GolemKitGolemEntity;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class GolemKitGolemCrackFeatureRenderer extends RenderLayer<GolemKitGolemEntity, IronGolemModel<GolemKitGolemEntity>> {
    private static final Map<IronGolem.Crackiness, ResourceLocation> DAMAGE_TO_TEXTURE;

    public GolemKitGolemCrackFeatureRenderer(RenderLayerParent<GolemKitGolemEntity, IronGolemModel<GolemKitGolemEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    public void render(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, GolemKitGolemEntity ironGolemEntity, float f, float g, float h, float j, float k, float l) {
        if (!ironGolemEntity.isInvisible()) {
            GolemKitGolemEntity.Crack crack = ironGolemEntity.getCrackiness();
            if (crack != GolemKitGolemEntity.Crack.NONE) {
                ResourceLocation identifier = DAMAGE_TO_TEXTURE.get(crack);
                renderColoredCutoutModel(this.getParentModel(), identifier, matrixStack, vertexConsumerProvider, i, ironGolemEntity, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    static {
        DAMAGE_TO_TEXTURE = ImmutableMap.of(GolemKitGolemEntity.Crack.LOW, new ResourceLocation("textures/entity/iron_golem/iron_golem_crackiness_low.png"), IronGolem.Crackiness.MEDIUM, new ResourceLocation("textures/entity/iron_golem/iron_golem_crackiness_medium.png"), IronGolem.Crackiness.HIGH, new ResourceLocation("textures/entity/iron_golem/iron_golem_crackiness_high.png"));
    }
}
