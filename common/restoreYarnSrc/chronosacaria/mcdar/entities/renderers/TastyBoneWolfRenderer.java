package jinrui.mcdar.entities.renderers;

import jinrui.mcdar.entities.TastyBoneWolfEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TastyBoneWolfRenderer extends MobRenderer<TastyBoneWolfEntity, WolfModel<TastyBoneWolfEntity>> {
    public TastyBoneWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new WolfModel<>(context.bakeLayer(ModelLayers.WOLF)), 0.5F);
    }

    public void render(TastyBoneWolfEntity wolfEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i) {
        if (wolfEntity.isWet()) {
            float h = wolfEntity.getWetShade(g);
            this.model.setColor(h, h, h);
        }

        super.render(wolfEntity, f, g, matrixStack, vertexConsumerProvider, i);
        if (wolfEntity.isWet()) {
            this.model.setColor(1.0F, 1.0F, 1.0F);
        }
    }
    @Override
    protected float getAnimationProgress(TastyBoneWolfEntity entity, float tickDelta) {
        return entity.getTailAngle();
    }

    @Override
    public ResourceLocation getTexture(TastyBoneWolfEntity entity){
        return new ResourceLocation("textures/entity/wolf/wolf" + (entity.isAngry() ? "_angry" : "") + ".png");
    }

}
