package mcd_java.mcda.networking;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.api.BooleanHelper;
import mcd_java.mcda.api.interfaces.IMcdaBooleans;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class McdaC2SPackets {
    public static void registerC2SReceivers(){
        ServerPlayNetworking.registerGlobalReceiver(Mcda.ID("fire_trail_toggle"), (server, PlayerEntity, handler, buf, responseSender) -> {
            boolean isFireTrailEnabled = buf.readBoolean();
            server.execute(() -> {
                BooleanHelper.setFireTrailEnabled(PlayerEntity, isFireTrailEnabled);
                ((IMcdaBooleans) PlayerEntity).setFireTrailEnabled(isFireTrailEnabled);
            });
        });
    }
}