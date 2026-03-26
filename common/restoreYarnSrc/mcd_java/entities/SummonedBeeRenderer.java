package mcd_java.entities;

import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SummonedBeeRenderer extends MobRenderer<SummonedBeeEntity, BeeModel<SummonedBeeEntity>> {
    private static final ResourceLocation ANGRY_TEXTURE = new ResourceLocation("textures/entity/bee/bee_angry.png");
    private static final ResourceLocation ANGRY_NECTAR_TEXTURE = new ResourceLocation("textures/entity/bee/bee_angry_nectar.png");
    private static final ResourceLocation PASSIVE_TEXTURE = new ResourceLocation("textures/entity/bee/bee.png");
    private static final ResourceLocation NECTAR_TEXTURE = new ResourceLocation("textures/entity/bee/bee_nectar.png");

    public SummonedBeeRenderer(EntityRendererProvider.Context context) {
        super(context, new BeeModel<>(context.bakeLayer(ModelLayers.BEE)), 0.4F);
    }

    public ResourceLocation getTexture(SummonedBeeEntity beeEntity) {
        if (beeEntity.isAngry()) {
            return beeEntity.hasNectar() ? ANGRY_NECTAR_TEXTURE : ANGRY_TEXTURE;
        } else {
            return beeEntity.hasNectar() ? NECTAR_TEXTURE : PASSIVE_TEXTURE;
        }
    }
}
