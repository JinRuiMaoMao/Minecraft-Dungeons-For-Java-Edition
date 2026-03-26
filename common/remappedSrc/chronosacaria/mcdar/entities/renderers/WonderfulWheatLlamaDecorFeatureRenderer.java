package jinrui.mcdar.entities.renderers;

import jinrui.mcdar.entities.WonderfulWheatLlamaEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class WonderfulWheatLlamaDecorFeatureRenderer extends RenderLayer<WonderfulWheatLlamaEntity, LlamaModel<WonderfulWheatLlamaEntity>> {
    private static final ResourceLocation TRADER_LLAMA_DECOR = new ResourceLocation("textures/entity/llama/decor/trader_llama.png");
    private final LlamaModel<WonderfulWheatLlamaEntity> model;

    public WonderfulWheatLlamaDecorFeatureRenderer(RenderLayerParent<WonderfulWheatLlamaEntity, LlamaModel<WonderfulWheatLlamaEntity>> context, EntityModelSet loader) {
        super(context);
        this.model = new LlamaModel<>(loader.bakeLayer(ModelLayers.LLAMA_DECOR));
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, int light, WonderfulWheatLlamaEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        this.getParentModel().copyPropertiesTo(this.model);
        this.model.setupAnim(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutoutNoCull(TRADER_LLAMA_DECOR));
        this.model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
