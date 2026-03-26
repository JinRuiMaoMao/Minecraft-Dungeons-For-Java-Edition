package jinrui.mcdar.artifacts.beacon;

import jinrui.mcdar.Mcdar;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MyRenderType extends RenderType {
    private final static ResourceLocation beaconBeamCore = new ResourceLocation(Mcdar.MOD_ID + ":textures/misc/beacon_beam_core.png");
    private final static ResourceLocation beaconBeamMain = new ResourceLocation(Mcdar.MOD_ID + ":textures/misc/beacon_beam_main.png");
    private final static ResourceLocation beaconBeamGlow = new ResourceLocation(Mcdar.MOD_ID + ":textures/misc/beacon_beam_glow.png");

    public MyRenderType(String name, VertexFormat vertexFormat, VertexFormat.Mode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    public static final Function<ResourceLocation, RenderType> BEACON_BEAM_MAIN = Util.memoize(identifier -> {
        RenderStateShard.TextureStateShard textureState = new RenderStateShard.TextureStateShard(identifier, false, false);
        return RenderType.create(
                "BeaconBeamMain",
                DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(new TextureStateShard(beaconBeamMain, false, false))
                        .setLayeringState(RenderStateShard.NO_LAYERING)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(false));
    });

    public static final Function<ResourceLocation, RenderType> BEACON_BEAM_GLOW = Util.memoize(identifier -> {
        RenderStateShard.TextureStateShard textureState = new RenderStateShard.TextureStateShard(identifier, false, false);
        return RenderType.create(
                "BeaconBeamGlow",
                DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(new TextureStateShard(beaconBeamGlow, false, false))
                        .setLayeringState(RenderStateShard.NO_LAYERING)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(false));
    });

    public static final Function<ResourceLocation, RenderType> BEACON_BEAM_CORE = Util.memoize(identifier -> {
        RenderStateShard.TextureStateShard textureState = new RenderStateShard.TextureStateShard(identifier, false, false);
        return RenderType.create(
                "BeaconBeamGlow",
                DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(new TextureStateShard(beaconBeamCore, false, false))
                        .setLayeringState(RenderStateShard.NO_LAYERING)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(false));
    });
}