package mcd_java.api;

import mcd_java.api.interfaces.IMcdaBooleans;
import net.minecraft.world.entity.player.Player;

public class BooleanHelper {

    public static boolean isFireTrailEnabled(Player playerEntity) {
        return ((IMcdaBooleans) playerEntity).isFireTrailEnabled();
    }

    public static void setFireTrailEnabled(Player pe, boolean fireTrailEnabled) {
        ((IMcdaBooleans) pe).setFireTrailEnabled(fireTrailEnabled);
    }
}
