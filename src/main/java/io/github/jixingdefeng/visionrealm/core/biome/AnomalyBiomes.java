package io.github.jixingdefeng.visionrealm.core.biome;

import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class AnomalyBiomes {
    public static final ResourceKey<Biome> BLOOD_CORRODED_CHERRY_GROVE = register("blood_corroded_cherry_grove");
    public static final ResourceKey<Biome> WITHERED_CHERRY_GROVE = register("withered_cherry_grove");
    public static final ResourceKey<Biome> ANOMALY_DESERT = register("anomaly_desert");

    private static ResourceKey<Biome> register(String key) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, key));
    }
}
