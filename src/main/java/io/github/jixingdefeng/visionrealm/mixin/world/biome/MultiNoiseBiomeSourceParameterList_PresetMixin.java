package io.github.jixingdefeng.visionrealm.mixin.world.biome;

import io.github.jixingdefeng.visionrealm.content.world.level.dimension.ModMultiNoiseBiomeSourceParameterLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(MultiNoiseBiomeSourceParameterList.Preset.class)
public class MultiNoiseBiomeSourceParameterList_PresetMixin {

    @Mutable
    @Shadow
    @Final
    static Map<ResourceLocation, MultiNoiseBiomeSourceParameterList.Preset> BY_NAME;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void init(CallbackInfo ci) {
        BY_NAME.put(
                ModMultiNoiseBiomeSourceParameterLists.VISIONREALM_PRESET.id(),
                ModMultiNoiseBiomeSourceParameterLists.VISIONREALM_PRESET
        );
    }
}
