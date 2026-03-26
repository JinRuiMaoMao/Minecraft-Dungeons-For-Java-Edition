package jinrui.mcdar.entities.renderers;

import jinrui.mcdar.entities.WonderfulWheatLlamaEntity;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WonderfulWheatLlamaRenderer extends MobRenderer<WonderfulWheatLlamaEntity, LlamaModel<WonderfulWheatLlamaEntity>> {
    public WonderfulWheatLlamaRenderer(EntityRendererProvider.Context context) {
        super(context, new LlamaModel<>(context.bakeLayer(ModelLayers.LLAMA)), 0.7F);
        this.addLayer(new WonderfulWheatLlamaDecorFeatureRenderer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTexture(WonderfulWheatLlamaEntity entity){
        return new ResourceLocation("textures/entity/llama/brown.png");
    }

}
