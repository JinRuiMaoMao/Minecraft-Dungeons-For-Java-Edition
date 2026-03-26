package jinrui.mcdar.entities.renderers;

import jinrui.mcdar.entities.EnchantedGrassRedSheepEntity;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class EnchantedGrassRedSheepRenderer extends MobRenderer<EnchantedGrassRedSheepEntity,
        SheepModel<EnchantedGrassRedSheepEntity>> {
    public EnchantedGrassRedSheepRenderer(EntityRendererProvider.Context context){
        super(context, new SheepModel<>(context.bakeLayer(ModelLayers.SHEEP)), 0.7F);
        this.addLayer(new EnchantedGrassRedSheepWoolFeatureRenderer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTexture(EnchantedGrassRedSheepEntity entity){
        return new ResourceLocation("textures/entity/sheep/sheep.png");
    }

}
