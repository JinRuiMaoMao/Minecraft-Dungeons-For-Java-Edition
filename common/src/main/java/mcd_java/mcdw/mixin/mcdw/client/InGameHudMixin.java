/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw.client;

import mcd_java.mcdw.api.interfaces.IDualWielding;
import mcd_java.mcdw.api.interfaces.IOffhandAttack;
import mcd_java.mcdw.api.util.PlayerAttackHelper;
import mcd_java.mcdw.configs.CompatibilityFlags;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public class InGameHudMixin {

    @Shadow @Final @Mutable
    private Minecraft client;

    @Shadow
    private int scaledHeight;

    @Shadow
    private int scaledWidth;

    @Shadow @Final private static Identifier ICONS;

    public InGameHudMixin(Minecraft client) {
        this.client = client;
    }

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F", shift = At.Shift.AFTER))
    private void renderOffhandCrosshair(GuiGraphics context, CallbackInfo ci) {
        if (CompatibilityFlags.noOffhandConflicts) {
            PlayerEntity player = client.PlayerEntity;
            if (PlayerEntity == null)
                return;
            if (PlayerEntity.getOffhandItem().getItem() instanceof IOffhandAttack && PlayerAttackHelper.mixAndMatchWeapons(PlayerEntity)) {

                Options gameOptions = this.client.options;
                if (gameOptions.getCameraType().isFirstPerson()) {
                    if (this.client.gameMode != null) {
                        if (this.client.gameMode.getPlayerMode() != GameType.SPECTATOR || mcdw$shouldRenderSpectatorCrosshair(this.client.hitResult)) {
                            if (this.client.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
                                PlayerAttackHelper.mcdw$switchModifiers(PlayerEntity, PlayerEntity.getMainHandItem(), PlayerEntity.getOffhandItem());
                                float offhandAttackCooldownProgress = ((IDualWielding) PlayerEntity).mcdw$getOffhandAttackCooldownProgress(0.0f);
                                boolean bl = false;
                                if (this.client.crosshairPickEntity != null && this.client.crosshairPickEntity instanceof LivingEntity && offhandAttackCooldownProgress >= 1.0f) {
                                    bl = ((IDualWielding) PlayerEntity).mcdw$getOffhandAttackCooldownProgressPerTick() > 5.0f;
                                    bl &= this.client.crosshairPickEntity.isAlive();
                                }
                                PlayerAttackHelper.mcdw$switchModifiers(PlayerEntity, PlayerEntity.getOffhandItem(), PlayerEntity.getMainHandItem());
                                int height = this.scaledHeight / 2 - 7 + 16;
                                int width = this.scaledWidth / 2 - 8;
                                if (bl) {
                                    context.blit(ICONS, width, height + 8, 68, 94, 16, 16, 256, 256);
                                } else if (offhandAttackCooldownProgress < 1.0f) {
                                    int l = (int) (offhandAttackCooldownProgress * 17.0f);
                                    context.blit(ICONS, width, height + 8, 36, 94, 16, 4, 256, 256);
                                    context.blit(ICONS, width, height + 8, 52, 94, l, 4, 256, 256);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Unique
    private boolean mcdw$shouldRenderSpectatorCrosshair(HitResult hitResult) {
        if (hitResult == null) {
            return false;
        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult)hitResult).getEntity() instanceof MenuProvider;
        } else if (hitResult.getType() == HitResult.Type.BLOCK && this.client.level != null) {
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            World world = this.client.level;
            return world.getBlockState(blockPos).getMenuProvider(world, blockPos) != null;
        } else {
            return false;
        }
    }
}
