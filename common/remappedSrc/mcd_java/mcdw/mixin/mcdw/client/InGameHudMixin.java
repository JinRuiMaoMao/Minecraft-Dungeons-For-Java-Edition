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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
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

    @Shadow @Final private static ResourceLocation ICONS;

    public InGameHudMixin(Minecraft client) {
        this.client = client;
    }

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F", shift = At.Shift.AFTER))
    private void renderOffhandCrosshair(GuiGraphics context, CallbackInfo ci) {
        if (CompatibilityFlags.noOffhandConflicts) {
            Player player = client.player;
            if (player == null)
                return;
            if (player.getOffhandItem().getItem() instanceof IOffhandAttack && PlayerAttackHelper.mixAndMatchWeapons(player)) {

                Options gameOptions = this.client.options;
                if (gameOptions.getCameraType().isFirstPerson()) {
                    if (this.client.gameMode != null) {
                        if (this.client.gameMode.getPlayerMode() != GameType.SPECTATOR || mcdw$shouldRenderSpectatorCrosshair(this.client.hitResult)) {
                            if (this.client.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
                                PlayerAttackHelper.mcdw$switchModifiers(player, player.getMainHandItem(), player.getOffhandItem());
                                float offhandAttackCooldownProgress = ((IDualWielding) player).mcdw$getOffhandAttackCooldownProgress(0.0f);
                                boolean bl = false;
                                if (this.client.crosshairPickEntity != null && this.client.crosshairPickEntity instanceof LivingEntity && offhandAttackCooldownProgress >= 1.0f) {
                                    bl = ((IDualWielding) player).mcdw$getOffhandAttackCooldownProgressPerTick() > 5.0f;
                                    bl &= this.client.crosshairPickEntity.isAlive();
                                }
                                PlayerAttackHelper.mcdw$switchModifiers(player, player.getOffhandItem(), player.getMainHandItem());
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
            Level world = this.client.level;
            return world.getBlockState(blockPos).getMenuProvider(world, blockPos) != null;
        } else {
            return false;
        }
    }
}
