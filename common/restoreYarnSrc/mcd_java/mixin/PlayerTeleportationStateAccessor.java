package mcd_java.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface PlayerTeleportationStateAccessor {

    @Accessor("inTeleportationState")
    void setInTeleportationState(boolean inTeleportationState);
}
