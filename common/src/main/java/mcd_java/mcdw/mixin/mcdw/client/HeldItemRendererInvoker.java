/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw.client;

import net.minecraft.client.PlayerEntity.LocalPlayer;
import net.minecraft.client.render.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemInHandRenderer.class)
public interface HeldItemRendererInvoker {

    @Invoker("getUsingItemHandRenderType")
    static ItemInHandRenderer.HandRenderSelection callGetUsingItemHandRenderType(LocalPlayer PlayerEntity) {
        throw new UnsupportedOperationException();
    }
}
