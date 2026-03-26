package mcd_java.registries;

import mcd_java.Mcda;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SoundsRegistry {
    /*
     *   Sound used for DODGE_SOUND is Bamboo Swing, C3 by InspectorJ,
     *   and it can be found at the following address:
     *   https://freesound.org/people/InspectorJ/sounds/394454/.
     *   It was used under the CC BY 3.0. The License may be
     *   viewed here: https://creativecommons.org/licenses/by/3.0/.
     */

    public static final ResourceLocation DODGE_SOUND = new ResourceLocation(Mcda.MOD_ID, "dodge");
    public static SoundEvent DODGE_SOUND_EVENT = SoundEvent.createVariableRangeEvent(DODGE_SOUND);

    public static void register(){
        Registry.register(BuiltInRegistries.SOUND_EVENT, DODGE_SOUND, DODGE_SOUND_EVENT);
    }
}
