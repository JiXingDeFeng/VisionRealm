package io.github.fengguoshuzhu.visionrealm.core.registry;

import io.github.fengguoshuzhu.visionrealm.common.world.erosion.block.BlockErosionKey;
import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ModRegistryKeys {

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, name));
    }
}
