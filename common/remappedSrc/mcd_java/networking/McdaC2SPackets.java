package mcd_java.networking;

import mcd_java.Mcda;
import mcd_java.api.BooleanHelper;
import mcd_java.api.interfaces.IMcdaBooleans;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class McdaC2SPackets {
    public static void registerC2SReceivers(){
        ServerPlayNetworking.registerGlobalReceiver(Mcda.ID("fire_trail_toggle"), (server, player, handler, buf, responseSender) -> {
            boolean isFireTrailEnabled = buf.readBoolean();
            server.execute(() -> {
                BooleanHelper.setFireTrailEnabled(player, isFireTrailEnabled);
                ((IMcdaBooleans) player).setFireTrailEnabled(isFireTrailEnabled);
            });
        });
    }
}