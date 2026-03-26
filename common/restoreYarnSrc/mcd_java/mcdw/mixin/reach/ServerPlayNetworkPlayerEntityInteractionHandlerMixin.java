/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.reach;

import mcd_java.mcdw.api.util.PlayerAttackHelper;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Copyright 2019 Erlend Åmdal
 * <br/><br/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <br/><br/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 * <br/><br/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 * <br/><br/>
 * The following code is from Reach Entity Attributes and can be found here:
 * <a href = "https://github.com/JamiesWhiteShirt/reach-entity-attributes/blob/1.19/src/main/java/com/jamieswhiteshirt/reachentityattributes/mixin/PlayerEntityInteractionHandlerMixin.java#L15">PlayerEntityInteractionHandlerMixin</a>
 */

@Mixin(targets = "net.minecraft.server.network.ServerPlayNetworkHandler$1")
public abstract class ServerPlayNetworkPlayerEntityInteractionHandlerMixin implements ServerboundInteractPacket.Handler {

    @Shadow @Final
    ServerGamePacketListenerImpl field_28963;

    @Shadow @Final
    Entity field_28962;

    @Inject(method = "attack", at = @At("HEAD"), require = 1, allow = 1, cancellable = true)
    private void mcdw$attack(CallbackInfo ci) {
        if (!PlayerAttackHelper.mcdw$isEntityWithinAttackRange(this.field_28963.player, this.field_28962)) {
            ci.cancel();
        }
    }
}
