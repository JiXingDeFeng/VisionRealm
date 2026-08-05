package io.github.jixingdefeng.visionrealm.core.data.worldgen.features;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class ModTreeFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> BLOOD_CORRODED_CHERRY = createKey("blood_corroded_cherry");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BLOOD_CORRODED_CHERRY_BEES_005 = createKey("blood_corroded_cherry_bees_005");

    public static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, name));
    }
}
