package jinrui.mcdar.entities.renderers;

import jinrui.mcdar.entities.EnchantedGrassBlueSheepEntity;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.render.entity.EntityRendererProvider;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.util.Identifier;

public class EnchantedGrassBlueSheepRenderer extends MobRenderer<EnchantedGrassBlueSheepEntity, SheepModel<EnchantedGrassBlueSheepEntity>> {
    public EnchantedGrassBlueSheepRenderer(EntityRendererProvider.Context context){
        super(context, new SheepModel<>(context.bakeLayer(ModelLayers.SHEEP)), 0.7f);
        this.addLayer(new EnchantedGrassBlueSheepWoolFeatureRenderer(this, context.getModelSet()));
    }

    @Override
    public Identifier getTexture(EnchantedGrassBlueSheepEntity entity){
        return new Identifier("textures/entity/sheep/sheep.png");
    }
}
