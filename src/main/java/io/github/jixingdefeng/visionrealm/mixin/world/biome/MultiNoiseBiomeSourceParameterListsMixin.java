package io.github.jixingdefeng.visionrealm.mixin.world.biome;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.content.world.level.dimension.ModMultiNoiseBiomeSourceParameterLists;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiNoiseBiomeSourceParameterLists.class)
public class MultiNoiseBiomeSourceParameterListsMixin {

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void bootstrap(
            BootstrapContext<MultiNoiseBiomeSourceParameterList> context,
            CallbackInfo ci,
            @Local HolderGetter<Biome> holderGetter
    ) {
        context.register(
                ResourceKey.create(
                        Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST,
                        ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "visionrealm")
                ),
                new MultiNoiseBiomeSourceParameterList(
                        ModMultiNoiseBiomeSourceParameterLists.VISIONREALM_PRESET, holderGetter
                )
        );
    }
}
