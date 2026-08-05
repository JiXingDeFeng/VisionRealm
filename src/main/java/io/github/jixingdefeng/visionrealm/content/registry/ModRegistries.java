package io.github.jixingdefeng.visionrealm.content.registry;

import com.mojang.serialization.MapCodec;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.api.incident.RegisteredIncident;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ModRegistries {
    public static final ResourceKey<Registry<RegisteredIncident<?, ?>>> INCIDENT = createRegistryKey("incident");
    public static final ResourceKey<Registry<ErosionType>> EROSION_TYPE = createRegistryKey("erosion_type");
    public static final ResourceKey<Registry<MapCodec<? extends ParticleConfig>>> PARTICLE_CONFIG_TYPE = createRegistryKey("particle_config_type");

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, name));
    }
}
