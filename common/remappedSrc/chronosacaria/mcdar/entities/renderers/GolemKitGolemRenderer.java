package jinrui.mcdar.entities.renderers;

import jinrui.mcdar.entities.GolemKitGolemEntity;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GolemKitGolemRenderer extends MobRenderer<GolemKitGolemEntity, IronGolemModel<GolemKitGolemEntity>> {
    public GolemKitGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new IronGolemModel<>(context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7F);
        this.addLayer(new GolemKitGolemCrackFeatureRenderer(this));
    }

    @Override
    public ResourceLocation getTexture(GolemKitGolemEntity entity){
        return new ResourceLocation("textures/entity/iron_golem/iron_golem.png");
    }

}
