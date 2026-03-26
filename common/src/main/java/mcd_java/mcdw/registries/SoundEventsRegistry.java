/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.registries;

import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;

public class SoundEventsRegistry {
    public static final Identifier ECHO_SOUND = new Identifier("mcdw:echo_sound");
    public static final SoundEvent ECHO_SOUND_EVENT = SoundEvent.createVariableRangeEvent(ECHO_SOUND);

    public static void register(){
        registerSound(ECHO_SOUND, ECHO_SOUND_EVENT);
    }

    @SuppressWarnings("SameParameterValue")
    protected static void registerSound(Identifier soundIdentifier, SoundEvent soundEvent) {
        Registry.register(Registries.SOUND_EVENT, soundIdentifier, soundEvent);

    }
}
