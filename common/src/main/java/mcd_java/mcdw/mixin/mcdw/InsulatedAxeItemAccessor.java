/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.item.AxeItem;
import net.minecraft.block.Block;

@Mixin(AxeItem.class)
public interface InsulatedAxeItemAccessor {
    @Accessor
    static Map<Block, Block> getSTRIPPED_BLOCKS() {
        throw new UnsupportedOperationException();
    }
}
