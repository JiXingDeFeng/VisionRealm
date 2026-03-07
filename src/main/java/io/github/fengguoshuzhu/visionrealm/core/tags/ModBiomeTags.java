package io.github.fengguoshuzhu.visionrealm.core.tags;

import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class ModBiomeTags {
    public static final TagKey<Biome> EROSION_ZONE = create("erosion_zone");

    private ModBiomeTags() {}

    private static TagKey<Biome> create(String name) {
        return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, name));
    }
}
