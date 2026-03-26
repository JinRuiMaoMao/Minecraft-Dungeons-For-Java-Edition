package jinrui.mcdar.entities.renderers;

import jinrui.mcdar.entities.BuzzyNestBeeEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.render.entity.EntityRendererProvider;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class BuzzyNestBeeRenderer extends MobRenderer<BuzzyNestBeeEntity, BeeModel<BuzzyNestBeeEntity>> {
    private static final Identifier ANGRY_TEXTURE = new Identifier("textures/entity/bee/bee_angry.png");
    private static final Identifier ANGRY_NECTAR_TEXTURE = new Identifier("textures/entity/bee/bee_angry_nectar.png");
    private static final Identifier PASSIVE_TEXTURE = new Identifier("textures/entity/bee/bee.png");
    private static final Identifier NECTAR_TEXTURE = new Identifier("textures/entity/bee/bee_nectar.png");

    public BuzzyNestBeeRenderer(EntityRendererProvider.Context context) {
        super(context, new BeeModel<>(context.bakeLayer(ModelLayers.BEE)), 0.4F);
    }

    public Identifier getTexture(BuzzyNestBeeEntity beeEntity) {
        if (beeEntity.isAngry())
            return beeEntity.hasNectar() ? ANGRY_NECTAR_TEXTURE : ANGRY_TEXTURE;
        else
            return beeEntity.hasNectar() ? NECTAR_TEXTURE : PASSIVE_TEXTURE;
    }
}
