package io.github.jixingdefeng.visionrealm.core.registry;

import io.github.jixingdefeng.visionrealm.api.incident.RegisteredIncident;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ModRegistries {
    public static final ResourceKey<Registry<RegisteredIncident<?, ?>>> INCIDENT = createRegistryKey("incident");
    public static final ResourceKey<Registry<ErosionType>> EROSION_TYPE = createRegistryKey("erosion_type");
    public static final ResourceKey<Registry<ParticleConfig>> PARTICLE_CONFIG = createRegistryKey("particle_config");

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, name));
    }
}
