/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class McdwMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("reach") && isReachEntityAttributesPresent()) {
            return false;
        }
        return true;
    }

    private static boolean isReachEntityAttributesPresent() {
        try {
            Class<?> fabricLoader = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object loader = fabricLoader.getMethod("getInstance").invoke(null);
            return (Boolean) fabricLoader.getMethod("isModLoaded", String.class).invoke(loader, "reach-entity-attributes");
        } catch (Throwable ignored) {
            // Not on Fabric or API not available at this phase.
        }
        try {
            Class<?> loadingModList = Class.forName("net.minecraftforge.fml.loading.LoadingModList");
            Object list = loadingModList.getMethod("get").invoke(null);
            Iterable<?> mods = (Iterable<?>) list.getClass().getMethod("getMods").invoke(list);
            for (Object mod : mods) {
                String modId = String.valueOf(mod.getClass().getMethod("getModId").invoke(mod));
                if ("reach_entity_attributes".equals(modId) || "reach-entity-attributes".equals(modId)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
            // Not on Forge or API not available at this phase.
        }
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
