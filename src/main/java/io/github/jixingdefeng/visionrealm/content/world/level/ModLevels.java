package io.github.jixingdefeng.visionrealm.content.world.level;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ModLevels {
    public static final ResourceKey<Level> VISIONREALM = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "visionrealm"));
}
