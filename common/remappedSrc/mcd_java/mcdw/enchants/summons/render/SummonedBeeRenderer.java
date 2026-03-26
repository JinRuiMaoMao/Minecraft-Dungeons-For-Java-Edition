/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enchants.summons.render;

import mcd_java.mcdw.enchants.summons.entity.SummonedBeeEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
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
