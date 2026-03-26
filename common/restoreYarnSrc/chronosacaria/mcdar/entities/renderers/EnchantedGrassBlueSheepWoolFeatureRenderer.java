package jinrui.mcdar.entities.renderers;

import jinrui.mcdar.entities.EnchantedGrassBlueSheepEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SheepFurLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

public class EnchantedGrassBlueSheepWoolFeatureRenderer extends RenderLayer<EnchantedGrassBlueSheepEntity, SheepModel<EnchantedGrassBlueSheepEntity>> {
    private static final ResourceLocation SKIN = new ResourceLocation("textures/entity/sheep/sheep_fur.png");
    private final SheepFurModel<EnchantedGrassBlueSheepEntity> model;
    public EnchantedGrassBlueSheepWoolFeatureRenderer(RenderLayerParent<EnchantedGrassBlueSheepEntity, SheepModel<EnchantedGrassBlueSheepEntity>> context, EntityModelSet loader) {
        super(context);
        this.model = new SheepFurModel<>(loader.bakeLayer(ModelLayers.SHEEP_FUR));

    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, EnchantedGrassBlueSheepEntity sheepEntity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        float u;
        float t;
        float s;
        if (sheepEntity.isSheared()) {
            return;
        }
        if (sheepEntity.isInvisible()) {
            Minecraft minecraftClient = Minecraft.getInstance();
            boolean bl = minecraftClient.shouldEntityAppearGlowing(sheepEntity);
            if (bl) {
                this.getParentModel().copyPropertiesTo(this.model);
                this.model.prepareMobModel(sheepEntity, limbAngle, limbDistance, tickDelta);
                this.model.setupAnim(sheepEntity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
                VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.outline(SKIN));
                this.model.renderToBuffer(matrixStack, vertexConsumer, light, LivingEntityRenderer.getOverlayCoords(sheepEntity, 0.0f), 0.0f, 0.0f, 0.0f, 1.0f);
            }
            return;
        }
        if (sheepEntity.hasCustomName() && "Lilly".equals(sheepEntity.getName().getString())) {
            float[] fs = Sheep.getColorArray(DyeColor.byId(6));
            s = fs[0];
            t = fs[1];
            u = fs[2];
        } else {
            float[] hs = Sheep.getColorArray(DyeColor.BLUE);
            s = hs[0];
            t = hs[1];
            u = hs[2];
        }
        SheepFurLayer.coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, SKIN, matrixStack, vertexConsumerProvider, light, sheepEntity, limbAngle, limbDistance, animationProgress, headYaw, headPitch, tickDelta, s, t, u);
    }
}
