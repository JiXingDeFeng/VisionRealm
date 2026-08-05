package io.github.jixingdefeng.visionrealm.content.world.level.dimension;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

public class ModDimensionTypes {
    public static final ResourceKey<DimensionType> VISIONREALM = ResourceKey.create(Registries.DIMENSION_TYPE, location("visionrealm"));
    public static final ResourceLocation VISIONREALM_EFFECTS = location("visionrealm");

    private static ResourceLocation location(String name) {
        return ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, name);

    }
}
