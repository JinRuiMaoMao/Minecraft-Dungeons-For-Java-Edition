package jinrui.mcdar.entities.renderers;

import jinrui.mcdar.entities.BuzzyNestBeeEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class BuzzyNestBeeRenderer extends MobRenderer<BuzzyNestBeeEntity, BeeModel<BuzzyNestBeeEntity>> {
    private static final ResourceLocation ANGRY_TEXTURE = new ResourceLocation("textures/entity/bee/bee_angry.png");
    private static final ResourceLocation ANGRY_NECTAR_TEXTURE = new ResourceLocation("textures/entity/bee/bee_angry_nectar.png");
    private static final ResourceLocation PASSIVE_TEXTURE = new ResourceLocation("textures/entity/bee/bee.png");
    private static final ResourceLocation NECTAR_TEXTURE = new ResourceLocation("textures/entity/bee/bee_nectar.png");

    public BuzzyNestBeeRenderer(EntityRendererProvider.Context context) {
        super(context, new BeeModel<>(context.bakeLayer(ModelLayers.BEE)), 0.4F);
    }

    public ResourceLocation getTexture(BuzzyNestBeeEntity beeEntity) {
        if (beeEntity.isAngry())
            return beeEntity.hasNectar() ? ANGRY_NECTAR_TEXTURE : ANGRY_TEXTURE;
        else
            return beeEntity.hasNectar() ? NECTAR_TEXTURE : PASSIVE_TEXTURE;
    }
}
