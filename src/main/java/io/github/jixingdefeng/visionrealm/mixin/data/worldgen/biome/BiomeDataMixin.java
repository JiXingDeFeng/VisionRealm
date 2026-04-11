package io.github.jixingdefeng.visionrealm.mixin.data.worldgen.biome;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.jixingdefeng.visionrealm.core.biome.AnomalyBiomes;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.biome.BiomeData;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(BiomeData.class)
public class BiomeDataMixin {
    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void bootstrap(BootstrapContext<Biome> context, CallbackInfo ci, @Local(ordinal = 0) HolderGetter<PlacedFeature> holderGetter, @Local(ordinal = 1) HolderGetter<ConfiguredWorldCarver<?>> holderGetter1) {
        context.register(AnomalyBiomes.BLOOD_CORRODED_CHERRY_GROVE, OverworldBiomes.meadowOrCherryGrove(holderGetter, holderGetter1, true));
        context.register(AnomalyBiomes.WITHERED_CHERRY_GROVE, OverworldBiomes.meadowOrCherryGrove(holderGetter, holderGetter1, true));
        context.register(AnomalyBiomes.ANOMALY_DESERT, OverworldBiomes.desert(holderGetter, holderGetter1));
    }
}
